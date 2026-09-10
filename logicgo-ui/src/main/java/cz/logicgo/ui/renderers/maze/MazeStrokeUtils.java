package cz.logicgo.ui.renderers.maze;

import cz.logicgo.core.gameClasses.maze.MazeUtils;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.gameModes.*;
import cz.logicgo.core.misc.Pair;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.HexagonalDirection;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.MazeDirection;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.RectangularDirection;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.HashSet;
import java.util.List;

import static cz.logicgo.ui.utils.SudokuUiUtils.PATTERN_PALETTE;


public class MazeStrokeUtils {

    public static double[] getCellCenter(MazeCell cell, double cellW, double cellH, MazeShape shape) {
        if (shape == MazeShape.HEXAGONAL) {
            double bSize = cellH / 2.0;
            double cx = (cellW / 2.0) + (cell.getCol() * 0.75 * cellW);
            double cy = (cellH / 2.0) + (cell.getRow() * cellH);
            if (cell.getCol() % 2 != 0) cy += bSize;
            return new double[]{cx, cy};
        } else {
            double cx = (cell.getCol() * cellW) + (cellW / 2.0);
            double cy = (cell.getRow() * cellH) + (cellH / 2.0);
            return new double[]{cx, cy};
        }
    }

    public static MazeDirection getDirectionBetween(MazeCell from, MazeCell to) {
        for (RectangularDirection dir : RectangularDirection.values()) {
            MazeCell n = from.getNeighbourFromDirection(dir);
            if (n != null && n.getRow() == to.getRow() && n.getCol() == to.getCol()) return dir;
        }
        for (HexagonalDirection dir : HexagonalDirection.values()) {
            MazeCell n = from.getNeighbourFromDirection(dir);
            if (n != null && n.getRow() == to.getRow() && n.getCol() == to.getCol()) return dir;
        }
        return null;
    }

    public static void strokeOnWall(int row, int col, double width, double height, MazeDirection innerDir, Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        switch (innerDir) {
            case HexagonalDirection hexagonalDirection -> {
                double aSize = width / 4.0;
                double bSize = height / 2.0;
                double radius = width / 2.0;

                double cx = (width / 2.0) + (col * 0.75 * width);
                double cy = (height / 2.0) + (row * height);

                if (col % 2 != 0) {
                    cy += bSize;
                }

                double xFw = cx - radius;
                double xNw = cx - aSize;
                double xNe = cx + aSize;
                double xFe = cx + radius;

                double yN = cy - bSize;
                double yM = cy;
                double yS = cy + bSize;

                switch (hexagonalDirection) {
                    case NORTH_WEST -> gc.strokeLine(xFw, yM, xNw, yN);
                    case NORTH -> gc.strokeLine(xNw, yN, xNe, yN);
                    case NORTH_EAST -> gc.strokeLine(xNe, yN, xFe, yM);
                    case SOUTH_EAST -> gc.strokeLine(xFe, yM, xNe, yS);
                    case SOUTH -> gc.strokeLine(xNe, yS, xNw, yS);
                    case SOUTH_WEST -> gc.strokeLine(xFw, yM, xNw, yS);
                }
            }
            case RectangularDirection rectangularDirection -> {
                double x1 = col * width;
                double y1 = row * height;
                double x2 = (col + 1) * width;
                double y2 = (row + 1) * height;

                switch (rectangularDirection) {
                    case NORTH -> gc.strokeLine(x1, y1, x2, y1);
                    case EAST -> gc.strokeLine(x2, y1, x2, y2);
                    case SOUTH -> gc.strokeLine(x1, y2, x2, y2);
                    case WEST -> gc.strokeLine(x1, y1, x1, y2);
                }
            }
        }
    }

    public static void strokePortals(Canvas canvas, PortalModifier mod, double cellW, double cellH, MazeShape shape, List<Color> colors) {
        var gc = canvas.getGraphicsContext2D();
        gc.save();
        int colorIndex = 0;

        for (var pairs : mod.portals()) {
            MazeCell innerPortal = pairs.getFirst();
            MazeCell outerPortal = pairs.getSecond();

            double[] centerInner = getCellCenter(innerPortal, cellW, cellH, shape);
            double[] centerOuter = getCellCenter(outerPortal, cellW, cellH, shape);
            double radius = cellW * 0.25;

            Color portalColor = colors.get(colorIndex % colors.size());
            colorIndex++;

            double strokeWidth = Math.max(1.5, cellW * 0.05);

            gc.setFill(portalColor);
            gc.setStroke(Color.web("#334155"));
            gc.setLineWidth(strokeWidth);

            gc.fillOval(centerInner[0] - radius, centerInner[1] - radius, radius * 2, radius * 2);
            gc.strokeOval(centerInner[0] - radius, centerInner[1] - radius, radius * 2, radius * 2);

            gc.fillOval(centerOuter[0] - radius, centerOuter[1] - radius, radius * 2, radius * 2);
            gc.strokeOval(centerOuter[0] - radius, centerOuter[1] - radius, radius * 2, radius * 2);
        }
        gc.restore();
    }

