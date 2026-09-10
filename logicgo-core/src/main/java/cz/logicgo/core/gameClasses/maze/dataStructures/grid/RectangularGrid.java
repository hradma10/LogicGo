package cz.logicgo.core.gameClasses.maze.dataStructures.grid;


import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.RectangularCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.helpers.MazeGridConstructor;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.RectangularDirection;

import static cz.logicgo.core.util.boardConverters.MazeConverters.*;

public non-sealed class RectangularGrid extends MazeGrid {

    public RectangularGrid(MazeGridConstructor builder) {
        super(builder.setMazeShape(MazeShape.RECTANGULAR));


        if (this.grid == null) {
            this.mask = builder.getMask();
            this.grid = initCells(rowCount, colCount);
        }

        if (builder.getDirections() != null) {
            configureCells(builder.getDirections());
        } else {
            configureCells();
        }
    }

    public RectangularGrid(RectangularGrid grid) {
        super(grid);
    }


    @Override
    protected MazeCell[][] initCells(int rowCount, int colCount) {
        MazeCell[][] grid = new MazeCell[rowCount][colCount];
        int[][] mask = getMask();
        MaskType type = returnMaskType(mask);

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                switch (type) {
                    case BOOL -> {
                        if (mask[i][j] == 1) {
                            grid[i][j] = new RectangularCell(i, j);
                        }
                    }
                    case SMALL, BIG, ALL, NONE -> {
                        grid[i][j] = new RectangularCell(i, j);
                    }
                }
            }
        }
        return grid;
    }


    @Override
    void configureCells(byte[] directions) {
        int index = 0;
        for (MazeCell[] row : grid) {
            for (MazeCell cell : row) {
                byte dirs = directions[index++];

                if (cell == null || dirs == FLAG_EMPTY) continue;

                linkNeighbors(cell);
                for (var dir : RectangularDirection.values()) {
                    if ((dirs & dir.getMask()) != 0) {
                        MazeCell neighbour = cell.getNeighbourFromDirection(dir);
                        if (neighbour != null) cell.link(neighbour);
                    }
                }
            }
        }
    }

    private void linkNeighbors(MazeCell cell) {
        int cellRow = cell.getRow();
        int cellCol = cell.getCol();

        if (cellRow > 0 && grid[cellRow - 1][cellCol] != null)
            cell.put(RectangularDirection.NORTH, grid[cellRow - 1][cellCol]);
        if (cellRow < rowCount - 1 && grid[cellRow + 1][cellCol] != null)
            cell.put(RectangularDirection.SOUTH, grid[cellRow + 1][cellCol]);
        if (cellCol > 0 && grid[cellRow][cellCol - 1] != null)
            cell.put(RectangularDirection.WEST, grid[cellRow][cellCol - 1]);
        if (cellCol < colCount - 1 && grid[cellRow][cellCol + 1] != null)
            cell.put(RectangularDirection.EAST, grid[cellRow][cellCol + 1]);
    }


    @Override
    public void configureCells() {
        for (MazeCell[] row : grid) {
            for (MazeCell cell : row) {
                if (cell == null) continue;
                linkNeighbors(cell);
            }
        }

    }
}
