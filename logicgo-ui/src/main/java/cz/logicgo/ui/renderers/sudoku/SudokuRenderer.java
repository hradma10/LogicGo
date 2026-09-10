package cz.logicgo.ui.renderers.sudoku;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.SudokuUtils;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.engine.algorithms.sudoku.validators.MultiGridConfig;
import cz.logicgo.ui.controllers.screenControllers.RedrawCanvasFunctions;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant.OFFSET;
import static cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant.PATTERNED;
import static cz.logicgo.ui.utils.SudokuUiUtils.printSudokuOutline;


public class SudokuRenderer {

    public static void renderWrongHints(Canvas secondaryCanvas, Sudoku sudoku, List<SudokuCell> wrongCells) {
        if (secondaryCanvas == null || wrongCells.isEmpty()) return;

        GraphicsContext gc = secondaryCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, secondaryCanvas.getWidth(), secondaryCanvas.getHeight());

        int size = sudoku.getBoard().length;
        double cellWidth = secondaryCanvas.getWidth() / size;
        double cellHeight = secondaryCanvas.getHeight() / size;

        gc.setFill(new Color(0.9, 0.2, 0.2, 0.4));

        for (SudokuCell cell : wrongCells) {
            double x = cell.getCol() * cellWidth;
            double y = cell.getRow() * cellHeight;
            gc.fillRect(x, y, cellWidth, cellHeight);
        }
    }

    public static void renderFullBoard(Canvas canvas, Sudoku sudoku, boolean forPrint) {
        renderFullBoard(canvas, sudoku, forPrint, Collections.emptyList());
    }

    public static void renderFullBoard(Canvas canvas, Sudoku sudoku, boolean forPrint, List<SudokuCell> wrongCells) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        if (sudoku.getBoard() == null || sudoku.getBoard().length == 0) return;

        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        int size = sudoku.getBoard().length;
        double cellWidth = canvasWidth / size;
        double cellHeight = canvasHeight / size;
        SudokuCell[][] board = sudoku.getBoard();

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                double x = col * cellWidth;
                double y = row * cellHeight;

                gc.setFill(Color.WHITE);
                gc.fillRect(x, y, cellWidth, cellHeight);

                SudokuCell cell = board[row][col];
                if (cell != null) {
                    if (!forPrint && cell.isConflicting()) {
                        gc.setFill(Color.color(0.95, 0.3, 0.3, 0.08));
                        gc.fillRect(x, y, cellWidth, cellHeight);

                        drawHatchedCell(gc, x, y, cellWidth, cellHeight, Color.color(0.95, 0.3, 0.3, 0.45));
                    } else if (!Objects.equals(cell.getBackgroundColor(), Color.WHITE.toString())) {
                        Color bg = Color.web(cell.getBackgroundColor());
                        Color semiTransparent = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 0.4);
                        gc.setFill(semiTransparent);
                        gc.fillRect(x, y, cellWidth, cellHeight);
                    }

                    if (!forPrint && !cell.isDrawToPrimary() && !Objects.equals(cell.getBackgroundColor(), Color.WHITE.toString())) {
                        Color semiBlue = new Color(0, 0, 1, 0.15);
                        gc.setStroke(semiBlue);
                        gc.setFill(semiBlue);
                        gc.fillRect(x, y, cellWidth, cellHeight);
                    }
                }
            }
        }

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (board[row][col] != null) {
                    double x = col * cellWidth;
                    double y = row * cellHeight;
                    gc.setStroke(Color.LIGHTGRAY);
                    gc.setLineWidth(1.0);
                    gc.strokeRect(x, y, cellWidth, cellHeight);
                }
            }
        }

        if (!MultiGridConfig.isMultiDoku(sudoku.getVariant())) {
            if (sudoku.getRegionLayout() != null && sudoku.getRegionLayout().getRegions() != null) {
                RedrawCanvasFunctions.strokeBoard(sudoku.getRegionLayout().getRegions(), cellWidth, cellHeight, gc, sudoku.getVariant(), sudoku.getPattern(), false);
            }
            if (!forPrint) {
                printSudokuOutline(gc, cellWidth, cellHeight, size);
            }
        } else {
            double regionLineWidth = Math.max(4.0, cellWidth * 0.12);
            var setup = MultiGridConfig.LAYOUTS.get(sudoku.getVariant());
            int[][] offsets = setup.offsets();

            gc.setStroke(Color.BLACK);
            gc.setLineWidth(regionLineWidth);

            for (int[] offset : offsets) {
                int startR = offset[0];
                int startC = offset[1];

                for (int i = 3; i <= 6; i += 3) {
                    double y = (startR + i) * cellHeight;
                    gc.strokeLine(startC * cellWidth, y, (startC + 9) * cellWidth, y);

                    double x = (startC + i) * cellWidth;
                    gc.strokeLine(x, startR * cellHeight, x, (startR + 9) * cellHeight);
                }
                gc.strokeRect(startC * cellWidth, startR * cellHeight, 9 * cellWidth, 9 * cellHeight);
            }

            if (!forPrint) {
                gc.setLineWidth(Math.max(3.5, cellWidth * 0.09));
                for (int[] offset : offsets) {
                    gc.setStroke(Color.BLACK);
                    gc.strokeRect(offset[1] * cellWidth, offset[0] * cellHeight, 9 * cellWidth, 9 * cellHeight);
                }
            }
        }

        if (sudoku.getModifiers() != null) {
            ConstraintRenderer.drawModifiers(
                    canvas, sudoku.getModifiers(), sudoku, 0, 0, canvasWidth, canvasHeight, forPrint
            );
        }

        var variant = sudoku.getVariant();
        if (variant == PATTERNED || variant == OFFSET) {
            gc.setLineDashes(null);
            gc.setGlobalAlpha(1.0);

            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    SudokuCell cell = board[row][col];
                    if (cell == null) continue;

                    double x = col * cellWidth;
                    double y = row * cellHeight;

                    if (!forPrint && cell.isConflicting()) continue;

                    if (!Objects.equals(cell.getBackgroundColor(), Color.WHITE.toString()) && !cell.isDrawToPrimary()) {
                        gc.setFill(Color.web("#2a75d3", 0.25));
                        gc.fillRect(x, y, cellWidth, cellHeight);
                    } else if (!Objects.equals(cell.getBackgroundColor(), Color.WHITE.toString())) {
                        Color bg = Color.web(cell.getBackgroundColor());
                        gc.setFill(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 0.5));
                        gc.fillRect(x, y, cellWidth, cellHeight);
                    }
                }
            }

            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    if (board[row][col] != null) {
                        double x = col * cellWidth;
                        double y = row * cellHeight;
                        gc.setStroke(Color.LIGHTGRAY);
                        gc.setLineWidth(1.0);
                        gc.strokeRect(x, y, cellWidth, cellHeight);
                    }
                }
            }

            if (!MultiGridConfig.isMultiDoku(sudoku.getVariant())) {
                if (sudoku.getRegionLayout() != null && sudoku.getRegionLayout().getRegions() != null) {
                    RedrawCanvasFunctions.strokeBoard(sudoku.getRegionLayout().getRegions(), cellWidth, cellHeight, gc, sudoku.getVariant(), sudoku.getPattern(), false);
                }
                if (!forPrint) {
                    printSudokuOutline(gc, cellWidth, cellHeight, size);
                }
            }

            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    SudokuCell cell = board[row][col];

                    if (cell != null && cell.getBackgroundColor() != Color.WHITE.toString() && !cell.isDrawToPrimary()) {
                        double x = col * cellWidth;
                        double y = row * cellHeight;

                        gc.setStroke(Color.web("#2a75d3", 0.95));
                        gc.setLineWidth(Math.max(2.5, cellWidth * 0.07));
                        gc.strokeRect(x + 1.5, y + 1.5, cellWidth - 3, cellHeight - 3);
                    }
                }
            }
        }

        if (!forPrint && wrongCells != null && !wrongCells.isEmpty()) {
            gc.setFill(new Color(0.9, 0.2, 0.2, 0.4));
            for (SudokuCell cell : wrongCells) {
                if (cell != null) {
                    double x = cell.getCol() * cellWidth;
                    double y = cell.getRow() * cellHeight;
                    gc.fillRect(x, y, cellWidth, cellHeight);
                }
            }
        }

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                SudokuCell cell = board[row][col];
                if (cell != null) {
                    double x = col * cellWidth;
                    double y = row * cellHeight;

                    boolean isConflict = !forPrint && cell.isConflicting();
                    boolean compressCandidates = switch (variant) {
                        case KROPKI, XV, CONSECUTIVE, GREATER_THAN, BETWEEN -> true;
                        case KILLER, QUADRUPLES, GROUP_SUMS -> true;
                        default -> false;
                    };
                    boolean forceChangeColorCandidates = switch (variant) {
                        case PATTERNED, OFFSET -> true;
                        default -> false;
                    };
                    drawCellValuesOnly(gc, cell, x, y, cellWidth, cellHeight, isConflict, forPrint, compressCandidates, forceChangeColorCandidates, size);
                }
            }
        }
    }

    public static void drawPulsatingCell(Canvas canvas, double timeSeconds, int gridSize, List<SudokuCell> sudokuCells) {
        if (sudokuCells == null || sudokuCells.isEmpty()) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();

        double cellWidth = canvas.getWidth() / gridSize;
        double cellHeight = canvas.getHeight() / gridSize;

        double pulse = 0.5 + 0.5 * Math.sin(timeSeconds * 4);

        double baseOpacity = 0.1 + 0.15 * pulse;
        double borderOpacity = 0.4 + 0.5 * pulse;

        Color fillColor = Color.color(0.95, 0.3, 0.3, baseOpacity);
        Color strokeColor = Color.color(0.95, 0.3, 0.3, borderOpacity);

        for (SudokuCell c : sudokuCells) {
            if (c == null) continue;

            double offset = 1.5;
            double x = (c.getCol() * cellWidth) + offset;
            double y = (c.getRow() * cellHeight) + offset;
            double w = cellWidth - (offset * 2);
            double h = cellHeight - (offset * 2);

            gc.setFill(fillColor);
            gc.fillRect(x, y, w, h);

            gc.setStroke(strokeColor);
            gc.setLineWidth(2.5);
            gc.strokeRect(x, y, w, h);
        }
    }

    private static void drawHatchedCell(GraphicsContext gc, double x, double y, double w, double h, Color color) {
        gc.save();

        gc.beginPath();
        gc.rect(x, y, w, h);
        gc.clip();

        gc.setStroke(color);
        gc.setLineWidth(1.5);

        double spacing = 6.0;

        for (double offset = -h; offset < w; offset += spacing) {
            gc.strokeLine(x + offset, y, x + offset + h, y + h);
        }

        gc.restore();
    }

    private static void drawCellValuesOnly(GraphicsContext gc, SudokuCell cell, double x, double y, double width, double height, boolean isConflict, boolean forPrint, boolean compressCandidates, boolean forceChangeColorCandidates, int sudokuSize) {
        if (cell.getValue() != 0) {
            gc.setFont(new Font("Arial", Math.min(width, height) * 0.55));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);

            if (forPrint) {
                gc.setFill(Color.BLACK);
            } else if (isConflict) {
                gc.setFill(Color.web("#d32f2f"));
            } else if (cell.isChangeable()) {
                gc.setFill(Color.web("#1A5F7A"));
            } else {
                gc.setFill(Color.BLACK);
            }

            gc.fillText(SudokuUtils.VALUE_TO_STRING.get(cell.getValue()), x + width / 2.0, y + height / 2.0);
            return;
        }

        if (cell.isShowCandidates() && cell.getCandidates() != null && !cell.getCandidates().isEmpty()) {

            int subCols = 3;
            int subRows = 3;

            if (sudokuSize <= 4) {
                subCols = 2;
                subRows = 2;
            } else if (sudokuSize <= 6) {
                subCols = 3;
                subRows = 2;
            } else if (sudokuSize <= 9) {
                subCols = 3;
                subRows = 3;
            } else if (sudokuSize <= 12) {
                subCols = 4;
                subRows = 3;
            }

            double paddingX = 0;
            double paddingY = 0;

            if (compressCandidates) {
                paddingX = width * 0.22;
                paddingY = height * 0.22;
            }

            double usableWidth = width - (2 * paddingX);
            double usableHeight = height - (2 * paddingY);

            double subCellWidth = usableWidth / subCols;
            double subCellHeight = usableHeight / subRows;

            double fontSizeMultiplier = compressCandidates ? 0.82 : 0.75;
            double fontSize = Math.min(subCellWidth, subCellHeight) * fontSizeMultiplier;

            gc.setFont(new Font("Arial", fontSize));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);

            boolean hasDarkBackground = cell.getBackgroundColor() != null && cell.getBackgroundColor() != Color.WHITE.toString();
            boolean hasOverlapDanger = compressCandidates || hasDarkBackground || forceChangeColorCandidates;

            Color candidateColor;
            if (forPrint) {
                candidateColor = hasOverlapDanger ? Color.BLACK : Color.DARKGRAY;
            } else {
                if (forceChangeColorCandidates) {
                    candidateColor = Color.WHITE;
                } else if (hasDarkBackground) {
                    candidateColor = Color.WHITE;
                } else if (compressCandidates) {
                    candidateColor = Color.web("#111111");
                } else {
                    candidateColor = Color.GRAY;
                }
            }

            for (int val : cell.getCandidates()) {
                if (val == 0) continue;
                int idx = val - 1;
                int subCol = idx % subCols;
                int subRow = idx / subCols;

                if (subRow >= subRows) continue;

                double candidateX = x + paddingX + (subCol * subCellWidth) + (subCellWidth / 2.0);
                double candidateY = y + paddingY + (subRow * subCellHeight) + (subCellHeight / 2.0);

                String candidateStr = SudokuUtils.VALUE_TO_STRING.get(val);
                if (candidateStr == null) {
                    if (val == 10) candidateStr = "A";
                    else if (val == 11) candidateStr = "B";
                    else if (val == 12) candidateStr = "C";
                    else candidateStr = String.valueOf(val);
                }

                if (!forPrint && hasOverlapDanger) {
                    boolean useDarkShadow = forceChangeColorCandidates || hasDarkBackground;
                    gc.setFill(useDarkShadow ? Color.color(0, 0, 0, 0.65) : Color.color(1, 1, 1, 0.9));

                    gc.fillText(candidateStr, candidateX - 1, candidateY);
                    gc.fillText(candidateStr, candidateX + 1, candidateY);
                    gc.fillText(candidateStr, candidateX, candidateY - 1);
                    gc.fillText(candidateStr, candidateX, candidateY + 1);
                }

                gc.setFill(candidateColor);
                gc.fillText(candidateStr, candidateX, candidateY);
            }
        }

    }
}
