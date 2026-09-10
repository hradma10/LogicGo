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


public class ChangeBridgeCountCommand extends OneBridgeCommand {
    final public static CommandType type = CommandType.CHANGE_BRIDGE_COUNT;
    final private int change;

    public ChangeBridgeCountCommand(Bridge bridgeGame, IslandBridge islandBridge, int change) {
        super(bridgeGame, islandBridge);
        this.change = change;
    }

    public ChangeBridgeCountCommand(LocalDateTime timestamp, IslandBridge bridge, Bridge bridgeGame, int change) {
        super(timestamp, bridgeGame, bridge);
        this.change = change;
    }

    @Override
    public byte getType() {
        return type.getType();
    }

    @Override
    public void execute() {
        int newCount = getBridge().getBridgeCount() + change;
        getBridge().setBridgeCount(newCount);
    }

    @Override
    public void undo() {
        int newCount = getBridge().getBridgeCount() - change;
        getBridge().setBridgeCount(newCount);
    }

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

            dos.writeInt(change);

            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
