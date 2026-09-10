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


public class RestartBridgesCommandOne extends BridgeCommand {
    final public static CommandType type = CommandType.REPLACE_BRIDGES;

    final private List<IslandBridge> oldBridges;

    final private List<IslandBridge> newBridges;

    public RestartBridgesCommandOne(Bridge bridge, List<IslandBridge> oldBridges, List<IslandBridge> newBridges) {
        super(null, bridge);
        this.oldBridges = oldBridges;
        this.newBridges = newBridges;
    }

    public RestartBridgesCommandOne(LocalDateTime timestamp, Bridge bridgeGame, List<IslandBridge> oldBridges, List<IslandBridge> newBridges) {
        super(timestamp, bridgeGame);
        this.oldBridges = oldBridges;
        this.newBridges = newBridges;
    }

    @Override
    public void execute() {
        getBridgeGame().setIslandBridges(newBridges);
    }

    @Override
    public void undo() {
        getBridgeGame().setIslandBridges(oldBridges);
    }

    @Override
    public byte[] getCommandsAsBytes() {
        byte[] oldCellsByte = serializeIslandBridges(oldBridges);
        byte[] newCellsByte = serializeIslandBridges(newBridges);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            dos.writeByte(this.getType());

            long epochSecond = getTimestamp().toEpochSecond(ZoneOffset.UTC);
            int nano = getTimestamp().getNano();

            dos.writeLong(epochSecond);
            dos.writeInt(nano);

            dos.writeInt(oldCellsByte.length);
            dos.write(oldCellsByte);

            dos.writeInt(newCellsByte.length);
            dos.write(newCellsByte);

            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<IslandBridge> getOldBridges() {
        return oldBridges;
    }

    public List<IslandBridge> getNewBridges() {
        return newBridges;
    }

    @Override
    public byte getType() {
        return type.getType();
    }
}
