package cz.logicgo.ui.handlers.sudokuGame;


import cz.logicgo.engine.algorithms.sudoku.SudokuGame;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.SudokuGameController;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class MouseKeySudokuHandlers {

    final private MouseClickedSudokuHandler mouseClickedHandler;
    final private KeyPressedSudokuHandler keyPressedHandler;
    final private KeyReleasedSudokuHandler keyReleasedHandler;
    final private MouseReleasedSudokuHandler mouseReleasedHandler;
    final private MouseMovedSudokuHandler mouseMovedHandler;

    public MouseKeySudokuHandlers(SudokuGame sudokuGameState, SudokuGameController sudokuGameController) {
        mouseClickedHandler = new MouseClickedSudokuHandler(sudokuGameState, sudokuGameController);
        keyPressedHandler = new KeyPressedSudokuHandler(sudokuGameState, sudokuGameController);
        keyReleasedHandler = new KeyReleasedSudokuHandler(sudokuGameState, sudokuGameController);
        mouseReleasedHandler = new MouseReleasedSudokuHandler(sudokuGameState, sudokuGameController);
        mouseMovedHandler = new MouseMovedSudokuHandler(sudokuGameState, sudokuGameController);
    }

    public void onMouseClicked(MouseEvent mouseEvent) {
        this.getMouseClickedHandler().onMouseClicked(mouseEvent);
    }

    public void onKeyPressed(KeyEvent keyEvent) throws IOException {
        this.getKeyPressedHandler().onKeyPressed(keyEvent);
    }

    public void onKeyReleased(KeyEvent keyEvent) {
        this.getKeyReleasedHandler().onKeyReleased(keyEvent);
    }

    public void onMouseReleased(MouseEvent mouseEvent) {
        this.getMouseReleasedHandler().onMouseReleased(mouseEvent);
    }

    public void onMouseMoved(MouseEvent mouseEvent) {
        this.getMouseMovedHandler().onMouseMoved(mouseEvent);
    }

    public MouseClickedSudokuHandler getMouseClickedHandler() {
        return mouseClickedHandler;
    }

    public KeyPressedSudokuHandler getKeyPressedHandler() {
        return keyPressedHandler;
    }

    public KeyReleasedSudokuHandler getKeyReleasedHandler() {
        return keyReleasedHandler;
    }

    public MouseReleasedSudokuHandler getMouseReleasedHandler() {
        return mouseReleasedHandler;
    }

    public MouseMovedSudokuHandler getMouseMovedHandler() {
        return mouseMovedHandler;
    }
}

