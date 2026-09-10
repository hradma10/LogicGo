package cz.logicgo.ui.handlers.bridgesGame;

import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.gameClasses.bridge.BridgeElement;
import cz.logicgo.core.gameClasses.bridge.Island;
import cz.logicgo.core.gameClasses.bridge.IslandBridge;
import cz.logicgo.core.misc.enums.hints.BridgeHintType;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.BridgeGameController;
import javafx.scene.input.MouseEvent;

import java.util.Optional;


public class MouseMovedBridgeHandler extends BridgeHandlerBase {

    public MouseMovedBridgeHandler(Bridge bridge, BridgeGameController bridgeGameController) {
        super(bridge, bridgeGameController);
    }

    public void onMouseMoved(MouseEvent mouseEvent) {
        BridgeGameController bgc = this.getBridgeGameController();

        bgc.updateMousePosition(mouseEvent.getX(), mouseEvent.getY());

        if (!bgc.isHintChoice()) return;

        double x = mouseEvent.getX();
        double y = mouseEvent.getY();

        Optional<BridgeElement> optionalElement = getElementAt(x, y);

        if (optionalElement.isEmpty()) {
            bgc.setHoveredElements((BridgeElement[]) null);
            return;
        }

        BridgeElement hovered = optionalElement.get();
        BridgeHintType activeHint = bgc.getActiveHint();
        if (activeHint == null) return;

        switch (activeHint) {
            case CHECK_ISLAND, CHECK_COUNT -> {
                if (hovered instanceof Island) {
                    bgc.setHoveredElements(hovered);
                } else {
                    bgc.setHoveredElements((BridgeElement[]) null);
                }
            }
            case CHECK_BRIDGE -> {
                if (hovered instanceof IslandBridge) {
                    bgc.setHoveredElements(hovered);
                } else {
                    bgc.setHoveredElements((BridgeElement[]) null);
                }
            }
        }
    }
}
