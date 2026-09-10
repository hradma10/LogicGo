package cz.logicgo.ui.utils;

import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.SudokuUtils;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuRegionLayout;
import cz.logicgo.core.misc.GridCell;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.core.misc.enums.settings.modes.HighlightMode;
import cz.logicgo.engine.algorithms.sudoku.SudokuGame;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.DiagonalType;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.SudokuGameController;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.stream.IntStream;

import static javafx.scene.paint.Color.CORNFLOWERBLUE;
import static javafx.scene.paint.Color.WHITE;

public final class SudokuUiUtils {

    public static final Map<KeyCode, Integer> KEYS_TO_NUMBER = new HashMap<>();
    private static final Color COLOR_BACKGROUND_RELATED = CORNFLOWERBLUE;

    public static final Color[] PATTERN_PALETTE = new Color[]{
            Color.rgb(215, 150, 220, 1.0),
            Color.rgb(120, 210, 120, 1.0),
            Color.rgb(110, 170, 245, 1.0),
            Color.rgb(255, 210, 80, 1.0),
            Color.rgb(180, 130, 255, 1.0),
            Color.rgb(80, 220, 220, 1.0),
            Color.rgb(255, 160, 90, 1.0),
            Color.rgb(255, 120, 180, 1.0),
            Color.rgb(240, 90, 90, 1.0),
            Color.rgb(90, 220, 160, 1.0),
            Color.rgb(80, 180, 240, 1.0),
            Color.rgb(255, 190, 130, 1.0),
            Color.rgb(150, 150, 255, 1.0),
            Color.rgb(180, 230, 100, 1.0),
            Color.rgb(210, 140, 140, 1.0),
            Color.rgb(100, 200, 200, 1.0),
            Color.rgb(230, 200, 60, 1.0),
            Color.rgb(120, 140, 230, 1.0),
            Color.rgb(160, 200, 110, 1.0),
            Color.rgb(230, 150, 100, 1.0),
            Color.rgb(200, 110, 200, 1.0)
    };

    static {
        IntStream.rangeClosed(0, 9).forEach(i -> {
            KEYS_TO_NUMBER.put(KeyCode.valueOf("NUMPAD" + i), i);
            KEYS_TO_NUMBER.put(KeyCode.valueOf("DIGIT" + i), i);
        });
        char c = 'A';
        int i = 10;
        while (c <= 'F') {
            KEYS_TO_NUMBER.put(KeyCode.getKeyCode(String.valueOf(c)), i);
            c++;
            i++;
        }
        KEYS_TO_NUMBER.put(KeyCode.DELETE, 0);
        KEYS_TO_NUMBER.put(KeyCode.BACK_SPACE, 0);
    }

    private SudokuUiUtils() {}

    public static void printSudokuOutline(GraphicsContext gc, double width, double height, int size) {
        double totalWidth = size * width;
        double totalHeight = size * height;

        gc.setLineWidth(3);
        gc.setStroke(Color.BLACK);
        gc.strokeLine(0, 0, totalWidth, 0);
        gc.strokeLine(0, totalHeight, totalWidth, totalHeight);
        gc.strokeLine(0, 0, 0, totalHeight);
        gc.strokeLine(totalWidth, 0, totalWidth, totalHeight);
    }

    public static void clearCellBackgrounds(Sudoku sudoku) {
        SudokuCell[] board = SudokuUtils.flattenBoard(sudoku.getBoard());
        Arrays.stream(board).filter(Objects::nonNull).forEach(cell -> cell.setBackgroundColor(WHITE.toString()));
    }

    public static void changeOnlyNumber(Sudoku sudoku, SudokuCell selectedCell) {
        if (selectedCell == null) return;
        clearCellBackgrounds(sudoku);
        selectedCell.setBackgroundColor(COLOR_BACKGROUND_RELATED.toString());
    }

    public static void changeColorSameNumber(Sudoku sudoku, SudokuCell selectedCell) {
        if (selectedCell == null) return;
        int val = selectedCell.getValue();
        SudokuCell[][] board = sudoku.getBoard();

        clearCellBackgrounds(sudoku);
        if (val == 0) {
            selectedCell.setBackgroundColor(COLOR_BACKGROUND_RELATED.toString());
            return;
        }

        for (SudokuCell[] sudokuCells : board) {
            for (SudokuCell cell : sudokuCells) {
                if (cell != null && val == cell.getValue()) {
                    cell.setBackgroundColor(COLOR_BACKGROUND_RELATED.toString());
                }
            }
        }
    }

