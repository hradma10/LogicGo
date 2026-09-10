package cz.logicgo.core.gameClasses.maze.dataStructures.cell;


import cz.logicgo.core.misc.enums.gameTypes.maze.directions.RectangularDirection;

public non-sealed class RectangularCell extends MazeCell {

    public RectangularCell(int row, int col) {
        super(row, col, RectangularDirection.class);
    }

    private RectangularCell(int row, int col, boolean dummy) {
        super(row, col);
    }

    public static MazeCell makeDummyCell(int row, int col) {
        return new RectangularCell(row, col, true);
    }

    public RectangularCell(RectangularCell cell) {
        super(cell, RectangularDirection.class);
    }
}
