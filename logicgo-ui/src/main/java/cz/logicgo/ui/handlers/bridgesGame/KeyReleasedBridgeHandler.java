package cz.logicgo.ui.handlers.bridgesGame;

import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.gameClasses.keys.KeyEventDTO;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.BridgeGameController;
import javafx.scene.input.KeyEvent;

import static cz.logicgo.ui.utils.KeyEventUtils.fromJavaFx;


public class KeyReleasedBridgeHandler extends BridgeHandlerBase {

    public KeyReleasedBridgeHandler(Bridge bridge, BridgeGameController bridgeGameController) {
        super(bridge, bridgeGameController);
    }

    public void onKeyReleased(KeyEvent keyEvent) {
        var user = getBridgeGame().getPlayer();
        var hotkeys = user.getSavedHotkeys().values();
        KeyEventDTO keyEventDTO = fromJavaFx(keyEvent);

        if (hotkeys.stream().anyMatch(keyEventDTO::matches)) {
            this.setKeyPressed(keyEvent, false);
        }
    }
}
