package cz.logicgo.ui.handlers.sudokuGame;

import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.core.misc.enums.settings.modes.HighlightMode;
import cz.logicgo.engine.algorithms.sudoku.SudokuGame;
import cz.logicgo.engine.algorithms.sudoku.validators.ISudokuValidator;
import cz.logicgo.ui.commands.sudokuCommands.SetSudokuNumberCommand;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.SudokuGameController;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static cz.logicgo.engine.algorithms.sudoku.helper.SudokuGridHelper.*;
import static cz.logicgo.ui.utils.SudokuUiUtils.*;


public class MouseClickedSudokuHandler extends SudokuHandlerBase {

    public MouseClickedSudokuHandler(SudokuGame sudokuGameState, SudokuGameController sudokuGameController) {
        super(sudokuGameState, sudokuGameController);
    }


    private static void highlightIfIncorrect(SudokuCell[][] solutionBoard, SudokuCell cell) {
        int row = cell.getRow();
        int col = cell.getCol();

        if (cell.isChangeable() && cell.getValue() != solutionBoard[row][col].getValue()) {
            cell.setBackgroundColor(cell.getValue() == 0 ? Color.CYAN.toString() : Color.RED.toString());
        }
    }

    public void onMouseClicked(MouseEvent mouseEvent) {
        var con = getSudokuGameController();
        con.stopHintFeedbackTimer();

        switch (mouseEvent.getButton()) {
            case PRIMARY -> {
                if (mouseEvent.getClickCount() == 1) {
                    onSingleClickPrimary(mouseEvent);
                }
            }
            case SECONDARY -> {
                if (mouseEvent.getClickCount() == 1) {
                    onSingleClickSecondary(mouseEvent);
                }
            }
            case null, default -> {
            }
        }
        redrawCanvases(true, true);
    }

    private void onSingleClickSecondary(MouseEvent mouseEvent) {
        SudokuGameController con = this.getSudokuGameController();
        boolean activeHint = con.isHintChoice();
        if (activeHint) {
            con.setHintChoice(false);
            con.setActiveHint(null);
            con.setHoveredSudokuCells(null);
        } else {
            clearCellBackgrounds(con.getSudokuGame().getSudoku());
        }
    }

    private void onSingleClickPrimary(MouseEvent mouseEvent) {
        SudokuGameController con = this.getSudokuGameController();

        con.stopHintFeedbackTimer();

        double x = mouseEvent.getX();
        double y = mouseEvent.getY();

        Optional<SudokuCell> newSelectedCellOptional = getCellAtPosition(x, y);

        if (con.isHintChoice() && newSelectedCellOptional.isPresent()) {
            SudokuCell sudokuCell = newSelectedCellOptional.get();
            handleHint(sudokuCell);

            con.redrawActive();
            return;
        }

        if (newSelectedCellOptional.isPresent()) {
            SudokuCell newSelectedCell = newSelectedCellOptional.get();
            this.selectCellOnCanvas(newSelectedCell);
            con.setSelectedSudokuCell(newSelectedCell);
            HighlightMode highlightMode = con.getHighlightMode();
            changeColorRelatedCells(con.getSudokuGame().getSudoku(), newSelectedCell, highlightMode);
        } else {
            con.setSelectedSudokuCell(null);

            if (con.getSudokuGame() != null && con.getSudokuGame().getSudoku() != null) {
                for (SudokuCell[] row : con.getSudokuGame().getSudoku().getBoard()) {
                    for (SudokuCell cell : row) {
                        if (cell != null) {
                            cell.setBackgroundColor(Color.WHITE.toString());
                            cell.setDrawToPrimary(true);
                        }
                    }
                }
            }

            clearCellBackgrounds(con.getSudokuGame().getSudoku());
        }
    }