    public static void changeColorRelatedCells(Sudoku sudoku, SudokuCell selectedCell, HighlightMode highlightMode) {
        if (selectedCell == null) return;
        switch (highlightMode) {
            case REGIONS -> changeColorCellsPosition(sudoku, selectedCell);
            case SAME_NUMBERS -> changeColorSameNumber(sudoku, selectedCell);
            case NONE -> changeOnlyNumber(sudoku, selectedCell);
        }
    }

    private static void changeColorCellsPosition(Sudoku sudoku, SudokuCell selectedCell) {
        if (selectedCell == null) return;
        int setRow = selectedCell.getRow();
        int setCol = selectedCell.getCol();

        SudokuCell[][] board = sudoku.getBoard();
        SudokuRegionLayout regionLayout = sudoku.getRegionLayout();

        clearCellBackgrounds(sudoku);
        for (int col = 0; col < sudoku.getType().getGridSize(); col++) {
            if (board[setRow][col] != null) {
                board[setRow][col].setBackgroundColor(COLOR_BACKGROUND_RELATED.toString());
            }
        }
        for (int row = 0; row < sudoku.getType().getGridSize(); row++) {
            if (board[row][setCol] != null) {
                board[row][setCol].setBackgroundColor(COLOR_BACKGROUND_RELATED.toString());
            }
        }

        if (regionLayout != null && regionLayout.getRegions() != null) {
            int index = regionLayout.getRegions()[setRow][setCol];
            ArrayList<GridCell> sameBoxCells = regionLayout.getCellsBelongToSubgrid(index);
            for (GridCell cellCoords : sameBoxCells) {
                if (board[cellCoords.row()][cellCoords.col()] != null) {
                    board[cellCoords.row()][cellCoords.col()].setBackgroundColor(COLOR_BACKGROUND_RELATED.toString());
                }
            }
        }

        int gridSize = sudoku.getType().getGridSize();
        SudokuVariant sudokuVariant = sudoku.getVariant();
        if (sudokuVariant == null) return;

        switch (sudokuVariant) {
            case DIAGONAL -> {
                if (SudokuUtils.isDiagonal(setRow, setCol, gridSize)) {
                    DiagonalType diagonalType = SudokuUtils.getDiagonalType(setRow, setCol, gridSize);
                    List<GridCell> diagonalCells = SudokuUtils.getDiagonalCells(gridSize, diagonalType);
                    for (GridCell diagonalCell : diagonalCells) {
                        SudokuCell cell = board[diagonalCell.row()][diagonalCell.col()];
                        if (cell != null) cell.setBackgroundColor(COLOR_BACKGROUND_RELATED.toString());
                    }
                }
            }
            case OFFSET -> {
                GridCell firstGridCell = SudokuUtils.getFirstOffsetGridCell(setRow, setCol);
                List<GridCell> offSetCells = SudokuUtils.getOffsetCells(gridSize, firstGridCell.row(), firstGridCell.col());
                for (GridCell diagonalCell : offSetCells) {
                    SudokuCell cell = board[diagonalCell.row()][diagonalCell.col()];
                    if (cell != null) cell.setBackgroundColor(COLOR_BACKGROUND_RELATED.toString());
                }
            }
            case PATTERNED -> {
                SudokuPatternLayout patternLayout = sudoku.getPattern();
                if (patternLayout != null) {
                    ArrayList<GridCell> cells = patternLayout.getCellsBelongToSubgrid(1);
                    if (cells.contains(new GridCell(setRow, setCol))) {
                        for (GridCell patternCell : cells) {
                            SudokuCell cell = board[patternCell.row()][patternCell.col()];
                            if (cell != null) cell.setBackgroundColor(COLOR_BACKGROUND_RELATED.toString());
                        }
                    }
                }
            }
            default -> {}
        }
    }

    public static void markWrong(SudokuGame sudokuGame, List<SudokuCell> cells, SudokuGameController con) {
        SudokuCell[][] solutionBoard = sudokuGame.getSudoku().getSolutionBoard();
        List<SudokuCell> wrongCells = new ArrayList<>();

        for (SudokuCell cell : cells) {
            if (cell == null || cell.getValue() == 0) continue;
            if (cell.getValue() != solutionBoard[cell.getRow()][cell.getCol()].getValue()) {
                wrongCells.add(cell);
            }
        }

        con.getWrongHintCells().clear();
        con.getWrongHintCells().addAll(wrongCells);
        con.setHintChoice(false);
        con.setActiveHint(null);
        con.setHoveredSudokuCells((SudokuCell[]) null);
        con.startHintFeedbackTimer(2.5, con::clearHintVisuals);
    }
}
