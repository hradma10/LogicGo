package cz.logicgo.ui.handlers.shikaku;

import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.gameClasses.shikaku.ShikakuUtils;
import cz.logicgo.core.misc.enums.keys.game.GeneralGameEvents;
import cz.logicgo.ui.commands.CommandExecutor;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.ShikakuGameController;
import cz.logicgo.core.gameClasses.keys.KeyEventDTO;
import cz.logicgo.ui.misc.windows.AlertBox;
import javafx.scene.input.KeyEvent;

import java.io.IOException;

import static cz.logicgo.ui.utils.KeyEventUtils.fromJavaFx;


public class KeyPressedShikakuHandler extends ShikakuHandlerBase {

    public KeyPressedShikakuHandler(Shikaku shikakuGame, ShikakuGameController shikakuGameController) {
        super(shikakuGame, shikakuGameController);
    }

    public void onKeyPressed(KeyEvent keyEvent) throws IOException {
        if (this.isKeyPressed(keyEvent)) return;
        boolean forceRedraw = false;

        ShikakuGameController con = getShikakuGameController();
        CommandExecutor commandExecutor = con.getCommandExecutor();
        User user = getShikakuGame().getPlayer();

        var hotkeys = user.getSavedHotkeys().values();
        KeyEventDTO keyEventDTO = fromJavaFx(keyEvent);

        KeyEventDTO matching = hotkeys.stream().filter(keyEventDTO::matches).findFirst().orElse(null);

        if (matching != null) {
            switch (matching.getKeystrokeEvent()) {
                case GeneralGameEvents generalGameEvents -> {
                    switch (generalGameEvents) {
                        case SAVE -> con.saveGame(true);
                        case UNDO -> {
                            commandExecutor.undo();
                            forceRedraw = true;
                        }
                        case REDO -> {
                            commandExecutor.redo();
                            forceRedraw = true;
                        }
                        case RESET -> {
                            if (AlertBox.initRestartGame()) {
                                con.onRestart();
                                forceRedraw = true;
                            }
                        }
                        case SOLUTION -> con.openSolution();
                        case HINT -> con.openHint();
                        case EXPORT -> con.openExport();
                        case PAUSE -> con.pause();
                        case EXIT -> con.onExit();
                    }
                }
                default -> {
                }
            }
        }

        switch (keyEvent.getCode()) {
            case ESCAPE -> {

                forceRedraw = true;
            }
        }

        if (forceRedraw) {
            con.redrawCanvases();
        }

        if (ShikakuUtils.isGameFinished(getShikakuGame())) {
            con.gameFinished();
        }

        keyEvent.consume();
    }
}
