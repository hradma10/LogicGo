package cz.logicgo.ui.renderers.sudoku;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifierRecords.*;
import cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifiers;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;
import cz.logicgo.core.misc.GridCell;
import cz.logicgo.core.misc.Pair;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.CompType;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.ParityType;
import cz.logicgo.engine.algorithms.sudoku.custom.CustomLayoutsLoader;
import cz.logicgo.engine.algorithms.sudoku.custom.variant.gen.OffsetGen;
import cz.logicgo.ui.utils.GameUiUtils;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.List;

public class ConstraintRenderer {

    public static void drawModifiers(Canvas canvas, SudokuModifiers mods, Sudoku sudoku,
                                     double offsetX, double offsetY, double gridW, double gridH, boolean forPrint) {
        if (mods == null || sudoku == null) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        int size = sudoku.getType().getGridSize();

        double cellWidth = gridW / size;
        double cellHeight = gridH / size;

        double borderLineWidth = cellWidth * 0.04;
        gc.setStroke(Color.BLACK);

        switch (sudoku.getVariant()) {
            case DIAGONAL -> {
                gc.setStroke(Color.DARKGRAY);
                gc.setLineWidth(borderLineWidth);

                gc.strokeLine(offsetX, offsetY, offsetX + gridW, offsetY + gridH);
                gc.strokeLine(offsetX + gridW, offsetY, offsetX, offsetY + gridH);
            }
            case PATTERNED -> {
                List<Color> colors = getColorTheme(sudoku);

                SudokuPatternLayout pattern = sudoku.getPattern();

                drawPatternLayout(gc, pattern, cellWidth, cellHeight, offsetX, offsetY, colors);
            }
            case OFFSET -> {
                List<Color> colors = getColorTheme(sudoku);

                var pattern = CustomLayoutsLoader.getPatternLayoutByNameAndSize(OffsetGen.createName(), size);

                drawPatternLayout(gc, pattern, cellWidth, cellHeight, offsetX, offsetY, colors);
            }
            default -> {
            }
        }

        if (mods.hasBetween()) drawBetweenLines(gc, mods.getBetween(), cellWidth, cellHeight, offsetX, offsetY);
        if (mods.hasVudoku()) drawVudokuMarks(gc, mods.getVudoku(), cellWidth, cellHeight, offsetX, offsetY);
        if (mods.hasEvenOdd()) drawEvenOddMarks(gc, mods.getEvenOdd(), cellWidth, cellHeight, offsetX, offsetY);
        if (mods.hasGreaterThan())
            drawGreaterThanConstraints(gc, mods.getGreaterThan(), cellWidth, cellHeight, size, offsetX, offsetY);
        if (mods.hasXv()) drawXVMarks(gc, mods.getXv(), cellWidth, cellHeight, offsetX, offsetY);
        if (mods.hasConsecutive())
            drawConsecutivePairs(gc, mods.getConsecutive(), cellWidth, cellHeight, offsetX, offsetY);
        if (mods.hasKropki())
            drawKropkiPairs(gc, mods.getKropki(), cellWidth, cellHeight, offsetX, offsetY);
        if (mods.hasKiller()) {
            drawKillerCages(gc, mods.getKiller(), cellWidth, cellHeight);
        }
        if (mods.hasGroupSums()) {
            drawGroupSums(gc, mods.getGroupSums(), cellWidth, cellHeight);
        }
        if (mods.hasQuadruples()) {
            drawQuadruples(gc, mods.getQuadruples(), cellWidth, cellHeight);
        }
    }

    private static List<Color> getColorTheme(Sudoku sudoku) {
        List<Color> colors = null;

        if (sudoku != null && sudoku.getPlayer() != null && sudoku.getPlayer().getUserSettings() != null) {
            colors = sudoku.getPlayer().getUserColors().stream().map(Color::web).toList();
        }

        if (colors == null || colors.isEmpty()) {
            colors = (List<Color>) GameUiUtils.getDefaultColorTheme();
        }

        return colors;
    }


    public static void drawPatternLayout(GraphicsContext gc, SudokuPatternLayout pattern, double cellWidth, double cellHeight, double offsetX, double offsetY, List<Color> colors) {
        if (pattern == null || colors == null || colors.isEmpty()) return;
        int max = pattern.getIndexCount();
        boolean selective = pattern.isSelective();

        int startIdx = selective ? 1 : 0;

        for (int i = startIdx; i <= max; i++) {
            int colorIdx = selective ? (i - 1) : i;

            Color fillColor = colors.get(colorIdx % colors.size());
            gc.setFill(fillColor);

            var cells = pattern.getCellsBelongToSubgrid(i);
            if (cells == null) continue;

            for (GridCell cell : cells) {
                double cx = offsetX + (cell.col() * cellWidth);
                double cy = offsetY + (cell.row() * cellHeight);
                gc.fillRect(cx, cy, cellWidth, cellHeight);
            }
        }
    }

