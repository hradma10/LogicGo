package cz.logicgo.ui.handlers.shikaku;


import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.ShikakuGameController;
import javafx.scene.input.MouseEvent;

public class MouseMovedShikakuHandler extends ShikakuHandlerBase {

    public MouseMovedShikakuHandler(Shikaku shikaku, ShikakuGameController shikakuGameController) {
        super(shikaku, shikakuGameController);
    }

    public void onMouseMoved(MouseEvent mouseEvent) {
        ShikakuGameController con = this.getShikakuGameController();

        con.updateMousePosition(mouseEvent.getX(), mouseEvent.getY());

        if (!con.isHintChoice()) return;

        con.redrawCanvases();
    }
}
