package cz.logicgo.core.gameClasses.maze.dataStructures.cell;


import cz.logicgo.core.misc.enums.gameTypes.maze.directions.HexagonalDirection;

public non-sealed class HexagonalCell extends MazeCell {

    public HexagonalCell(int row, int col) {
        super(row, col, HexagonalDirection.class);
    }

    public HexagonalCell(HexagonalCell cell) {
        super(cell, HexagonalDirection.class);
    }

    private HexagonalCell(int row, int col, boolean dummy) {
        super(row, col);
    }

    public static MazeCell makeDummyCell(int row, int col) {
        return new HexagonalCell(row, col, true);
    }
}