    public static void drawEditorPattern(GraphicsContext gc, int[][] layout, double cellWidth, double cellHeight, List<Color> colors) {
        if (layout == null) return;
        int size = layout.length;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int regionId = layout[r][c];
                if (regionId > 0) {
                    Color color = colors.get((regionId - 1) % colors.size());
                    gc.setFill(color);
                    gc.fillRect(c * cellWidth, r * cellHeight, cellWidth, cellHeight);
                }
            }
        }
    }

    public static void drawGroupSums(GraphicsContext gc, GroupSumsModifier constraint, double cellWidth, double cellHeight) {
        if (constraint == null || constraint.marks() == null) return;

        double radius = cellWidth * 0.35;

        for (GroupSumMark mark : constraint.marks()) {
            double centerX = (mark.topLeft().col() + 1) * cellWidth;
            double centerY = (mark.topLeft().row() + 1) * cellHeight;
            gc.setFill(Color.WHITE);
            gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
            gc.setStroke(Color.rgb(30, 30, 30));
            gc.setLineWidth(2.0);
            gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, radius * 0.85));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);

            String text = String.valueOf(mark.targetSum());
            gc.fillText(text, centerX, centerY);
        }
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.BASELINE);
    }

    public static void drawQuadruples(GraphicsContext gc, QuadruplesModifier constraint, double cellWidth, double cellHeight) {
        if (constraint == null || constraint.marks() == null) return;

        double radius = cellWidth * 0.38;

        for (QuadrupleMark mark : constraint.marks()) {
            double centerX = (mark.topLeft().col() + 1) * cellWidth;
            double centerY = (mark.topLeft().row() + 1) * cellHeight;

            gc.setFill(Color.WHITE);
            gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
            gc.setStroke(Color.rgb(30, 30, 30));
            gc.setLineWidth(2.0);
            gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, radius * 0.55));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);

            var vals = mark.values();

            if (vals.size() == 4) {
                gc.fillText(formatVal(vals.get(0)) + "  " + formatVal(vals.get(1)), centerX, centerY - radius * 0.3);
                gc.fillText(formatVal(vals.get(2)) + "  " + formatVal(vals.get(3)), centerX, centerY + radius * 0.3);
            } else if (vals.size() == 3) {
                gc.fillText(formatVal(vals.get(0)) + "  " + formatVal(vals.get(1)), centerX, centerY - radius * 0.25);
                gc.fillText(formatVal(vals.get(2)), centerX, centerY + radius * 0.3);
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < vals.size(); i++) {
                    sb.append(formatVal(vals.get(i)));
                    if (i < vals.size() - 1) sb.append("  ");
                }
                gc.fillText(sb.toString(), centerX, centerY);
            }
        }

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.BASELINE);
    }

    private static String formatVal(Integer val) {
        if (val == null) return "";
        return val == 10 ? "A" : String.valueOf(val);
    }

    public static void drawKillerCages(GraphicsContext gc, KillerModifier modifier, double cellWidth, double cellHeight) {
        if (modifier == null || modifier.cages() == null) return;


        double lineWidth = cellWidth * 0.025;
        gc.setLineWidth(lineWidth);

        gc.setStroke(Color.rgb(40, 40, 40));

        gc.setLineDashes(cellWidth * 0.12, cellWidth * 0.08);

        for (KillerCage cage : modifier.cages()) {
            GridCell topLeftCell = null;

            double offset = cellWidth * 0.08;

            for (GridCell cell : cage.cells()) {
                double x = cell.col() * cellWidth;
                double y = cell.row() * cellHeight;

                if (topLeftCell == null ||
                        (cell.row() < topLeftCell.row()) ||
                        (cell.row() == topLeftCell.row() && cell.col() < topLeftCell.col())) {
                    topLeftCell = cell;
                }

                if (!cage.cells().contains(new GridCell(cell.row() - 1, cell.col()))) {
                    gc.strokeLine(x + offset, y + offset, x + cellWidth - offset, y + offset);
                }
                if (!cage.cells().contains(new GridCell(cell.row() + 1, cell.col()))) {
                    gc.strokeLine(x + offset, y + cellHeight - offset, x + cellWidth - offset, y + cellHeight - offset);
                }
                if (!cage.cells().contains(new GridCell(cell.row(), cell.col() - 1))) {
                    gc.strokeLine(x + offset, y + offset, x + offset, y + cellHeight - offset);
                }
                if (!cage.cells().contains(new GridCell(cell.row(), cell.col() + 1))) {
                    gc.strokeLine(x + cellWidth - offset, y + offset, x + cellWidth - offset, y + cellHeight - offset);
                }
            }

            if (topLeftCell != null) {
                String sumText = String.valueOf(cage.targetSum());

                double fontSize = cellWidth * 0.22;
                double textX = topLeftCell.col() * cellWidth + offset + (lineWidth / 2);
                double textY = topLeftCell.row() * cellHeight + offset + (lineWidth / 2);

                gc.setFill(Color.BLACK);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, fontSize));
                gc.setTextAlign(TextAlignment.LEFT);
                gc.setTextBaseline(VPos.TOP);
                gc.fillText(sumText, textX, textY);
            }
        }

        gc.setLineDashes(null);
        gc.setTextBaseline(VPos.BASELINE);
    }

    public static void drawOutsideModifiers(GraphicsContext gc, SudokuModifiers mods, int size,
                                            double cellW, double cellH, double offsetX, double offsetY,
                                            double gridW, double gridH, boolean forPrint) {
        if (mods == null) return;

        if (mods.hasSkyscraper()) {
            drawSkyscraperClues(gc, mods.getSkyscraper(), size, cellW, cellH, offsetX, offsetY, gridW, gridH, forPrint);
        }
        if (mods.hasSandwich()) {
            drawSandwichClues(gc, mods.getSandwich(), size, cellW, cellH, offsetX, offsetY, gridW, gridH, forPrint);
        }
        if (mods.hasXSums()) {
            drawXSumsClues(gc, mods.getXSums(), size, cellW, cellH, offsetX, offsetY, gridW, gridH, forPrint);
        }
    }

    private static void drawSkyscraperClues(GraphicsContext gc, SkyscraperModifier mod, int size,
                                            double cellW, double cellH, double offsetX, double offsetY,
                                            double gridW, double gridH, boolean forPrint) {
        strokeOutsideNumberClues(gc, size, cellW, cellH, offsetX, offsetY, gridW, gridH, forPrint, mod.top(), mod.bottom(), mod.left(), mod.right());
    }

    private static void drawXSumsClues(GraphicsContext gc, XSumsModifier mod, int size,
                                       double cellW, double cellH, double offsetX, double offsetY,
                                       double gridW, double gridH, boolean forPrint) {
        strokeOutsideNumberClues(gc, size, cellW, cellH, offsetX, offsetY, gridW, gridH, forPrint, mod.top(), mod.bottom(), mod.left(), mod.right());
    }

    private static void strokeOutsideNumberClues(GraphicsContext gc, int size, double cellW, double cellH, double offsetX, double offsetY, double gridW, double gridH, boolean forPrint, int[] top, int[] bottom, int[] left, int[] right) {
        gc.setFill(forPrint ? Color.BLACK : Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(Font.font("System", FontWeight.BOLD, cellH * 0.4));

        for (int i = 0; i < size; i++) {
            if (top != null && top[i] > 0) {
                gc.fillText(String.valueOf(top[i]), offsetX + i * cellW + cellW / 2.0, offsetY / 2.0);
            }
            if (bottom != null && bottom[i] > 0) {
                gc.fillText(String.valueOf(bottom[i]), offsetX + i * cellW + cellW / 2.0, offsetY + gridH + offsetY / 2.0);
            }
            if (left != null && left[i] > 0) {
                gc.fillText(String.valueOf(left[i]), offsetX / 2.0, offsetY + i * cellH + cellH / 2.0);
            }
            if (right != null && right[i] > 0) {
                gc.fillText(String.valueOf(right[i]), offsetX + gridW + offsetX / 2.0, offsetY + i * cellH + cellH / 2.0);
            }
        }
    }

    private static void drawSandwichClues(GraphicsContext gc, SandwichModifier mod, int size,
                                          double cellW, double cellH, double offsetX, double offsetY,
                                          double gridW, double gridH, boolean forPrint) {
        strokeOutsideNumberClues(gc, size, cellW, cellH, offsetX, offsetY, gridW, gridH, forPrint, mod.top(), null, mod.left(), null);
    }

    private static void drawEvenOddMarks(GraphicsContext gc, EvenOddModifier mod, double w, double h, double ox, double oy) {
        ParityType[][] parityGrid = mod.parityTypes();
        if (parityGrid == null) return;
        double shapeSize = Math.min(w, h) * 0.7;
        gc.setLineWidth(Math.min(w, h) * 0.03);
        gc.setStroke(Color.DARKGRAY);
        gc.setFill(Color.rgb(200, 200, 200, 0.4));

        for (int r = 0; r < parityGrid.length; r++) {
            if (parityGrid[r] == null) continue;
            for (int c = 0; c < parityGrid[r].length; c++) {
                ParityType type = parityGrid[r][c];
                if (type == null || type == ParityType.NONE) continue;
                double x = ox + c * w + (w - shapeSize) / 2.0;
                double y = oy + r * h + (h - shapeSize) / 2.0;
                if (type == ParityType.EVEN) {
                    gc.fillRect(x, y, shapeSize, shapeSize);
                    gc.strokeRect(x, y, shapeSize, shapeSize);
                } else {
                    gc.fillOval(x, y, shapeSize, shapeSize);
                    gc.strokeOval(x, y, shapeSize, shapeSize);
                }
            }
        }
    }

    private static void drawGreaterThanConstraints(GraphicsContext gc, GreaterThanModifier mod, double w, double h, int size, double ox, double oy) {
        double baseScale = Math.min(w, h);
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(baseScale * 0.04);
        double s = baseScale * 0.07;

        if (mod.horizontal() != null) {
            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size - 1; c++) {
                    CompType type = mod.horizontal()[r][c];
                    if (type == null) continue;
                    double cx = ox + (c + 1) * w;
                    double cy = oy + r * h + h / 2.0;
                    if (type == CompType.BIGGER) drawChevronRight(gc, cx, cy, s, baseScale);
                    else drawChevronLeft(gc, cx, cy, s, baseScale);
                }
            }
        }
        if (mod.vertical() != null) {
            for (int r = 0; r < size - 1; r++) {
                for (int c = 0; c < size; c++) {
                    CompType type = mod.vertical()[r][c];
                    if (type == null) continue;
                    double cx = ox + c * w + w / 2.0;
                    double cy = oy + (r + 1) * h;
                    if (type == CompType.BIGGER) drawChevronDown(gc, cx, cy, s, baseScale);
                    else drawChevronUp(gc, cx, cy, s, baseScale);
                }
            }
        }
    }

    public static void drawXVMarks(GraphicsContext gc, XvModifier mod, double w, double h, double ox, double oy) {
        if (mod.marks() == null) return;
        double baseScale = Math.min(w, h);
        double s = baseScale * 0.15;
        gc.setFont(Font.font("System", FontWeight.BOLD, baseScale * 0.3));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        for (XVPair pair : mod.marks()) {
            double midX = ox + (pair.first().col() + pair.second().col()) / 2.0 * w + w / 2.0;
            double midY = oy + (pair.first().row() + pair.second().row()) / 2.0 * h + h / 2.0;
            double radius = s * 1.8;
            gc.setFill(Color.WHITE);
            gc.fillOval(midX - radius, midY - radius, radius * 2, radius * 2);
            gc.setStroke(Color.LIGHTGRAY);
            gc.setLineWidth(baseScale * 0.02);
            gc.strokeOval(midX - radius, midY - radius, radius * 2, radius * 2);
            gc.setFill(Color.BLACK);
            gc.fillText(String.valueOf(pair.markType()), midX, midY);
        }
    }

    private static void drawConsecutivePairs(GraphicsContext gc, ConsecutiveModifier mod, double w, double h, double ox, double oy) {
        if (mod.cells() == null) return;
        double baseScale = Math.min(w, h);
        double markerSize = baseScale * 0.25;
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(baseScale * 0.02);

        for (Pair<GridCell, GridCell> pair : mod.cells()) {
            double midX = ox + (pair.getFirst().col() + pair.getSecond().col()) / 2.0 * w + w / 2.0;
            double midY = oy + (pair.getFirst().row() + pair.getSecond().row()) / 2.0 * h + h / 2.0;
            gc.fillOval(midX - markerSize / 2, midY - markerSize / 2, markerSize, markerSize);
            gc.strokeOval(midX - markerSize / 2, midY - markerSize / 2, markerSize, markerSize);
        }
    }

    private static void drawKropkiPairs(GraphicsContext gc, KropkiModifier mod, double w, double h, double ox, double oy) {
        if (mod.dots() == null) return;
        double baseScale = Math.min(w, h);
        double markerSize = baseScale * 0.25;
        gc.setLineWidth(baseScale * 0.02);

        for (KropkiDot dot : mod.dots()) {
            double midX = ox + (dot.first().col() + dot.second().col()) / 2.0 * w + w / 2.0;
            double midY = oy + (dot.first().row() + dot.second().row()) / 2.0 * h + h / 2.0;
            gc.setStroke(Color.BLACK);
            gc.setFill(dot.color() == DotColor.BLACK ? Color.BLACK : Color.WHITE);
            gc.fillOval(midX - markerSize / 2, midY - markerSize / 2, markerSize, markerSize);
            gc.strokeOval(midX - markerSize / 2, midY - markerSize / 2, markerSize, markerSize);
        }
    }

    private static void drawVudokuMarks(GraphicsContext gc, VudokuModifier mod, double w, double h, double ox, double oy) {
        if (mod.marks() == null) return;
        gc.setStroke(Color.rgb(100, 100, 100, 0.5));
        gc.setLineWidth(Math.min(w, h) * 0.1);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        for (VudokuMark mark : mod.marks()) {
            gc.beginPath();
            gc.moveTo(ox + mark.arm1().col() * w + w / 2, oy + mark.arm1().row() * h + h / 2);
            gc.lineTo(ox + mark.vertex().col() * w + w / 2, oy + mark.vertex().row() * h + h / 2);
            gc.lineTo(ox + mark.arm2().col() * w + w / 2, oy + mark.arm2().row() * h + h / 2);
            gc.stroke();
        }
    }

    private static void drawBetweenLines(GraphicsContext gc, BetweenModifier mod, double w, double h, double ox, double oy) {
        if (mod.lines() == null) return;
        double baseScale = Math.min(w, h);
        gc.setStroke(Color.rgb(180, 180, 180, 0.7));
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        for (BetweenLine line : mod.lines()) {
            gc.setLineWidth(baseScale * 0.15);
            gc.beginPath();
            double sx = ox + line.startCircle().col() * w + w / 2;
            double sy = oy + line.startCircle().row() * h + h / 2;
            double ex = ox + line.endCircle().col() * w + w / 2;
            double ey = oy + line.endCircle().row() * h + h / 2;
            gc.moveTo(sx, sy);
            for (GridCell cell : line.lineCells()) {
                gc.lineTo(ox + cell.col() * w + w / 2, oy + cell.row() * h + h / 2);
            }
            gc.lineTo(ex, ey);
            gc.stroke();

            double r = baseScale * 0.7;
            gc.setFill(Color.rgb(180, 180, 180, 0.7));
            gc.fillOval(sx - r / 2, sy - r / 2, r, r);
            gc.fillOval(ex - r / 2, ey - r / 2, r, r);
            gc.setStroke(Color.DARKGRAY);
            gc.setLineWidth(baseScale * 0.03);
            gc.strokeOval(sx - r / 2, sy - r / 2, r, r);
            gc.strokeOval(ex - r / 2, ey - r / 2, r, r);
        }
    }

    private static void drawChevronBackground(GraphicsContext gc, double x, double y, double s, double baseScale) {
        double radius = s * 1.8;
        gc.setFill(Color.WHITE);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(baseScale * 0.02);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(baseScale * 0.04);
    }

    private static void drawChevronRight(GraphicsContext gc, double x, double y, double s, double baseScale) {
        drawChevronBackground(gc, x, y, s, baseScale);
        gc.strokeLine(x - s, y - s, x + s, y);
        gc.strokeLine(x + s, y, x - s, y + s);
    }

    private static void drawChevronLeft(GraphicsContext gc, double x, double y, double s, double baseScale) {
        drawChevronBackground(gc, x, y, s, baseScale);
        gc.strokeLine(x + s, y - s, x - s, y);
        gc.strokeLine(x - s, y, x + s, y + s);
    }

    private static void drawChevronDown(GraphicsContext gc, double x, double y, double s, double baseScale) {
        drawChevronBackground(gc, x, y, s, baseScale);
        gc.strokeLine(x - s, y - s, x, y + s);
        gc.strokeLine(x, y + s, x + s, y - s);
    }

    private static void drawChevronUp(GraphicsContext gc, double x, double y, double s, double baseScale) {
        drawChevronBackground(gc, x, y, s, baseScale);
        gc.strokeLine(x - s, y + s, x, y - s);
        gc.strokeLine(x, y - s, x + s, y + s);
    }
}
