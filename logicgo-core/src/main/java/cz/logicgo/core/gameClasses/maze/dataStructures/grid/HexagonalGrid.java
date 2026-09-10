package cz.logicgo.core.gameClasses.maze.dataStructures.grid;


import cz.logicgo.core.gameClasses.maze.dataStructures.cell.HexagonalCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.helpers.MazeGridConstructor;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.HexagonalDirection;
import cz.logicgo.core.util.boardConverters.MazeConverters;

import static cz.logicgo.core.misc.enums.gameTypes.maze.directions.HexagonalDirection.*;
import static cz.logicgo.core.util.boardConverters.MazeConverters.FLAG_EMPTY;
import static cz.logicgo.core.util.boardConverters.MazeConverters.returnMaskType;

public non-sealed class HexagonalGrid extends MazeGrid {

    public HexagonalGrid(MazeGridConstructor builder) {
        super(builder.setMazeShape(MazeShape.HEXAGONAL));

        if (this.grid == null) {
            this.grid = initCells(rowCount, colCount);
        }

        if (builder.getDirections() != null) {
            configureCells(builder.getDirections());
        } else {
            configureCells();
        }
    }

    public HexagonalGrid(HexagonalGrid grid) {
        super(grid);
    }

    @Override
    void configureCells(byte[] directions) {
        int index = 0;
        for (MazeCell[] row : grid) {
            for (MazeCell cell : row) {
                byte dirs = directions[index++];

                if (cell == null || dirs == FLAG_EMPTY) continue;

                linkNeighbors(cell);
                for (var dir : HexagonalDirection.values()) {
                    if ((dirs & dir.getMask()) != 0) {
                        MazeCell neighbour = cell.getNeighbourFromDirection(dir);
                        if (neighbour != null) cell.link(neighbour);
                    }
                }
            }
        }
    }

    @Override
    protected MazeCell[][] initCells(int rowCount, int colCount) {
        MazeCell[][] grid = new MazeCell[rowCount][colCount];
        int[][] mask = getMask();
        MazeConverters.MaskType type = returnMaskType(mask);

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                switch (type) {
                    case BOOL -> {
                        if (mask[i][j] == 1) {
                            grid[i][j] = new HexagonalCell(i, j);
                        }
                    }
                    case SMALL, BIG, ALL, NONE -> {
                        grid[i][j] = new HexagonalCell(i, j);
                    }
                }
            }
        }
        return grid;
    }


    @Override
    protected void configureCells() {
        for (MazeCell cell : getFlattenedGrid()) {
            linkNeighbors(cell);
        }
    }

    private void linkNeighbors(MazeCell cell) {
        if (cell == null) return;
        int row = cell.getRow();
        int col = cell.getCol();
        int northDiagonal, southDiagonal;

        if (col % 2 == 0) {
            northDiagonal = row - 1;
            southDiagonal = row;
        } else {
            northDiagonal = row;
            southDiagonal = row + 1;
        }

        int maxRow = grid.length - 1;
        int maxCol = grid[0].length - 1;

        if (northDiagonal >= 0 && col - 1 >= 0 && grid[northDiagonal][col - 1] != null)
            cell.put(NORTH_WEST, grid[northDiagonal][col - 1]);
        if (row - 1 >= 0 && grid[row - 1][col] != null)
            cell.put(NORTH, grid[row - 1][col]);
        if (northDiagonal >= 0 && col + 1 <= maxCol && grid[northDiagonal][col + 1] != null)
            cell.put(NORTH_EAST, grid[northDiagonal][col + 1]);
        if (southDiagonal <= maxRow && col - 1 >= 0 && grid[southDiagonal][col - 1] != null)
            cell.put(SOUTH_WEST, grid[southDiagonal][col - 1]);
        if (row + 1 <= maxRow && grid[row + 1][col] != null)
            cell.put(SOUTH, grid[row + 1][col]);
        if (southDiagonal <= maxRow && col + 1 <= maxCol && grid[southDiagonal][col + 1] != null)
            cell.put(SOUTH_EAST, grid[southDiagonal][col + 1]);
    }
}
