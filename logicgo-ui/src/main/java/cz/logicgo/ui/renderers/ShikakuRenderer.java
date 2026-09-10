package cz.logicgo.ui.renderers;


import cz.logicgo.core.gameClasses.shikaku.ShikakuCell;
import cz.logicgo.core.gameClasses.shikaku.ShikakuRectangle;
import cz.logicgo.core.entity.games.shikaku.Shikaku;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.List;

public class ShikakuRenderer {

    private static final Color BG_COLOR = Color.web("#F8FAFC");
    private static final Color GRID_COLOR = Color.web("#94A3B8");
    private static final Color GRID_PRINT_COLOR = Color.web("#475569");
    private static final Color TEXT_COLOR = Color.web("#0F172A");
    private static final Color NULL_CELL_COLOR = Color.web("#CBD5E1");

    private static final Color RECT_FILL = Color.web("#3B82F6", 0.15);
    private static final Color RECT_STROKE = Color.web("#2563EB");

    public static void render(Canvas canvas, Shikaku game) {
        render(canvas, game, false);
    }

    public static void renderHintsOverlay(Canvas canvas, Shikaku game, List<ShikakuRectangle> correctRects, List<ShikakuRectangle> wrongRects) {
        if (canvas == null || game == null) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        int cols = game.getWidth();
        int rows = game.getHeight();
        double cellW = canvas.getWidth() / cols;
        double cellH = canvas.getHeight() / rows;
        double cellSize = Math.min(cellW, cellH);

        gc.save();
        gc.setLineCap(StrokeLineCap.ROUND);

        for (ShikakuRectangle rect : correctRects) {
            drawHintRectangle(gc, rect, cellW, cellH, cellSize, Color.web("#38BDF8"));
        }

        for (ShikakuRectangle rect : wrongRects) {
            drawHintRectangle(gc, rect, cellW, cellH, cellSize, Color.web("#EF4444"));
        }

        gc.restore();
    }

    private static void drawHintRectangle(GraphicsContext gc, ShikakuRectangle rect, double cellW, double cellH, double cellSize, Color accentColor) {
        double padding = cellSize * 0.05;
        double rx = rect.getMinCol() * cellW + padding;
        double ry = rect.getMinRow() * cellH + padding;
        double rw = (rect.getMaxCol() - rect.getMinCol() + 1) * cellW - (padding * 2);
        double rh = (rect.getMaxRow() - rect.getMinRow() + 1) * cellH - (padding * 2);
        double cornerRadius = cellSize * 0.12;

        gc.setFill(accentColor.deriveColor(0, 1, 1, 0.2));
        gc.fillRoundRect(rx, ry, rw, rh, cornerRadius, cornerRadius);

        gc.setStroke(accentColor);
        gc.setLineWidth(Math.max(3.0, cellSize * 0.08));
        gc.strokeRoundRect(rx, ry, rw, rh, cornerRadius, cornerRadius);
    }

