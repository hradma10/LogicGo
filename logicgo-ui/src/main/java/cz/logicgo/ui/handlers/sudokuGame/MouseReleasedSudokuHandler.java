package cz.logicgo.ui.handlers.sudokuGame;

import cz.logicgo.engine.algorithms.sudoku.SudokuGame;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.SudokuGameController;
import javafx.scene.input.MouseEvent;

public class MouseReleasedSudokuHandler extends SudokuHandlerBase {

    public MouseReleasedSudokuHandler(SudokuGame sudokuGame, SudokuGameController sudokuGameController) {
        super(sudokuGame, sudokuGameController);
    }

    public void onMouseReleased(MouseEvent mouseEvent) {

    }
}
