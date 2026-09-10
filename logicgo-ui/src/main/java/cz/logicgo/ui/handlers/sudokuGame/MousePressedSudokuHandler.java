package cz.logicgo.ui.handlers.sudokuGame;

import cz.logicgo.engine.algorithms.sudoku.SudokuGame;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.SudokuGameController;
import javafx.scene.input.MouseEvent;

public class MousePressedSudokuHandler extends SudokuHandlerBase {

    public MousePressedSudokuHandler(SudokuGame sudokuGame, SudokuGameController sudokuGameController) {
        super(sudokuGame, sudokuGameController);
    }

    public void onMousePressed(MouseEvent mouseEvent) {

        double x = mouseEvent.getX();
        double y = mouseEvent.getY();
    }
}

