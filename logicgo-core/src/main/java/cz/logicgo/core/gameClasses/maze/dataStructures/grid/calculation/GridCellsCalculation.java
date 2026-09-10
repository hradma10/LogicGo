package cz.logicgo.core.gameClasses.maze.dataStructures.grid.calculation;


import cz.logicgo.core.model.games.drawable.bounds.Point;

public class GridCellsCalculation {

    public static HexagonalVertices getHexagonVertices(int row, int col, double width, double height) {
        double cx = width / 2.0 + (col * 0.75 * width);
        double cy = height / 2.0 + (row * height);

        if (col % 2 != 0) {
            cy += height / 2.0;
        }

        double halfW = width / 2.0;
        double quarterW = width / 4.0;
        double halfH = height / 2.0;
        double xFw = cx - halfW;
        double xNw = cx - quarterW;
        double xNe = cx + quarterW;
        double xFe = cx + halfW;

        double yN = cy - halfH;
        double yM = cy;
        double yS = cy + halfH;

        return new HexagonalVertices(
                new Point(xFw, yM),
                new Point(xNw, yN),
                new Point(xNe, yN),
                new Point(xFe, yM),
                new Point(xNe, yS),
                new Point(xNw, yS));
    }

    public static RectangularVertices getCellGeometry(int row, int col, double width, double height) {
        double x1 = col * width;
        double y1 = row * height;
        double x2 = x1 + width;
        double y2 = y1 + height;

        return new RectangularVertices(
                new Point(x1, y1),
                new Point(x2, y1),
                new Point(x1, y2),
                new Point(x2, y2)
        );
    }
}
