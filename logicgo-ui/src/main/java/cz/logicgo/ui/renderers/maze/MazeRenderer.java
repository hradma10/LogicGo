package cz.logicgo.ui.renderers.maze;


import cz.logicgo.core.GameUtils;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.HexagonalCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.RectangularCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.gameModes.*;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.core.gameClasses.maze.dataStructures.path.PlayerPath;
import cz.logicgo.core.misc.enums.gameTypes.maze.LevelType;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.HexagonalDirection;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.RectangularDirection;
import cz.logicgo.core.misc.enums.hints.MazeHintType;
import cz.logicgo.ui.utils.GameUiUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

import java.util.ArrayList;
import java.util.List;

import static cz.logicgo.ui.renderers.maze.MazeStrokeUtils.*;


public class MazeRenderer {

    public static void renderGrid(Canvas canvas, MazeGrid grid, int currentFloor, int totalFloors, boolean forPrint, User user) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        double scale = 0.95;
        double width = canvasWidth * scale;
        double height = canvasHeight * scale;
        double offsetX = (canvasWidth - width) / 2;
        double offsetY = (canvasHeight - height) / 2;

        gc.save();
        gc.translate(offsetX, offsetY);

        int cols = grid.getColCount();
        int rows = grid.getRowCount();
        double cellW;
        double cellH;

        if (grid.getMazeShape() == MazeShape.HEXAGONAL) {
            double aw = 1.0 + 0.75 * (cols - 1);
            double ah = rows + (cols > 1 ? 0.5 : 0.0);
            cellW = width / aw;
            cellH = height / ah;
        } else {
            cellW = width / cols;
            cellH = height / rows;
        }

        for (MazeCell[] row : grid.getGrid()) {
            for (MazeCell cell : row) {
                if (cell != null) {
                    renderCell(gc, cell, cellW, cellH, currentFloor, totalFloors, forPrint, grid.getMazeShape());
                }
            }
        }

        List<Color> colors;
        if (user == null) {
            colors = GameUiUtils.getDefaultColorTheme();
        } else {
            colors = user.getUserColors().stream().map(Color::web).toList();
        }

        for (MazeModifier modifier : grid.getModifiers()) {
            switch (modifier) {
                case PortalModifier portalModifier ->
                        strokePortals(canvas, portalModifier, cellW, cellH, grid.getMazeShape(), colors);
                case WallModifier wallModifier -> strokeBWalls(canvas, wallModifier, cellW, cellH);
                case OneWayModifier oneWayModifier ->
                        strokeOneWayPaths(canvas, oneWayModifier, cellW, cellH, grid.getMazeShape());
                case CheckpointModifier checkpointModifier ->
                        strokeOrderedCheckpoints(canvas, checkpointModifier, grid.getPath().getActivePath().stream().toList(), cellW, cellH, grid.getMazeShape());
                case WrapAroundModifier wrapAroundModifier ->
                        strokeWrapAround(canvas, wrapAroundModifier, cellW, cellH, grid.getMazeShape());
                case PatternModifier patternModifier ->
                        strokePatterns(canvas, patternModifier, cellW, cellH, grid.getMazeShape());
                case TollModifier tollModifier ->
                        strokeTolls(canvas, tollModifier, grid.getPath().getActivePath().stream().toList(), cellW, cellH, grid.getMazeShape());
                case ExactStepsModifier _ -> {
                }
            }
        }