    private void handleHint(SudokuCell selectedCell) {
        SudokuGameController con = getSudokuGameController();
        SudokuGame sudokuGame = con.getSudokuGame();
        Sudoku sudoku = sudokuGame.getSudoku();
        SudokuCell[][] solutionBoard = sudoku.getSolutionBoard();
        ISudokuValidator sudokuValidator = sudokuGame.getSudokuValidator();

        clearCellBackgrounds(sudokuGame.getSudoku());
        if (con.getActiveHint() == null) return;

        int row = selectedCell.getRow();
        int col = selectedCell.getCol();

        switch (con.getActiveHint()) {
            case CHOSEN_CELL -> {
                if (selectedCell.getValue() != 0) break;
                int value = solutionBoard[row][col].getValue();
                con.getCommandExecutor().execute(new SetSudokuNumberCommand(sudokuGame, row, col, value, 0));
                con.getTabState().setChangePending();
            }
            case CHECK_CELL -> highlightIfIncorrect(solutionBoard, selectedCell);
            case CHECK_REGION -> {
                List<SudokuCell> cells = getRegionCells(sudoku, selectedCell, sudokuValidator).stream().toList();
                markWrong(sudokuGame, cells, con);
            }
            case CHECK_ROW -> {
                List<SudokuCell> cells = getRowCells(sudoku, selectedCell, sudokuValidator).stream().toList();
                markWrong(sudokuGame, cells, con);
            }
            case CHECK_COLUMN -> {
                List<SudokuCell> cells = getColCells(sudoku, selectedCell, sudokuValidator).stream().toList();
                markWrong(sudokuGame, cells, con);
            }
            case CHECK_ONE_BOARD -> {
                List<SudokuCell> cells = getBoardCells(sudoku, selectedCell, sudokuValidator).stream().toList();
                markWrong(sudokuGame, cells, con);
            }
            case CHECK_MODIFIER_CELLS -> {
                List<SudokuCell> cells = new ArrayList<>();
                switch (selectedCell.getVariant()) {
                    case PATTERNED, OFFSET -> {
                        var pattern = sudoku.getPattern();
                        int index = pattern.getGrid()[row][col];
                        if (index > 1 || (index == 0 && !pattern.isSelective())) {
                            var cellsPattern = pattern.getCellsBelongToSubgrid(index);
                            for (var cell : cellsPattern) {
                                int cellRow = cell.row();
                                int cellCol = cell.col();
                                SudokuCell cellGrid = sudoku.getSudokuCell(cellRow, cellCol);
                                cells.add(cellGrid);
                            }
                        }
                    }
                    default -> {
                        List<SudokuCell> list = getModifierCells(sudoku, selectedCell, sudoku.getModifiers().getActiveModifiers()).stream().toList();
                        cells.addAll(list);
                    }
                }

                cells = new ArrayList<>(new HashSet<>(cells));

                markWrong(sudokuGame, cells, con);
            }
            case CHECK_AFFECTED_CELLS -> {
                HashSet<SudokuCell> affectedSet = new HashSet<>();
                affectedSet.addAll(getColCells(sudoku, selectedCell, sudokuValidator));
                affectedSet.addAll(getRowCells(sudoku, selectedCell, sudokuValidator));
                affectedSet.addAll(getRegionCells(sudoku, selectedCell, sudokuValidator));
                affectedSet.addAll(getModifierCells(sudoku, selectedCell, sudoku.getModifiers().getActiveModifiers()));
                switch (selectedCell.getVariant()) {
                    case PATTERNED, OFFSET -> {
                        var pattern = sudoku.getPattern();
                        int index = pattern.getGrid()[row][col];
                        if (index > 1 || (index == 0 && !pattern.isSelective())) {
                            var cells = pattern.getCellsBelongToSubgrid(index);
                            for (var cell : cells) {
                                int cellRow = cell.row();
                                int cellCol = cell.col();
                                SudokuCell cellGrid = sudoku.getSudokuCell(cellRow, cellCol);
                                affectedSet.add(cellGrid);
                            }
                        }
                    }

                }
                markWrong(sudokuGame, new ArrayList<>(affectedSet), con);
            }

            default -> {
            }
        }

        con.setHintChoice(true);
        con.setActiveHint(null);
        con.setHoveredSudokuCells((SudokuCell[]) null);

        con.startHintFeedbackTimer(2.5, con::clearHintVisuals);
    }


}
