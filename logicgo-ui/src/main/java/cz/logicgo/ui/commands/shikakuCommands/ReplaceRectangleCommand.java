package cz.logicgo.ui.commands.shikakuCommands;


import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.gameClasses.shikaku.ShikakuRectangle;
import cz.logicgo.core.util.boardConverters.ShikakuConverters;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static cz.logicgo.ui.commands.CommandType.REPLACE_RECTANGLES_SHIKAKU;


public class ReplaceRectangleCommand extends ShikakuCommand {
    final private List<ShikakuRectangle> oldRectangles;
    final private List<ShikakuRectangle> newRectangles;

    public ReplaceRectangleCommand(Shikaku shikakuGame, List<ShikakuRectangle> oldRectangles, List<ShikakuRectangle> newRectangles) {
        super(shikakuGame);
        this.oldRectangles = oldRectangles;
        this.newRectangles = newRectangles;
    }

    public ReplaceRectangleCommand(LocalDateTime timestamp, Shikaku shikakuGame, List<ShikakuRectangle> oldRectangles, List<ShikakuRectangle> newRectangles) {
        super(timestamp, shikakuGame);
        this.oldRectangles = oldRectangles;
        this.newRectangles = newRectangles;
    }

    @Override
    public byte getType() {
        return REPLACE_RECTANGLES_SHIKAKU.getType();
    }

    @Override
    public void execute() {
        oldRectangles.forEach(shikakuGame::removeRectangle);
        newRectangles.forEach(shikakuGame::addRectangle);
    }

    @Override
    public void undo() {
        newRectangles.forEach(shikakuGame::removeRectangle);
        oldRectangles.forEach(shikakuGame::addRectangle);
    }

    @Override
    public byte[] getCommandsAsBytes() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            byte type = getType();
            dos.writeByte(type);

            long epochSecond = getTimestamp().toEpochSecond(ZoneOffset.UTC);
            int nano = getTimestamp().getNano();

            dos.writeLong(epochSecond);
            dos.writeInt(nano);

            byte[] bytes = ShikakuConverters.shikakuRectangleListToBytes(oldRectangles);
            int length = bytes.length;
            dos.writeInt(length);
            dos.write(bytes);

            byte[] bytesList = ShikakuConverters.shikakuRectangleListToBytes(newRectangles);
            int lengthList = bytes.length;
            dos.writeInt(lengthList);
            dos.write(bytesList);

            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
