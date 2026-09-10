package cz.logicgo.core.gameClasses.maze.dataStructures.grid;


import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.MazeDirection;
import cz.logicgo.core.gameClasses.maze.dataStructures.StartAndEnd;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.HexagonalCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.RectangularCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.gameModes.MazeModifier;
import cz.logicgo.core.gameClasses.maze.dataStructures.helpers.MazeGridConstructor;
import cz.logicgo.core.gameClasses.maze.dataStructures.path.PlayerPath;

import java.util.*;
import java.util.stream.Collectors;

import static cz.logicgo.core.gameClasses.maze.MazeUtils.insertNeighborsFromOldGrid;
import static cz.logicgo.core.gameClasses.maze.MazeUtils.makeEmptyMask;

public sealed abstract class MazeGrid permits HexagonalGrid, RectangularGrid {
    protected final int rowCount;
    protected final int colCount;
    protected MazeCell[][] grid;
    protected final MazeShape mazeShape;
    protected MazeType mazeType;
    protected MazeCell startCell;
    protected MazeCell endCell;
    protected MazeDirection startDirection;
    protected MazeDirection endDirection;
    protected final Set<MazeModifier> modifiers = new HashSet<>();
    protected PlayerPath path = new PlayerPath();
    protected Set<MazeCell> triedWays = new HashSet<>();
    protected int[][] mask;
    protected int floorNumber = 0;
    protected MazeGrid previousGrid = null;
    protected MazeGrid nextGrid = null;

    public static final int CELL_SIZE = 30;

    public MazeGrid getPreviousGrid() {
        return previousGrid;
    }

    public MazeGrid setPreviousGrid(MazeGrid previousGrid) {
        this.previousGrid = previousGrid;
        return this;
    }

    public MazeGrid setMazeType(MazeType mazeType) {
        this.mazeType = mazeType;
        return this;
    }

    public MazeGrid getNextGrid() {
        return nextGrid;
    }

    public MazeGrid setTriedWays(Set<MazeCell> triedWays) {
        this.triedWays = triedWays;
        return this;
    }

    public MazeGrid setNextGrid(MazeGrid nextGrid) {
        this.nextGrid = nextGrid;
        return this;
    }

    public MazeDirection getEndDirection() {
        return endDirection;
    }

    public int[][] getMask() {
        return mask;
    }

    public MazeGrid setGrid(MazeCell[][] grid) {
        this.grid = grid;
        return this;
    }

    public MazeGrid setMask(int[][] mask) {
        this.mask = mask;
        return this;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public MazeGrid setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
        return this;
    }

    public Set<MazeCell> getTriedWays() {
        return triedWays;
    }

    public PlayerPath getPath() {
        return path;
    }

    public MazeType getMazeType() {
        return mazeType;
    }

    public MazeGrid setEndDirection(MazeDirection endDirection) {
        this.endDirection = endDirection;
        return this;
    }

    abstract void configureCells(byte[] directions);

    public MazeDirection getStartDirection() {
        return startDirection;
    }

    public MazeGrid setStartDirection(MazeDirection startDirection) {
        this.startDirection = startDirection;
        return this;
    }

    public MazeCell[][] getGrid() {
        return grid;
    }


    public MazeGrid setPath(PlayerPath path) {
        this.path = path;
        return this;
    }


