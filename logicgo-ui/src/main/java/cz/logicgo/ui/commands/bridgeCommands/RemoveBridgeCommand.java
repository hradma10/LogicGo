package cz.logicgo.ui.commands.bridgeCommands;


import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.gameClasses.bridge.IslandBridge;
import cz.logicgo.ui.commands.CommandType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static cz.logicgo.core.util.boardConverters.BridgeConverters.serializeIslandBridges;


public class RemoveBridgeCommand extends OneBridgeCommand {

    public RemoveBridgeCommand(IslandBridge bridge, Bridge bridgeGame) {
        super(bridgeGame, bridge);
    }

    public RemoveBridgeCommand(LocalDateTime timestamp, IslandBridge bridge, Bridge bridgeGame) {
        super(timestamp, bridgeGame, bridge);
    }

    final public static CommandType type = CommandType.REMOVE_BRIDGE;

    @Override
    public byte getType() {
        return type.getType();
    }

    @Override
    public void execute() {
        getBridgeGame().getIslandBridges().remove(this.getBridge());
    }

    @Override
    public void undo() {
        getBridgeGame().getIslandBridges().add(this.getBridge());
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

            byte[] bytes = serializeIslandBridges(List.of(getBridge()));
            int length = bytes.length;
            dos.writeInt(length);
            dos.write(bytes);

            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