    public static void strokeBWalls(Canvas canvas, WallModifier mod, double cellW, double cellH) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.save();

        Color glowColor = Color.web("#EA580C", 0.35);
        Color coreColor = Color.web("#C2410C", 0.95);

        for (MazeUtils.RegionEdge pairs : mod.regionEdges()) {
            MazeCell first = pairs.cellA();
            MazeCell second = pairs.cellB();

            MazeDirection direction = getDirectionBetween(first, second);
            if (direction == null) continue;

            int row = first.getRow();
            int col = first.getCol();

            gc.setStroke(glowColor);
            gc.setLineWidth(Math.max(5.0, cellW * 0.20));
            gc.setLineDashes(null);
            strokeOnWall(row, col, cellW, cellH, direction, canvas);

            gc.setStroke(coreColor);
            gc.setLineWidth(Math.max(2.5, cellW * 0.09));
            gc.setLineDashes(6, 4);
            strokeOnWall(row, col, cellW, cellH, direction, canvas);
        }
        gc.restore();
    }

    public static void strokeOrderedCheckpoints(Canvas canvas, CheckpointModifier modifier, List<MazeCell> visitedCells, double cellW, double cellH, MazeShape shape) {
        var gc = canvas.getGraphicsContext2D();
        HashSet<MazeCell> visited = visitedCells != null ? new HashSet<>(visitedCells) : new HashSet<>();

        gc.save();

        double lineWidth = Math.max(1.5, cellW * 0.08);
        gc.setStroke(Color.web("#0EA5E9"));
        gc.setLineWidth(lineWidth);

        for (Pair<MazeCell, MazeCell> path : modifier.getOneWays()) {
            MazeCell from = path.getFirst();
            MazeCell to = path.getSecond();

            double[] cFrom = getCellCenter(from, cellW, cellH, shape);
            double[] cTo = getCellCenter(to, cellW, cellH, shape);

            double angle = Math.atan2(cTo[1] - cFrom[1], cTo[0] - cFrom[0]);
            double dist = Math.hypot(cTo[0] - cFrom[0], cTo[1] - cFrom[1]);
            double offset = dist * 0.3;

            double startX = cFrom[0] + offset * Math.cos(angle);
            double startY = cFrom[1] + offset * Math.sin(angle);
            double endX = cTo[0] - offset * Math.cos(angle);
            double endY = cTo[1] - offset * Math.sin(angle);

            gc.strokeLine(startX, startY, endX, endY);

            double arrowSize = Math.max(cellW * 0.25, 4.0);
            gc.strokeLine(endX, endY, endX - arrowSize * Math.cos(angle - Math.PI / 6), endY - arrowSize * Math.sin(angle - Math.PI / 6));
            gc.strokeLine(endX, endY, endX - arrowSize * Math.cos(angle + Math.PI / 6), endY - arrowSize * Math.sin(angle + Math.PI / 6));
        }

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        double fontSize = Math.max(cellW * 0.55, 8.0);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, fontSize));

        for (var entry : modifier.getCheckpoints().entrySet()) {
            MazeCell cell = entry.getValue();
            if (visited.contains(cell)) continue;

            double[] center = getCellCenter(cell, cellW, cellH, shape);
            double cx = center[0];
            double cy = center[1];

            double radius = cellW * 0.35;
            gc.setFill(Color.web("#0F172A", 0.1));
            gc.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

            gc.setFill(Color.web("#0EA5E9"));
            gc.fillText(String.valueOf(entry.getKey()), cx, cy);
        }
        gc.restore();
    }

    public static void strokeOneWayPaths(Canvas canvas, OneWayModifier modifier, double cellW, double cellH, MazeShape shape) {
        var gc = canvas.getGraphicsContext2D();

        gc.save();
        gc.setStroke(Color.web("#0EA5E9"));
        gc.setLineWidth(2.5);

        for (Pair<MazeCell, MazeCell> path : modifier.oneWayPaths()) {
            MazeCell from = path.getFirst();
            MazeCell to = path.getSecond();

            double[] cFrom = getCellCenter(from, cellW, cellH, shape);
            double[] cTo = getCellCenter(to, cellW, cellH, shape);

            double angle = Math.atan2(cTo[1] - cFrom[1], cTo[0] - cFrom[0]);
            double dist = Math.hypot(cTo[0] - cFrom[0], cTo[1] - cFrom[1]);
            double offset = dist * 0.3;

            double startX = cFrom[0] + offset * Math.cos(angle);
            double startY = cFrom[1] + offset * Math.sin(angle);
            double endX = cTo[0] - offset * Math.cos(angle);
            double endY = cTo[1] - offset * Math.sin(angle);

            gc.strokeLine(startX, startY, endX, endY);

            double arrowSize = 6.0;
            gc.strokeLine(endX, endY, endX - arrowSize * Math.cos(angle - Math.PI / 6), endY - arrowSize * Math.sin(angle - Math.PI / 6));
            gc.strokeLine(endX, endY, endX - arrowSize * Math.cos(angle + Math.PI / 6), endY - arrowSize * Math.sin(angle + Math.PI / 6));
        }
        gc.restore();
    }

    public static void strokePatterns(Canvas canvas, PatternModifier mod, double cellW, double cellH, MazeShape shape) {
        var gc = canvas.getGraphicsContext2D();
        gc.save();
        gc.setLineWidth(Math.max(1.8, cellW * 0.08));

        for (var entry : mod.getCellPatterns().entrySet()) {
            MazeCell cell = entry.getKey();
            int patternId = entry.getValue();

            double[] center = getCellCenter(cell, cellW, cellH, shape);
            double cx = center[0];
            double cy = center[1];
            double size = cellW * 0.22;

            strokePatternStuff(gc, patternId, cx, cy, size);
        }
        gc.restore();
    }

    public static void strokePatternStuff(GraphicsContext pGc, int patternId, double cx, double cy, double size) {
        Color color = PATTERN_PALETTE[patternId % PATTERN_PALETTE.length];
        pGc.setStroke(color);

        int shapeId = patternId % 8;
        switch (shapeId) {
            case 0 -> pGc.strokeOval(cx - size, cy - size, size * 2, size * 2);
            case 1 -> pGc.strokeRect(cx - size, cy - size, size * 2, size * 2);
            case 2 -> pGc.strokePolygon(
                    new double[]{cx, cx + size, cx - size},
                    new double[]{cy - size, cy + size, cy + size}, 3);
            case 3 -> pGc.strokePolygon(
                    new double[]{cx, cx + size, cx, cx - size},
                    new double[]{cy - size, cy, cy + size, cy}, 4);
            case 4 -> {
                double h = size * Math.sqrt(3) / 2;
                pGc.strokePolygon(
                        new double[]{cx - size / 2, cx + size / 2, cx + size, cx + size / 2, cx - size / 2, cx - size},
                        new double[]{cy - h, cy - h, cy, cy + h, cy + h, cy}, 6);
            }
            case 5 -> {
                double[] px = new double[5];
                double[] py = new double[5];
                for (int j = 0; j < 5; j++) {
                    px[j] = cx + size * Math.cos(Math.toRadians(j * 72 - 90));
                    py[j] = cy + size * Math.sin(Math.toRadians(j * 72 - 90));
                }
                pGc.strokePolygon(px, py, 5);
            }
            case 6 -> {
                pGc.strokeLine(cx - size, cy, cx + size, cy);
                pGc.strokeLine(cx, cy - size, cx, cy + size);
            }
            case 7 -> {
                double offset = size * 0.8;
                pGc.strokeLine(cx - offset, cy - offset, cx + offset, cy + offset);
                pGc.strokeLine(cx + offset, cy - offset, cx - offset, cy + offset);
            }
        }
    }

    public static void strokeTolls(Canvas canvas, TollModifier mod, List<MazeCell> visitedCells, double cellW, double cellH, MazeShape shape) {
        var gc = canvas.getGraphicsContext2D();
        gc.save();

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        for (var entry : mod.tolls().entrySet()) {
            MazeCell cell = entry.getKey();
            int value = entry.getValue();

            double[] center = getCellCenter(cell, cellW, cellH, shape);
            double cx = center[0];
            double cy = center[1];

            double radius = cellW * 0.35;
            double fontSize = Math.max(cellW * 0.55, 8.0);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, fontSize));

            int displayValue = Math.abs(value);
            gc.setFill(Color.web("#F8FAFC"));
            gc.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

            if (value > 0) {
                gc.setFill(Color.web("#D97706"));
                gc.fillText(String.valueOf(displayValue), cx, cy);
            } else {
                gc.setFill(Color.web("#DC2626"));
                gc.fillText(String.valueOf(displayValue), cx, cy);
            }
        }
        gc.restore();
    }

    public static void strokeWrapAround(Canvas canvas, WrapAroundModifier modifier, double cellW, double cellH, MazeShape shape) {
        var gc = canvas.getGraphicsContext2D();
        gc.save();
        gc.setLineWidth(3.0);
        gc.setLineDashes(8, 6);
        Color[] wrapColors = {
                Color.web("#6366F1"), Color.web("#F59E0B"), Color.web("#10B981"),
                Color.web("#EC4899"), Color.web("#06B6D4"), Color.web("#8B5CF6")
        };
        int colorIndex = 0;

        for (Pair<MazeCell, MazeCell> wrap : modifier.wrapLinks()) {
            MazeCell cellA = wrap.getFirst();
            MazeCell cellB = wrap.getSecond();

            double[] centerA = getCellCenter(cellA, cellW, cellH, shape);
            double[] centerB = getCellCenter(cellB, cellW, cellH, shape);
            double radius = cellW * 0.28;

            gc.setStroke(wrapColors[colorIndex % wrapColors.length]);
            colorIndex++;

            gc.strokeOval(centerA[0] - radius, centerA[1] - radius, radius * 2, radius * 2);
            gc.strokeOval(centerB[0] - radius, centerB[1] - radius, radius * 2, radius * 2);
        }
        gc.restore();
    }
}
