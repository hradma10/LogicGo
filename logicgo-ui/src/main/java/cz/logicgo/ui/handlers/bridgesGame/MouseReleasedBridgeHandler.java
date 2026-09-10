package cz.logicgo.ui.handlers.bridgesGame;

import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.BridgeGameController;
import javafx.scene.input.MouseEvent;

public class MouseReleasedBridgeHandler extends BridgeHandlerBase {

    public MouseReleasedBridgeHandler(Bridge bridge, BridgeGameController bridgeGameController) {
        super(bridge, bridgeGameController);
    }

    public void onMouseReleased(MouseEvent mouseEvent) {

    }
}
