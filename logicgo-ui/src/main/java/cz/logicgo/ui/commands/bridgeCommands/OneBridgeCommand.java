package cz.logicgo.ui.commands.bridgeCommands;


import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.gameClasses.bridge.IslandBridge;

import java.time.LocalDateTime;

public abstract class OneBridgeCommand extends BridgeCommand {

    final private IslandBridge bridge;

    public OneBridgeCommand(Bridge bridgeGame, IslandBridge bridge) {
        super(bridgeGame);
        this.bridge = bridge;
    }

    public OneBridgeCommand(LocalDateTime timestamp, Bridge bridgeGame, IslandBridge bridge) {
        super(timestamp, bridgeGame);
        this.bridge = bridge;
    }

    public IslandBridge getBridge() {
        return bridge;
    }
}
