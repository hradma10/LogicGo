package cz.logicgo.ui.handlers.bridgesGame;


import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.BridgeGameController;
import javafx.event.Event;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.util.Objects;

public class MouseKeyBridgeHandlers {

    final private MouseClickedBridgeHandler mouseClickedHandler;
    final private KeyPressedBridgeHandler keyPressedHandler;
    final private KeyReleasedBridgeHandler keyReleasedHandler;
    final private MouseReleasedBridgeHandler mouseReleasedHandler;
    final private MouseMovedBridgeHandler mouseMovedHandler;
    final private MouseDraggedBridgeHandler mouseDraggedHandler;

    public MouseKeyBridgeHandlers(Bridge bridge, BridgeGameController bridgeGameController) {
        this.mouseDraggedHandler = new MouseDraggedBridgeHandler(bridge, bridgeGameController);
        mouseClickedHandler = new MouseClickedBridgeHandler(bridge, bridgeGameController);
        keyPressedHandler = new KeyPressedBridgeHandler(bridge, bridgeGameController);
        keyReleasedHandler = new KeyReleasedBridgeHandler(bridge, bridgeGameController);
        mouseReleasedHandler = new MouseReleasedBridgeHandler(bridge, bridgeGameController);
        mouseMovedHandler = new MouseMovedBridgeHandler(bridge, bridgeGameController);
    }

    public void onMouseClicked(MouseEvent mouseEvent) {
        if (consumedEvent(mouseEvent)) return;
        this.getMouseClickedHandler().onMouseClicked(mouseEvent);
    }

    private boolean consumedEvent(Event event) {
        if (Objects.requireNonNull(event.getSource()) instanceof Canvas) {
            return false;
        }
        event.consume();
        return true;
    }

    public void onKeyPressed(KeyEvent keyEvent) throws IOException {
        this.getKeyPressedHandler().onKeyPressed(keyEvent);
    }

    public void onKeyReleased(KeyEvent keyEvent) {
        if (consumedEvent(keyEvent)) return;
        this.getKeyReleasedHandler().onKeyReleased(keyEvent);
    }

    public void onMouseReleased(MouseEvent mouseEvent) {
        if (consumedEvent(mouseEvent)) return;
        this.getMouseReleasedHandler().onMouseReleased(mouseEvent);
    }

    public void onMouseMoved(MouseEvent mouseEvent) {
        if (consumedEvent(mouseEvent)) return;
        this.getMouseMovedHandler().onMouseMoved(mouseEvent);
    }

    public void onMouseDragged(MouseEvent mouseEvent) {
        if (consumedEvent(mouseEvent)) return;
        this.getMouseDraggedHandler().onMouseDragged(mouseEvent);
    }

    public MouseClickedBridgeHandler getMouseClickedHandler() {
        return mouseClickedHandler;
    }

    public KeyPressedBridgeHandler getKeyPressedHandler() {
        return keyPressedHandler;
    }

    public KeyReleasedBridgeHandler getKeyReleasedHandler() {
        return keyReleasedHandler;
    }

    public MouseReleasedBridgeHandler getMouseReleasedHandler() {
        return mouseReleasedHandler;
    }

    public MouseMovedBridgeHandler getMouseMovedHandler() {
        return mouseMovedHandler;
    }

    public MouseDraggedBridgeHandler getMouseDraggedHandler() {
        return mouseDraggedHandler;
    }
}