        gc.restore();
    }

    private static void renderCell(GraphicsContext gc, MazeCell cell, double cellW, double cellH, int currentFloor, int totalFloors, boolean forPrint, MazeShape shape) {
        switch (cell) {
            case HexagonalCell hexagonalCell ->
                    renderHexCell(gc, hexagonalCell, cellW, cellH, currentFloor, totalFloors, forPrint, shape);
            case RectangularCell rectCell ->
                    renderRectangularCell(gc, rectCell, cellW, cellH, currentFloor, totalFloors, forPrint, shape);
        }
    }

    private static void renderRectangularCell(GraphicsContext gc, RectangularCell cell, double width, double height, int currentFloor, int totalFloors, boolean isPrintMode, MazeShape shape) {
        gc.setStroke(Color.web("#0F172A"));
        double wallLineWidth = Math.max(2.0, width * 0.08);
        gc.setLineWidth(wallLineWidth);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        int cellRow = cell.getRow();
        int cellCol = cell.getCol();
        double x1 = cellCol * width;
        double y1 = cellRow * height;
        double x2 = (cellCol + 1) * width;
        double y2 = (cellRow + 1) * height;

        if (cell.isXMarked()) {
            gc.setFill(Color.rgb(255, 0, 0, 0.15));
            gc.fillRect(x1, y1, width, height);
        }
        MazeCell.StatusCell activeStatus = MazeCell.StatusCell.NONE;
        for (RectangularDirection dir : RectangularDirection.values()) {
            MazeCell.StatusCell status = cell.getStartOrEndStatus(dir);
            if (status != MazeCell.StatusCell.NONE) {
                activeStatus = status;
                break;
            }
        }
        if (activeStatus == MazeCell.StatusCell.NONE && cell.getStartAndEnd() != null) {
            if (cell.getStartAndEnd().startCell() == cell) {
                activeStatus = MazeCell.StatusCell.START;
            } else if (cell.getStartAndEnd().endCell() == cell) {
                activeStatus = MazeCell.StatusCell.END;
            }
        }

        if (activeStatus != MazeCell.StatusCell.NONE) {
            double[] center = getCellCenter(cell, width, height, shape);
            drawStairsOrEnd(gc, activeStatus, center[0], center[1], width, height, currentFloor, totalFloors, isPrintMode);
        }
        if (shouldRenderRectWall(cell, RectangularDirection.NORTH)) gc.strokeLine(x1, y1, x2, y1);
        if (shouldRenderRectWall(cell, RectangularDirection.SOUTH)) gc.strokeLine(x1, y2, x2, y2);
        if (shouldRenderRectWall(cell, RectangularDirection.WEST)) gc.strokeLine(x1, y1, x1, y2);
        if (shouldRenderRectWall(cell, RectangularDirection.EAST)) gc.strokeLine(x2, y1, x2, y2);
    }

    private static boolean shouldRenderRectWall(RectangularCell cell, RectangularDirection dir) {
        MazeCell mc = cell.getNeighbourFromDirection(dir);
        if (mc != null && cell.isLinked(mc)) return false;

        if (cell.getLevelType() == LevelType.MIDDLE) {
            return true;
        }

        boolean notSkip = cell.notSkipWall(dir, cell);
        if (!notSkip) {
            LevelType levelType = cell.getLevelType();
            if (levelType == LevelType.START) {
                return cell.getStartOrEndStatus(dir) != MazeCell.StatusCell.START;
            }
            if (levelType == LevelType.END) {
                return cell.getStartOrEndStatus(dir) != MazeCell.StatusCell.END;
            }
        }
        return notSkip;
    }

    private static void renderHexCell(GraphicsContext gc, HexagonalCell cell, double width, double height, int currentFloor, int totalFloors, boolean isPrintMode, MazeShape shape) {
        gc.setStroke(Color.web("#0F0F1A"));
        double wallLineWidth = Math.max(2.0, width * 0.08);
        gc.setLineWidth(wallLineWidth);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        double aSize = width / 4.0;
        double bSize = height / 2.0;
        double radius = width / 2.0;

        double[] center = getCellCenter(cell, width, height, shape);
        double cx = center[0];
        double cy = center[1];

        double xFw = cx - radius;
        double xNw = cx - aSize;
        double xNe = cx + aSize;
        double xFe = cx + radius;
        double yN = cy - bSize;
        double yM = cy;
        double yS = cy + bSize;
        MazeCell.StatusCell activeStatus = MazeCell.StatusCell.NONE;
        for (HexagonalDirection dir : HexagonalDirection.values()) {
            MazeCell.StatusCell status = cell.getStartOrEndStatus(dir);
            if (status != MazeCell.StatusCell.NONE) {
                activeStatus = status;
                break;
            }
        }

        if (activeStatus == MazeCell.StatusCell.NONE && cell.getStartAndEnd() != null) {
            if (cell.getStartAndEnd().startCell() == cell) {
                activeStatus = MazeCell.StatusCell.START;
            } else if (cell.getStartAndEnd().endCell() == cell) {
                activeStatus = MazeCell.StatusCell.END;
            }
        }

        if (activeStatus != MazeCell.StatusCell.NONE) {
            drawStairsOrEnd(gc, activeStatus, cx, cy, width, height, currentFloor, totalFloors, isPrintMode);
        }
        if (shouldRenderHexWall(cell, HexagonalDirection.SOUTH_WEST)) gc.strokeLine(xFw, yM, xNw, yS);
        if (shouldRenderHexWall(cell, HexagonalDirection.SOUTH)) gc.strokeLine(xNe, yS, xNw, yS);
        if (shouldRenderHexWall(cell, HexagonalDirection.SOUTH_EAST)) gc.strokeLine(xFe, yM, xNe, yS);
        if (shouldRenderHexWall(cell, HexagonalDirection.NORTH_WEST)) gc.strokeLine(xFw, yM, xNw, yN);
        if (shouldRenderHexWall(cell, HexagonalDirection.NORTH)) gc.strokeLine(xNw, yN, xNe, yN);
        if (shouldRenderHexWall(cell, HexagonalDirection.NORTH_EAST)) gc.strokeLine(xNe, yN, xFe, yM);
    }

    private static boolean shouldRenderHexWall(HexagonalCell cell, HexagonalDirection dir) {
        MazeCell mc = cell.getNeighbourFromDirection(dir);
        if (mc != null && cell.isLinked(mc)) return false;

        if (cell.getLevelType() == LevelType.MIDDLE) {
            return true;
        }

        boolean notSkip = cell.notSkipWall(dir, cell);
        if (!notSkip) {
            LevelType levelType = cell.getLevelType();
            if (levelType == LevelType.START) {
                return cell.getStartOrEndStatus(dir) != MazeCell.StatusCell.START;
            }
            if (levelType == LevelType.END) {
                return cell.getStartOrEndStatus(dir) != MazeCell.StatusCell.END;
            }
        }
        return notSkip;
    }

    private static void drawStairsOrEnd(GraphicsContext gc, MazeCell.StatusCell status, double cx, double cy, double cellW, double cellH, int currentFloor, int totalFloors, boolean isPrintMode) {
        double radius = Math.min(cellW, cellH) * 0.45;
        gc.save();

        double shapeW = radius * 1.2;

        boolean isGlobalStart = (status == MazeCell.StatusCell.START && currentFloor == 0);
        boolean isGlobalEnd = (status == MazeCell.StatusCell.END && currentFloor == totalFloors - 1);
        boolean isFloorTransition = (!isGlobalStart && !isGlobalEnd && totalFloors > 1);

        Color startColor = isPrintMode ? Color.BLACK : Color.web("#10B981");
        Color endColor = isPrintMode ? Color.BLACK : Color.web("#EF4444");
        Color ladderColor = isPrintMode ? Color.web("#1E293B") : Color.web("#8B4513");
        Color arrowColor = isPrintMode ? Color.BLACK : Color.web("#334155");

        if (isGlobalStart) {
            gc.setFill(startColor);
            gc.fillPolygon(
                    new double[]{cx - shapeW / 3, cx + shapeW / 2, cx - shapeW / 3},
                    new double[]{cy - shapeW / 2, cy, cy + shapeW / 2},
                    3
            );
        } else if (isGlobalEnd) {
            gc.setStroke(endColor);
            gc.setLineWidth(shapeW * 0.25);
            gc.strokeOval(cx - shapeW / 2, cy - shapeW / 2, shapeW, shapeW);
            gc.setFill(endColor);
            gc.fillOval(cx - shapeW / 6, cy - shapeW / 6, shapeW / 3, shapeW / 3);
        } else if (isFloorTransition) {
            gc.setStroke(ladderColor);
            gc.setLineWidth(Math.max(1.5, cellW * 0.06));

            double ladderW = shapeW * 0.5;
            double ladderH = shapeW * 0.8;
            double leftX = cx - ladderW / 2;
            double rightX = cx + ladderW / 2;
            double topY = cy - ladderH / 2;
            double bottomY = cy + ladderH / 2;

            gc.strokeLine(leftX, topY, leftX, bottomY);
            gc.strokeLine(rightX, topY, rightX, bottomY);

            int rungs = 4;
            double step = ladderH / (rungs + 1);
            for (int i = 1; i <= rungs; i++) {
                double rungY = topY + i * step;
                gc.strokeLine(leftX, rungY, rightX, rungY);
            }

            gc.setFill(arrowColor);
            if (status == MazeCell.StatusCell.END) {
                gc.fillPolygon(
                        new double[]{cx - ladderW / 3, cx + ladderW / 3, cx},
                        new double[]{bottomY + ladderH * 0.1, bottomY + ladderH * 0.1, bottomY + ladderH * 0.4},
                        3
                );
            } else {
                gc.fillPolygon(
                        new double[]{cx - ladderW / 3, cx + ladderW / 3, cx},
                        new double[]{topY - ladderH * 0.1, topY - ladderH * 0.1, topY - ladderH * 0.4},
                        3
                );
            }
        }
        gc.restore();
    }

    private static double[] getCellCenter(MazeCell cell, double cellW, double cellH, MazeShape shape) {
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

    public static void renderPath(Canvas canvas, PlayerPath playerPath, MazeGrid grid, boolean forPrint) {
        var activePath = playerPath.getActivePath();
        if (activePath.isEmpty()) return;

        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        GraphicsContext gc = canvas.getGraphicsContext2D();

        double scale = 0.95;
        double width = canvasWidth * scale;
        double height = canvasHeight * scale;
        double offsetX = (canvasWidth - width) / 2;
        double offsetY = (canvasHeight - height) / 2;

        gc.save();
        gc.translate(offsetX, offsetY);

        int cols = grid.getColCount();
        int rows = grid.getRowCount();
        double cellW, cellH;

        if (grid.getMazeShape() == MazeShape.HEXAGONAL) {
            double aw = 1.0 + 0.75 * (cols - 1);
            double ah = rows + (cols > 1 ? 0.5 : 0.0);
            cellW = width / aw;
            cellH = height / ah;
        } else {
            cellW = width / cols;
            cellH = height / rows;
        }

        List<MazeCell> pathList = new ArrayList<>(activePath);

        double estimatedCellSize = Math.min(cellW, cellH);
        double strokeWidth = estimatedCellSize * 0.16;
        double dotRadius = strokeWidth * 0.9;

        MazeCell currentCell = pathList.getFirst();
        List<MazeCell> solutionPath = playerPath.getSolutionPath();
        List<MazeCell> hintParts = null;

        int indexOfCurrent = solutionPath.indexOf(currentCell);
        MazeHintType hintType = playerPath.getMazeHintType();

        if (hintType != null && indexOfCurrent == -1) {
            for (var cell : pathList) {
                indexOfCurrent = solutionPath.indexOf(cell);
                if (indexOfCurrent > -1) {
                    break;
                }
            }
        }

        if (indexOfCurrent != -1) {
            switch (hintType) {
                case SHOW_LITTLE_OF_PATH ->
                        hintParts = solutionPath.subList(indexOfCurrent, Math.min(indexOfCurrent + 10, solutionPath.size()));
                case SHOW_ALL_OF_PATH -> hintParts = solutionPath.subList(indexOfCurrent, solutionPath.size());
                case null, default -> hintParts = null;
            }
        }

        if (hintParts != null) {
            gc.save();
            gc.setStroke(forPrint ? Color.web("#0284C7") : Color.web("#06B6D4"));
            gc.setLineWidth(strokeWidth);
            gc.setLineCap(StrokeLineCap.ROUND);
            gc.setLineJoin(StrokeLineJoin.ROUND);
            gc.setLineDashes(10);
            drawPathLine(gc, hintParts, dotRadius, false, cellW, cellH, grid.getMazeShape(), forPrint);
            gc.restore();
        }

        if (playerPath.isShowTravelPath()) {
            gc.save();
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(strokeWidth * 1.6);
            gc.setLineCap(StrokeLineCap.ROUND);
            gc.setLineJoin(StrokeLineJoin.ROUND);
            drawPathLine(gc, pathList, dotRadius, false, cellW, cellH, grid.getMazeShape(), forPrint);
            gc.restore();

            Color defStroke = forPrint ? Color.web("#1D4ED8") : Color.web("#2563EB");
            gc.setStroke(defStroke);
            gc.setLineWidth(strokeWidth);
            gc.setLineCap(StrokeLineCap.ROUND);
            gc.setLineJoin(StrokeLineJoin.ROUND);

            drawPathLine(gc, pathList, dotRadius, true, cellW, cellH, grid.getMazeShape(), forPrint);
        } else if (!forPrint) {
            drawHead(gc, currentCell, dotRadius, cellW, cellH, grid.getMazeShape());
        }

        gc.restore();
    }

    private static void drawPathLine(GraphicsContext gc, List<MazeCell> pathList, double dotRadius, boolean drawHead, double cellW, double cellH, MazeShape shape, boolean forPrint) {
        if (pathList.isEmpty()) return;

        gc.beginPath();
        double[] firstCenter = getCellCenter(pathList.getFirst(), cellW, cellH, shape);
        gc.moveTo(firstCenter[0], firstCenter[1]);

        for (int i = 0; i < pathList.size() - 1; i++) {
            MazeCell current = pathList.get(i);
            MazeCell next = pathList.get(i + 1);

            if (isTeleport(current, next)) {
                double[] curCenter = getCellCenter(current, cellW, cellH, shape);
                gc.lineTo(curCenter[0], curCenter[1]);
                gc.stroke();

                double[] nextCenter = getCellCenter(next, cellW, cellH, shape);
                gc.beginPath();
                gc.moveTo(nextCenter[0], nextCenter[1]);
            } else {
                double[] nextCenter = getCellCenter(next, cellW, cellH, shape);
                gc.lineTo(nextCenter[0], nextCenter[1]);
            }
        }

        double[] lastCenter = getCellCenter(pathList.getLast(), cellW, cellH, shape);
        gc.lineTo(lastCenter[0], lastCenter[1]);
        gc.stroke();

        if (drawHead && !forPrint) {
            drawHead(gc, pathList.getFirst(), dotRadius, cellW, cellH, shape);
        }
    }

    private static void drawHead(GraphicsContext gc, MazeCell cell, double r, double cellW, double cellH, MazeShape shape) {
        if (cell == null) return;
        double[] center = getCellCenter(cell, cellW, cellH, shape);
        gc.setFill(Color.web("#DC2626"));
        gc.fillOval(center[0] - r, center[1] - r, r * 2, r * 2);
    }

    private static boolean isTeleport(MazeCell current, MazeCell next) {
        return !current.getNeighbours().contains(next);
    }
}
