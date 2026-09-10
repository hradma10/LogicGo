package cz.logicgo.ui.commands.shikakuCommands;


import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.ui.commands.Command;

import java.time.LocalDateTime;

public abstract class ShikakuCommand extends Command {

    final Shikaku shikakuGame;

    public ShikakuCommand(Shikaku shikakuGame) {
        this.shikakuGame = shikakuGame;
    }

    public ShikakuCommand(LocalDateTime timestamp, Shikaku shikakuGame) {
        super(timestamp);
        this.shikakuGame = shikakuGame;
    }
}
