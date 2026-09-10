package cz.logicgo.core.gameClasses.maze.dataStructures.grid.calculation;


import cz.logicgo.core.model.games.drawable.bounds.Point;

public record RectangularVertices(Point topLeft, Point topRight, Point bottomLeft,
                                  Point bottomRight) implements CellGridCalculation {
}