    protected MazeGrid(MazeGrid grid) {
        this.rowCount = grid.getRowCount();
        this.colCount = grid.getColCount();
        this.mazeShape = grid.getMazeShape();
        this.mazeType = grid.getMazeType();
        this.mask = Arrays.stream(grid.getMask())
                .map(int[]::clone)
                .toArray(int[][]::new);
        this.startDirection = grid.getStartDirection();
        this.endDirection = grid.getEndDirection();
        this.floorNumber = grid.getFloorNumber();

        MazeCell[][] newGrid = new MazeCell[rowCount][colCount];
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                MazeCell oldCell = grid.getCell(r, c);
                if (oldCell == null) continue;
                MazeCell newCell = switch (oldCell) {
                    case HexagonalCell hCell -> new HexagonalCell(hCell);
                    case RectangularCell rCell -> new RectangularCell(rCell);
                };
                newGrid[r][c] = newCell;

            }
        }

        this.grid = newGrid;

        insertNeighborsFromOldGrid(grid, newGrid);

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                MazeCell oldCell = grid.getCell(r, c);
                if (oldCell == null) continue;
                MazeCell newCell = newGrid[r][c];
                for (MazeCell link : oldCell.getLinked()) {
                    int row = link.getRow();
                    int col = link.getCol();
                    MazeCell neighborInNew = newGrid[row][col];
                    neighborInNew.link(newCell);
                }
            }
        }


        int startRow = grid.getStartCell().getRow();
        int startCol = grid.getStartCell().getCol();

        int endRow = grid.getEndCell().getRow();
        int endCol = grid.getEndCell().getCol();

        this.startCell = newGrid[startRow][startCol];
        this.endCell = newGrid[endRow][endCol];

        StartAndEnd sAE = new StartAndEnd(this.startCell, this.startDirection, this.endCell, this.endDirection);
        this.getFlattenedGrid().stream().filter(Objects::nonNull).forEach(cell -> cell.setStartAndEnd(sAE));

        List<MazeModifier> newModifiers = new ArrayList<>();
        for (MazeModifier mod : grid.getModifiers()) {
            newModifiers.add(mod.copy(newGrid));
        }

        this.modifiers.addAll(newModifiers);

        PlayerPath copy = grid.getPath().copy(newGrid);

        this.path.getSolutionPath().addAll(copy.getSolutionPath());
        this.path.getActivePath().addAll(copy.getActivePath());
        this.path.getFullHistory().addAll(copy.getFullHistory());

    }

    protected MazeGrid(MazeGridConstructor builder) {
        this.rowCount = builder.getRowCount();
        this.colCount = builder.getColCount();
        this.mazeShape = builder.getMazeShape();
        this.mask = builder.getMask() != null ? builder.getMask() : makeEmptyMask(rowCount, colCount);

        if (builder.getModifiers() != null) this.modifiers.addAll(builder.getModifiers());

        this.grid = builder.getGrid();
        this.mazeType = builder.getMazeType();
        this.startCell = builder.getStartCell();
        this.endCell = builder.getEndCell();
        this.startDirection = builder.getStartDirection();
        this.endDirection = builder.getEndDirection();
    }

    public void move(MazeCell cell) {
        path.move(cell);
    }

    protected MazeCell[][] initCells(int rowCount, int colCount) {
        MazeCell[][] grid = new MazeCell[rowCount][colCount];
        int[][] mask = getMask();

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                if (mask != null && i < mask.length && j < mask[i].length && mask[i][j] < 0) {
                    continue;
                }

                if (mazeShape == MazeShape.HEXAGONAL) {
                    grid[i][j] = new HexagonalCell(i, j);
                } else {
                    grid[i][j] = new RectangularCell(i, j);
                }
            }
        }
        return grid;
    }

    protected abstract void configureCells();

    public Set<MazeModifier> getModifiers() {
        return modifiers;
    }

    public List<MazeCell> getFlattenedGrid() {
        List<MazeCell> cells = new ArrayList<>();
        for (MazeCell[] row : grid) {
            Collections.addAll(cells, row);
        }
        return cells;
    }

    public MazeCell getCell(int row, int col) {
        if (row < 0 || col < 0 || row >= rowCount || col >= colCount) {
            return null;
        }
        return grid[row][col];
    }

    public int size() {
        return rowCount * colCount;
    }

    public List<MazeCell> getRow(int row) {
        return List.of(grid[row]);
    }

    public List<List<MazeCell>> getRows() {
        return Arrays.stream(grid).map(List::of).collect(Collectors.toList());
    }

    public List<MazeCell> getColumn(int col) {
        MazeCell[] colArray = new MazeCell[colCount];
        for (int i = 0; i < colCount; i++) {
            colArray[i] = grid[i][col];
        }
        return List.of(colArray);
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColCount() {
        return colCount;
    }

    public MazeShape getMazeShape() {
        return mazeShape;
    }

    public MazeCell getStartCell() {
        return startCell;
    }

    public MazeCell getEndCell() {
        return endCell;
    }

    public void setStartCell(MazeCell cell) {
        this.startCell = cell;
    }

    public void setEndCell(MazeCell cell) {
        this.endCell = cell;
    }

    public MazeCell getRandomCell(Random random) {
        int row = random.nextInt(rowCount);
        int col = random.nextInt(colCount);
        return grid[row][col];
    }

    public MazeCell getRandomCell() {
        return getRandomCell(new Random());
    }

    boolean notSkipWall(MazeDirection dir, MazeCell cell) {
        if (cell.getRow() == startCell.getRow() && startCell.getCol() == cell.getCol() && dir == startDirection)
            return false;
        return cell.getRow() != endCell.getRow() || endCell.getCol() != cell.getCol() || dir != endDirection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MazeGrid mazeGrid)) return false;

        if (rowCount != mazeGrid.rowCount) return false;
        if (colCount != mazeGrid.colCount) return false;
        if (floorNumber != mazeGrid.floorNumber) return false;
        if (mazeShape != mazeGrid.mazeShape) return false;
        if (mazeType != mazeGrid.mazeType) return false;
        if (startDirection != mazeGrid.startDirection) return false;
        if (endDirection != mazeGrid.endDirection) return false;

        boolean startMatch = (startCell == null && mazeGrid.startCell == null) ||
                (startCell != null && mazeGrid.startCell != null &&
                        startCell.getRow() == mazeGrid.startCell.getRow() &&
                        startCell.getCol() == mazeGrid.startCell.getCol());
        if (!startMatch) return false;

        boolean endMatch = (endCell == null && mazeGrid.endCell == null) ||
                (endCell != null && mazeGrid.endCell != null &&
                        endCell.getRow() == mazeGrid.endCell.getRow() &&
                        endCell.getCol() == mazeGrid.endCell.getCol());
        if (!endMatch) return false;

        if (!Arrays.deepEquals(mask, mazeGrid.mask)) return false;

        return Arrays.deepEquals(grid, mazeGrid.grid);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(rowCount, colCount, mazeShape, mazeType, startDirection, endDirection, floorNumber);
        result = 31 * result + Arrays.deepHashCode(mask);
        result = 31 * result + Arrays.deepHashCode(grid);
        return result;
    }
}
