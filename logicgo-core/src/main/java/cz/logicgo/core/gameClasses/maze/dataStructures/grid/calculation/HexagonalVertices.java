package cz.logicgo.core.gameClasses.maze.dataStructures.grid.calculation;


import cz.logicgo.core.model.games.drawable.bounds.Point;

public record HexagonalVertices(Point fW, Point nW, Point nE, Point fE, Point N,
                                Point S) implements CellGridCalculation {
}


