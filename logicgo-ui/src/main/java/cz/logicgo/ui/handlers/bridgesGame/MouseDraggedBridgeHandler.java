package cz.logicgo.ui.handlers.bridgesGame;

import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.BridgeGameController;
import javafx.scene.input.MouseEvent;

public class MouseDraggedBridgeHandler extends BridgeHandlerBase {

    public MouseDraggedBridgeHandler(Bridge bridgeGame, BridgeGameController bridgeGameController) {
        super(bridgeGame, bridgeGameController);
    }

    public void onMouseDragged(MouseEvent mouseEvent) {
        BridgeGameController bgc = getBridgeGameController();

        bgc.updateMousePosition(mouseEvent.getX(), mouseEvent.getY());

        bgc.redrawCanvases();
    }
}
