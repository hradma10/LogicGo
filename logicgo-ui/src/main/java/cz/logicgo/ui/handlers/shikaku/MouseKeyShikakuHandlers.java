package cz.logicgo.ui.handlers.shikaku;

import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.ShikakuGameController;
import javafx.event.Event;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.util.Objects;

public class MouseKeyShikakuHandlers {

    final private MouseClickedShikakuHandler mouseClickedHandler;
    final private MousePressedShikakuHandler mousePressedHandler;
    final private MouseDraggedShikakuHandler mouseDraggedHandler;
    final private MouseReleasedShikakuHandler mouseReleasedHandler;
    final private MouseMovedShikakuHandler mouseMovedHandler;
    final private KeyPressedShikakuHandler keyPressedHandler;
    final private KeyReleasedShikakuHandler keyReleasedHandler;

    public MouseKeyShikakuHandlers(Shikaku shikaku, ShikakuGameController shikakuGameController) {
        this.mouseClickedHandler = new MouseClickedShikakuHandler(shikaku, shikakuGameController);
        this.mousePressedHandler = new MousePressedShikakuHandler(shikaku, shikakuGameController);
        this.mouseDraggedHandler = new MouseDraggedShikakuHandler(shikaku, shikakuGameController);
        this.mouseReleasedHandler = new MouseReleasedShikakuHandler(shikaku, shikakuGameController);
        this.mouseMovedHandler = new MouseMovedShikakuHandler(shikaku, shikakuGameController);
        this.keyPressedHandler = new KeyPressedShikakuHandler(shikaku, shikakuGameController);
        this.keyReleasedHandler = new KeyReleasedShikakuHandler(shikaku, shikakuGameController);
    }

    public void onMouseClicked(MouseEvent mouseEvent) {
        if (consumedEvent(mouseEvent)) return;
        this.getMouseClickedHandler().onMouseClicked(mouseEvent);
    }

    public void onMousePressed(MouseEvent mouseEvent) {
        if (consumedEvent(mouseEvent)) return;
        this.getMousePressedHandler().onMousePressed(mouseEvent);
    }

    public void onMouseDragged(MouseEvent mouseEvent) {
        if (consumedEvent(mouseEvent)) return;
        this.getMouseDraggedHandler().onMouseDragged(mouseEvent);
    }

    public void onMouseReleased(MouseEvent mouseEvent) {
        if (consumedEvent(mouseEvent)) return;
        this.getMouseReleasedHandler().onMouseReleased(mouseEvent);
    }

    public void onMouseMoved(MouseEvent mouseEvent) {
        if (consumedEvent(mouseEvent)) return;
        this.getMouseMovedHandler().onMouseMoved(mouseEvent);
    }

    public void onKeyPressed(KeyEvent keyEvent) throws IOException {
        this.getKeyPressedHandler().onKeyPressed(keyEvent);
    }

    public void onKeyReleased(KeyEvent keyEvent) {
        if (consumedEvent(keyEvent)) return;
        this.getKeyReleasedHandler().onKeyReleased(keyEvent);
    }

    private boolean consumedEvent(Event event) {
        if (Objects.requireNonNull(event.getSource()) instanceof Canvas) {
            return false;
        }
        event.consume();
        return true;
    }

    public MouseClickedShikakuHandler getMouseClickedHandler() {
        return mouseClickedHandler;
    }

    public MousePressedShikakuHandler getMousePressedHandler() {
        return mousePressedHandler;
    }

    public MouseDraggedShikakuHandler getMouseDraggedHandler() {
        return mouseDraggedHandler;
    }

    public MouseReleasedShikakuHandler getMouseReleasedHandler() {
        return mouseReleasedHandler;
    }

    public MouseMovedShikakuHandler getMouseMovedHandler() {
        return mouseMovedHandler;
    }

    public KeyPressedShikakuHandler getKeyPressedHandler() {
        return keyPressedHandler;
    }

    KeyReleasedShikakuHandler getKeyReleasedHandler() {
        return keyReleasedHandler;
    }
}
