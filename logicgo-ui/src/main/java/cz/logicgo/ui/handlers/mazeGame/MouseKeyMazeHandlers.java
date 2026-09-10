package cz.logicgo.ui.handlers.mazeGame;


import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.MazeGameController;
import javafx.event.Event;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.util.Objects;

public class MouseKeyMazeHandlers {

    final private MouseClickedMazeHandler mouseClickedHandler;
    final private KeyPressedMazeHandler keyPressedHandler;
    final private KeyReleasedMazeHandler keyReleasedHandler;
    final private MouseReleasedMazeHandler mouseReleasedHandler;
    final private MouseMovedMazeHandler mouseMovedHandler;

    public MouseKeyMazeHandlers(Maze maze, MazeGameController mazeGameController) {
        mouseClickedHandler = new MouseClickedMazeHandler(maze, mazeGameController);
        keyPressedHandler = new KeyPressedMazeHandler(maze, mazeGameController);
        keyReleasedHandler = new KeyReleasedMazeHandler(maze, mazeGameController);
        mouseReleasedHandler = new MouseReleasedMazeHandler(maze, mazeGameController);
        mouseMovedHandler = new MouseMovedMazeHandler(maze, mazeGameController);
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

    public MouseClickedMazeHandler getMouseClickedHandler() {
        return mouseClickedHandler;
    }

    public KeyPressedMazeHandler getKeyPressedHandler() {
        return keyPressedHandler;
    }

    public KeyReleasedMazeHandler getKeyReleasedHandler() {
        return keyReleasedHandler;
    }

    public MouseReleasedMazeHandler getMouseReleasedHandler() {
        return mouseReleasedHandler;
    }

    public MouseMovedMazeHandler getMouseMovedHandler() {
        return mouseMovedHandler;
    }
}

