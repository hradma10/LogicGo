package cz.logicgo.ui.controllers.screenControllers;

import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.ui.renderers.sudoku.SudokuRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Arrays;


public class RedrawCanvasFunctions {

    public static void clearCanvas(Canvas canvas) {
        canvas.getGraphicsContext2D().save();
        canvas.getGraphicsContext2D().setFill(Color.WHITE);
        canvas.getGraphicsContext2D().fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.getGraphicsContext2D().restore();
    }

    public static void clearCanvasTransparent(Canvas canvas) {
        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public static void drawSudokuThumbnail(Canvas previewCanvas, Sudoku sudoku) {
        if (sudoku == null || previewCanvas == null || sudoku.getBoard() == null) return;

        clearCanvas(previewCanvas);

        SudokuCell firstValidCell = null;
        findCellLoop:
        for (SudokuCell[] row : sudoku.getBoard()) {
            for (SudokuCell cell : row) {
                if (cell != null) {
                    firstValidCell = cell;
                    break findCellLoop;
                }
            }
        }

        if (firstValidCell == null) return;

        boolean originalPrintState = firstValidCell.isForPrint();

        setPrintState(sudoku, true);

        SudokuRenderer.renderFullBoard(previewCanvas, sudoku, true);

        setPrintState(sudoku, originalPrintState);
    }

    private static void setPrintState(Sudoku sudoku, boolean state) {
        for (SudokuCell[] row : sudoku.getBoard()) {
            for (SudokuCell cell : row) {
                if (cell == null) continue;
                cell.setForPrint(state);
            }
        }
    }

    public static void redrawCanvas(Canvas canvas, Sudoku sudoku) {
        clearCanvas(canvas);
        SudokuRenderer.renderFullBoard(canvas, sudoku, false);
    }

    public static Integer[][] toIntegerArray(int[][] input) {
        return Arrays.stream(input)
                .map(row -> Arrays.stream(row).boxed().toArray(Integer[]::new))
                .toArray(Integer[][]::new);
    }

    public static void strokeBoard(int[][] regions, double cellWidth, double cellHeight, GraphicsContext gc, SudokuVariant sudokuVariant, SudokuPatternLayout layout, boolean preview) {
        strokeBoard(toIntegerArray(regions), cellWidth, cellHeight, gc, sudokuVariant, layout, preview);
    }

    public static void strokeBoard(Integer[][] regions, double cellWidth, double cellHeight, GraphicsContext gc, SudokuVariant sudokuVariant, SudokuPatternLayout layout, boolean preview) {
        if (regions == null || regions.length == 0) return;

        int size = regions.length;
        if (cellHeight <= 0) cellHeight = 50;
        if (cellWidth <= 0) cellWidth = 50;

        double normalLineWidth = cellWidth * 0.02;
        double regionLineWidth = cellWidth * 0.06;
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(normalLineWidth);
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (isPlayableRegion(regions, row, col)) {
                    double x = col * cellWidth;
                    double y = row * cellHeight;
                    gc.strokeRect(x, y, cellWidth, cellHeight);
                }
            }
        }
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(regionLineWidth);
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (!isPlayableRegion(regions, row, col)) continue;

                Integer currentRegion = regions[row][col];
                double x = col * cellWidth;
                double y = row * cellHeight;
                double rightX = x + cellWidth;
                double bottomY = y + cellHeight;
                if (!isPlayableRegion(regions, row - 1, col) || !currentRegion.equals(regions[row - 1][col])) {
                    gc.strokeLine(x, y, rightX, y);
                }
                if (!isPlayableRegion(regions, row + 1, col) || !currentRegion.equals(regions[row + 1][col])) {
                    gc.strokeLine(x, bottomY, rightX, bottomY);
                }
                if (!isPlayableRegion(regions, row, col - 1) || !currentRegion.equals(regions[row][col - 1])) {
                    gc.strokeLine(x, y, x, bottomY);
                }
                if (!isPlayableRegion(regions, row, col + 1) || !currentRegion.equals(regions[row][col + 1])) {
                    gc.strokeLine(rightX, y, rightX, bottomY);
                }
            }
        }
    }


    private static boolean isPlayableRegion(Integer[][] regions, int r, int c) {
        if (r < 0 || r >= regions.length || c < 0 || c >= regions[0].length) {
            return false;
        }
        return regions[r][c] != null && regions[r][c] >= 0;
    }
}
