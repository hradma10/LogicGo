package cz.logicgo.ui.renderers;


import cz.logicgo.core.gameClasses.bridge.Island;
import cz.logicgo.core.gameClasses.bridge.IslandBridge;
import cz.logicgo.core.entity.games.bridges.Bridge;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class BridgeRenderer {

    private static final Color BG_COLOR = Color.web("#1E1E24");
    private static final Color DEFAULT_LIGHT = Color.web("#E2E8F0");
    private static final Color COMPLETED_COLOR = Color.web("#10B981");
    private static final Color ERROR_COLOR = Color.web("#EF4444");

    private static final Color BG_COLOR_EXPORT = Color.WHITE;
    private static final Color DEFAULT_DARK_EXPORT = Color.web("#1E1E24");

    public static void render(Canvas canvas, Bridge bridge) {
        render(canvas, bridge, false);
    }

    public static void render(Canvas canvas, Bridge bridge, boolean forPrint) {


        GraphicsContext gc = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        Color background = forPrint ? BG_COLOR_EXPORT : BG_COLOR;
        gc.setFill(background);
        gc.fillRect(0, 0, width, height);
        if (bridge.getIslands() == null || bridge.getIslands().isEmpty()) return;

        double cellW = width / bridge.getWidth();
        double cellH = height / bridge.getHeight();

        double cellSize = Math.min(cellW, cellH);
        double lineWidth = Math.max(2.5, cellSize * 0.06);

        gc.setLineCap(StrokeLineCap.ROUND);

        for (IslandBridge islandBridge : bridge.getIslandBridges()) {
            drawBridge(gc, islandBridge, cellW, cellH, lineWidth, cellSize, forPrint);
        }

        for (Island island : bridge.getIslands()) {
            drawIsland(gc, island, cellW, cellH, lineWidth, bridge, forPrint);
        }

        if (forPrint) {
            gc.save();
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1.0);
            gc.strokeRect(0, 0, width, height);
            gc.restore();
        }
    }

    private static void drawBridge(GraphicsContext gc, IslandBridge bridge, double cellW, double cellH, double lineWidth, double cellSize, boolean forPrint) {
        double x1 = (bridge.getStartIsland().getCol() * cellW) + (cellW / 2);
        double y1 = (bridge.getStartIsland().getRow() * cellH) + (cellH / 2);
        double x2 = (bridge.getEndIsland().getCol() * cellW) + (cellW / 2);
        double y2 = (bridge.getEndIsland().getRow() * cellH) + (cellH / 2);

        int count = bridge.getBridgeCount();
        if (count <= 0) return;

        Color background = forPrint ? DEFAULT_DARK_EXPORT : DEFAULT_LIGHT;
        if (bridge.getLineColor() != null && !forPrint) {
            gc.save();
            gc.setStroke(Color.web(bridge.getLineColor()).deriveColor(0, 1, 1, 0.4));
            gc.setLineWidth(lineWidth * 2.5);
            renderBridgeLines(gc, count, x1, y1, x2, y2, lineWidth, cellSize);
            gc.restore();
        }
        Color lineColor = bridge.getLineColor() != null ? Color.web(bridge.getLineColor()) : background;
        gc.setStroke(lineColor);
        gc.setLineWidth(lineWidth);

        renderBridgeLines(gc, count, x1, y1, x2, y2, lineWidth, cellSize);

        if (count > 3) {
            drawBridgeLabel(gc, count, (x1 + x2) / 2, (y1 + y2) / 2, cellSize, lineColor, forPrint);
        }
    }

    private static void renderBridgeLines(GraphicsContext gc, int count, double x1, double y1, double x2, double y2, double lineWidth, double cellSize) {
        if (count == 1 || count > 3) {
            gc.strokeLine(x1, y1, x2, y2);
        } else {
            double dx = x2 - x1;
            double dy = y2 - y1;
            double length = Math.sqrt(dx * dx + dy * dy);

            double ux = -dy / length;
            double uy = dx / length;
            double offset = Math.max(lineWidth * 1.5, cellSize * 0.12);

            if (count == 2) {
                gc.strokeLine(x1 + ux * (offset * 0.5), y1 + uy * (offset * 0.5), x2 + ux * (offset * 0.5), y2 + uy * (offset * 0.5));
                gc.strokeLine(x1 - ux * (offset * 0.5), y1 - uy * (offset * 0.5), x2 - ux * (offset * 0.5), y2 - uy * (offset * 0.5));
            } else if (count == 3) {
                gc.strokeLine(x1, y1, x2, y2);
                gc.strokeLine(x1 + ux * offset, y1 + uy * offset, x2 + ux * offset, y2 + uy * offset);
                gc.strokeLine(x1 - ux * offset, y1 - uy * offset, x2 - ux * offset, y2 - uy * offset);
            }
        }
    }

    private static void drawBridgeLabel(GraphicsContext gc, int count, double mx, double my, double cellSize, Color color, boolean forPrint) {
        double labelSize = cellSize * 0.40;
        Color background = forPrint ? BG_COLOR_EXPORT : BG_COLOR;
        gc.setFill(background);
        gc.setStroke(color);
        gc.setLineWidth(1.5);
        gc.fillOval(mx - labelSize / 2, my - labelSize / 2, labelSize, labelSize);
        gc.strokeOval(mx - labelSize / 2, my - labelSize / 2, labelSize, labelSize);

        gc.setFill(color);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, labelSize * 0.9));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(String.valueOf(count), mx, my);
    }

    private static void drawIsland(GraphicsContext gc, Island island, double cellW, double cellH, double lineWidth, Bridge bridge, boolean forPrint) {
        double centerX = (island.getCol() * cellW) + (cellW / 2);
        double centerY = (island.getRow() * cellH) + (cellH / 2);
        double radius = Math.min(cellW, cellH) * 0.4;

        int createdBridges = bridge.getIslandBridges().stream()
                .filter(b -> b.getStartIsland() == island || b.getEndIsland() == island)
                .mapToInt(IslandBridge::getBridgeCount)
                .sum();

        int requiredBridges = island.getBridgeCount();
        Color statusColor = DEFAULT_LIGHT;
        if (forPrint) {
            statusColor = DEFAULT_DARK_EXPORT;
        } else {
            if (createdBridges == requiredBridges) {
                statusColor = COMPLETED_COLOR;
            } else if (createdBridges > requiredBridges) {
                statusColor = ERROR_COLOR;
            }
            if (island.getIslandColor() != null) {
                statusColor = Color.web(island.getIslandColor());
            }
        }

        Color background = forPrint ? BG_COLOR_EXPORT : BG_COLOR;
        gc.setFill(background);
        gc.setStroke(statusColor);
        gc.setLineWidth(lineWidth);
        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        gc.setFill(statusColor);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, radius * 1.2));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(String.valueOf(requiredBridges), centerX, centerY);
    }
}