    public static void render(Canvas canvas, Shikaku game, boolean forPrint) {
        if (canvas == null || game == null) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        gc.setFill(forPrint ? Color.WHITE : BG_COLOR);
        gc.fillRect(0, 0, w, h);

        int cols = game.getWidth();
        int rows = game.getHeight();
        if (cols == 0 || rows == 0) return;

        double cellW = w / cols;
        double cellH = h / rows;
        double cellSize = Math.min(cellW, cellH);

        gc.setLineCap(StrokeLineCap.ROUND);
        gc.save();
        gc.setFill(forPrint ? Color.web("#E2E8F0") : NULL_CELL_COLOR);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (game.getCell(row, col) == null) {
                    gc.fillRect(col * cellW, row * cellH, cellW, cellH);
                }
            }
        }
        gc.restore();
        gc.setStroke(forPrint ? GRID_PRINT_COLOR : GRID_COLOR);
        gc.setLineWidth(2.0);
        for (int r = 1; r < rows; r++) {
            gc.strokeLine(0, r * cellH, w, r * cellH);
        }
        for (int c = 1; c < cols; c++) {
            gc.strokeLine(c * cellW, 0, c * cellW, h);
        }
        if (game.getRectangles() != null) {
            for (ShikakuRectangle rect : game.getRectangles()) {
                double padding = cellSize * 0.05;
                double rx = rect.getMinCol() * cellW + padding;
                double ry = rect.getMinRow() * cellH + padding;
                double rw = (rect.getMaxCol() - rect.getMinCol() + 1) * cellW - (padding * 2);
                double rh = (rect.getMaxRow() - rect.getMinRow() + 1) * cellH - (padding * 2);
                double cornerRadius = cellSize * 0.12;

                Color fill = rect.isHintWrong() ? Color.web("#EF4444", 0.25) : RECT_FILL;
                Color stroke = rect.isHintWrong() ? Color.web("#EF4444") : RECT_STROKE;

                gc.setFill(fill);
                gc.fillRoundRect(rx, ry, rw, rh, cornerRadius, cornerRadius);

                gc.setStroke(stroke);
                gc.setLineWidth(Math.max(2.0, cellSize * 0.05));
                gc.strokeRoundRect(rx, ry, rw, rh, cornerRadius, cornerRadius);
            }
        }

        gc.setFill(TEXT_COLOR);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        double fontSize = cellSize * 0.45;
        gc.setFont(Font.font("Arial", FontWeight.BOLD, fontSize));

        ShikakuCell[][] board = game.getBoard();
        if (board != null) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    ShikakuCell cell = board[r][c];
                    if (cell != null && cell.getClue() > 0) {
                        double textX = (c * cellW) + (cellW / 2);
                        double textY = (r * cellH) + (cellH / 2);
                        gc.fillText(String.valueOf(cell.getClue()), textX, textY);
                    }
                }
            }
        }

        gc.setStroke(Color.web("#475569"));
        gc.setLineWidth(2.5);
        gc.strokeRect(0, 0, w, h);
    }

    public static void drawPulsatingCell(Canvas canvas, double timeSeconds, int gridWidth, int gridHeight, ShikakuCell cell) {
        if (cell == null) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        double cellWidth = canvas.getWidth() / gridWidth;
        double cellHeight = canvas.getHeight() / gridHeight;
        double cellSize = Math.min(cellWidth, cellHeight);

        double opacity = 0.15 + 0.20 * (0.5 + 0.5 * Math.sin(timeSeconds * 3.5));
        gc.setFill(Color.web("#EF4444", opacity));

        double padding = cellSize * 0.05;
        double x = cell.getCol() * cellWidth + padding;
        double y = cell.getRow() * cellHeight + padding;
        double w = cellWidth - (padding * 2);
        double h = cellHeight - (padding * 2);
        double radius = cellSize * 0.12;

        gc.fillRoundRect(x, y, w, h, radius, radius);
    }

    public static void drawPulsatingRectangle(Canvas canvas, double timeSeconds, int gridWidth, int gridHeight, ShikakuRectangle rect) {
        if (rect == null) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        double cellWidth = canvas.getWidth() / gridWidth;
        double cellHeight = canvas.getHeight() / gridHeight;
        double cellSize = Math.min(cellWidth, cellHeight);

        double opacity = 0.15 + 0.20 * (0.5 + 0.5 * Math.sin(timeSeconds * 3.5));
        gc.setFill(Color.web("#EF4444", opacity));

        int minCol = rect.getMinCol();
        int maxCol = rect.getMaxCol();
        int minRow = rect.getMinRow();
        int maxRow = rect.getMaxRow();

        int colsCount = maxCol - minCol + 1;
        int rowsCount = maxRow - minRow + 1;

        double padding = cellSize * 0.05;
        double x = minCol * cellWidth + padding;
        double y = minRow * cellHeight + padding;
        double w = colsCount * cellWidth - (padding * 2);
        double h = rowsCount * cellHeight - (padding * 2);
        double radius = cellSize * 0.12;

        gc.fillRoundRect(x, y, w, h, radius, radius);
    }
}
