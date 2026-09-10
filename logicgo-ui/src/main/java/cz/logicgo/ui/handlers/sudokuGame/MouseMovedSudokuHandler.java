package cz.logicgo.ui.handlers.sudokuGame;

import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.engine.algorithms.sudoku.SudokuGame;
import cz.logicgo.engine.algorithms.sudoku.validators.ISudokuValidator;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.SudokuGameController;
import javafx.scene.input.MouseEvent;

import java.util.HashSet;
import java.util.Optional;

import static cz.logicgo.engine.algorithms.sudoku.helper.SudokuGridHelper.*;


public class MouseMovedSudokuHandler extends SudokuHandlerBase {

    public MouseMovedSudokuHandler(SudokuGame sudokuGame, SudokuGameController sudokuGameController) {
        super(sudokuGame, sudokuGameController);
    }

    public void onMouseMoved(MouseEvent mouseEvent) {
        var sgc = this.getSudokuGameController();
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();

        boolean hintChoice = sgc.isHintChoice();
        Sudoku sudoku = sgc.getSudokuGame().getSudoku();
        ISudokuValidator validator = sgc.getSudokuGame().getSudokuValidator();

        if (hintChoice) {
            Optional<SudokuCell> optionalCell = this.getCellAtPosition(x, y);

            if (optionalCell.isEmpty()) {
                sgc.setHoveredSudokuCells(null);
                return;
            }
            SudokuCell sudokuCell = optionalCell.get();

            if (sgc.getActiveHint() == null) return;

            SudokuCell[] result = switch (sgc.getActiveHint()) {
                case CHOSEN_CELL, CHECK_CELL, CHECK_VALIDITY -> new SudokuCell[]{sudokuCell};

                case CHECK_ROW -> getRowCells(sudoku, sudokuCell, validator).toArray(new SudokuCell[0]);

                case CHECK_COLUMN -> getColCells(sudoku, sudokuCell, validator).toArray(new SudokuCell[0]);

                case CHECK_REGION -> getRegionCells(sudoku, sudokuCell, validator).toArray(new SudokuCell[0]);

                case CHECK_AFFECTED_CELLS -> {
                    HashSet<SudokuCell> cells = new HashSet<>();
                    cells.addAll(getRowCells(sudoku, sudokuCell, validator));
                    cells.addAll(getColCells(sudoku, sudokuCell, validator));
                    cells.addAll(getRegionCells(sudoku, sudokuCell, validator));
                    cells.addAll(getModifierCells(sudoku, sudokuCell, sudoku.getModifiers().getActiveModifiers()));
                    yield cells.toArray(new SudokuCell[0]);
                }
                case CHECK_ONE_BOARD -> getBoardCells(sudoku, sudokuCell, validator).toArray(new SudokuCell[0]);
                case CHECK_MODIFIER_CELLS ->
                        getModifierCells(sudoku, sudokuCell, sudoku.getModifiers().getActiveModifiers()).toArray(new SudokuCell[0]);
                default -> new SudokuCell[0];
            };

            sgc.setHoveredSudokuCells(result);
        }
    }


}
