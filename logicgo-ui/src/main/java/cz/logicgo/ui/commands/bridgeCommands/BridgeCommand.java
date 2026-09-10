package cz.logicgo.ui.commands.bridgeCommands;


import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.ui.commands.Command;

import java.time.LocalDateTime;

public abstract class BridgeCommand extends Command {

    final private Bridge bridgeGame;

    protected BridgeCommand(Bridge bridgeGame) {
        this.bridgeGame = bridgeGame;
    }

    public BridgeCommand(LocalDateTime timestamp, Bridge bridgeGame) {
        super(timestamp);
        this.bridgeGame = bridgeGame;
    }

    public Bridge getBridgeGame() {
        return bridgeGame;
    }
}
