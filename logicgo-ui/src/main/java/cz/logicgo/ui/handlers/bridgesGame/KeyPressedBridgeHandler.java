package cz.logicgo.ui.handlers.bridgesGame;

import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.gameClasses.bridge.BridgeElement;
import cz.logicgo.core.gameClasses.bridge.BridgeUtils;
import cz.logicgo.core.gameClasses.keys.KeyEventDTO;
import cz.logicgo.core.misc.enums.keys.game.GeneralGameEvents;
import cz.logicgo.ui.commands.CommandExecutor;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.BridgeGameController;
import cz.logicgo.ui.misc.windows.AlertBox;
import javafx.scene.input.KeyEvent;

import java.io.IOException;

import static cz.logicgo.ui.utils.KeyEventUtils.fromJavaFx;


public class KeyPressedBridgeHandler extends BridgeHandlerBase {

    public KeyPressedBridgeHandler(Bridge bridgeGame, BridgeGameController bridgeGameController) {
        super(bridgeGame, bridgeGameController);
    }

    public void onKeyPressed(KeyEvent keyEvent) throws IOException {
        boolean forceRedraw = false;
        User user = getBridgeGame().getPlayer();
        BridgeGameController con = getBridgeGameController();
        CommandExecutor commandExecutor = con.getCommandExecutor();

        var hotkeys = user.getSavedHotkeys().values();
        KeyEventDTO keyEventDTO = fromJavaFx(keyEvent);
        KeyEventDTO matching = hotkeys.stream().filter(keyEventDTO::matches).findFirst().orElse(null);
        con.saveGame(false);
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
                        case EXPORT -> con.openExport();
                        case HINT -> con.openHint();
                        case PAUSE -> con.pause();
                        case EXIT -> con.onExit();
                    }
                }
                default -> {
                }
            }
        }

        if (keyEvent.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
            if (con.isHintChoice()) {
                con.setHintChoice(false);
                con.setActiveHint(null);
                con.setHoveredElements((BridgeElement[]) null);
                con.clearHintVisuals();
                forceRedraw = true;
            }
        }

        if (forceRedraw) {
            con.redrawCanvases();
        }

        if (BridgeUtils.isGameFinished(getBridgeGame())) {
            con.gameFinished();
        }
        keyEvent.consume();
    }
}
