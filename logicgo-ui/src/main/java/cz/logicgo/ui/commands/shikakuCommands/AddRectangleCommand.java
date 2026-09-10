package cz.logicgo.ui.commands.shikakuCommands;


import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.gameClasses.shikaku.ShikakuRectangle;
import cz.logicgo.core.util.boardConverters.ShikakuConverters;
import cz.logicgo.ui.commands.CommandType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class AddRectangleCommand extends ShikakuCommand {

    ShikakuRectangle shikakuRectangle;
    List<ShikakuRectangle> removedRectangles;

    public AddRectangleCommand(Shikaku shikakuGame, ShikakuRectangle shikakuRectangle, List<ShikakuRectangle> removedRectangles) {
        super(shikakuGame);
        this.shikakuRectangle = shikakuRectangle;
        this.removedRectangles = removedRectangles;
    }

    public AddRectangleCommand(LocalDateTime timestamp, Shikaku shikakuGame, ShikakuRectangle shikakuRectangle, List<ShikakuRectangle> removedRectangles) {
        super(timestamp, shikakuGame);
        this.shikakuRectangle = shikakuRectangle;
        this.removedRectangles = removedRectangles;
    }

    @Override
    public void execute() {
        removedRectangles.forEach(shikakuGame::removeRectangle);
        shikakuGame.addRectangle(shikakuRectangle);
    }

    @Override
    public void undo() {
        shikakuGame.removeRectangle(shikakuRectangle);
        removedRectangles.forEach(shikakuGame::addRectangle);
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

            byte[] bytes = ShikakuConverters.shikakuRectangleListToBytes(List.of(shikakuRectangle));
            int length = bytes.length;
            dos.writeInt(length);
            dos.write(bytes);

            byte[] bytesList = ShikakuConverters.shikakuRectangleListToBytes(removedRectangles);
            int lengthList = bytes.length;
            dos.writeInt(lengthList);
            dos.write(bytesList);

            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte getType() {
        return CommandType.ADD_RECTANGLE.getType();
    }
}
