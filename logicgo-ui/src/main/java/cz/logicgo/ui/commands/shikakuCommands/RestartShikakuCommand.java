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

import static cz.logicgo.ui.commands.CommandType.REPLACE_SHIKAKU;


public class RestartShikakuCommand extends ShikakuCommand {

    final private List<ShikakuRectangle> oldRectangles;
    final private List<ShikakuRectangle> newRectangles;
    final int lastId;
    final static int newId = 0;

    public RestartShikakuCommand(Shikaku shikakuGame, List<ShikakuRectangle> oldRectangles, List<ShikakuRectangle> newRectangles, int lastId) {
        super(shikakuGame);
        this.lastId = lastId;
        this.oldRectangles = oldRectangles;
        this.newRectangles = newRectangles;
    }

    public RestartShikakuCommand(LocalDateTime timestamp, Shikaku shikakuGame, List<ShikakuRectangle> oldRectangles, List<ShikakuRectangle> newRectangles, int lastId) {
        super(timestamp, shikakuGame);
        this.lastId = lastId;
        this.oldRectangles = oldRectangles;
        this.newRectangles = newRectangles;
    }

    @Override
    public byte getType() {
        return REPLACE_SHIKAKU.getType();
    }

    @Override
    public void execute() {
        shikakuGame.setRectangles(newRectangles);
        shikakuGame.setCounter(newId);
    }

    @Override
    public void undo() {
        shikakuGame.setRectangles(oldRectangles);
        shikakuGame.setCounter(lastId);
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

            byte[] oldBytes = ShikakuConverters.shikakuRectangleListToBytes((oldRectangles));
            int oldLength = oldBytes.length;
            dos.writeInt(oldLength);
            dos.write(oldBytes);

            byte[] newBytes = ShikakuConverters.shikakuRectangleListToBytes((newRectangles));
            int newLength = newBytes.length;
            dos.writeInt(newLength);
            dos.write(newBytes);

            dos.writeInt(lastId);

            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
