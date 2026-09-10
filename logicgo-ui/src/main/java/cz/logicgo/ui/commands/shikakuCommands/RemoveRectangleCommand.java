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

public class RemoveRectangleCommand extends ShikakuCommand {
    ShikakuRectangle shikakuRectangle;


    public RemoveRectangleCommand(Shikaku shikakuGame, ShikakuRectangle shikakuRectangle) {
        super(shikakuGame);
        this.shikakuRectangle = shikakuRectangle;
    }

    public RemoveRectangleCommand(LocalDateTime timestamp, Shikaku shikakuGame, ShikakuRectangle shikakuRectangle) {
        super(timestamp, shikakuGame);
        this.shikakuRectangle = shikakuRectangle;
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

            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void undo() {
        shikakuGame.addRectangle(shikakuRectangle);
    }

    @Override
    public void execute() {
        shikakuGame.removeRectangle(shikakuRectangle);
    }

    @Override
    public byte getType() {
        return CommandType.REMOVE_RECTANGLE.getType();
    }
}
