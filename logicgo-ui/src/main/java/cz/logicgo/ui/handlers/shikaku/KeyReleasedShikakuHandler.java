package cz.logicgo.ui.handlers.shikaku;

import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.gameClasses.keys.KeyEventDTO;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.ShikakuGameController;
import javafx.scene.input.KeyEvent;

import static cz.logicgo.ui.utils.KeyEventUtils.fromJavaFx;


public class KeyReleasedShikakuHandler extends ShikakuHandlerBase {

    public KeyReleasedShikakuHandler(Shikaku shikaku, ShikakuGameController shikakuGameController) {
        super(shikaku, shikakuGameController);
    }

    public void onKeyReleased(KeyEvent keyEvent) {
        var user = getShikakuGame().getPlayer();
        var hotkeys = user.getSavedHotkeys().values();
        KeyEventDTO keyEventDTO = fromJavaFx(keyEvent);

        if (hotkeys.stream().anyMatch(keyEventDTO::matches)) {
            this.setKeyPressed(keyEvent, false);
        }
    }
}
