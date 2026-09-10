package cz.logicgo.core.gameClasses.maze.dataStructures.cell;

import cz.logicgo.core.misc.enums.gameTypes.maze.LevelType;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.MazeDirection;
import cz.logicgo.core.gameClasses.maze.dataStructures.StartAndEnd;
import cz.logicgo.core.gameClasses.maze.dataStructures.maps.NeighbourMap;
import cz.logicgo.core.gameClasses.maze.dataStructures.maps.OneNeighbourMap;

import java.util.*;

public sealed abstract class MazeCell permits HexagonalCell, RectangularCell {
    final private int row;
    final private int col;
    private final NeighbourMap neighbouringCells;
    final private HashSet<MazeCell> linked;
    private StartAndEnd startAndEnd;
    private int markCount = 0;
    private LevelType levelType = LevelType.NONE;


    public MazeCell(int row, int col, Class<? extends MazeDirection> directionType) {
        this.row = row;
        this.col = col;
        neighbouringCells = new OneNeighbourMap(directionType);
        linked = new LinkedHashSet<>();
    }

    protected MazeCell(int row, int col) {
        this.row = row;
        this.col = col;
        neighbouringCells = null;
        linked = null;
    }

    protected MazeCell(MazeCell cell, Class<? extends MazeDirection> directionType) {
        this.row = cell.row;
        this.col = cell.col;
        neighbouringCells = new OneNeighbourMap(directionType);
        linked = new LinkedHashSet<>();
    }


    public void link(MazeCell link) {
        linked.add(link);
        link.linkFromNeighbour(this);
    }

    public MazeCell getNeighbourFromDirection(MazeDirection direction) {
        List<MazeCell> res = neighbouringCells.get(direction);
        if (res == null || res.isEmpty()) return null;
        return res.getFirst();
    }

    public List<MazeCell> getNeighboursFromDirection(MazeDirection direction) {
        return neighbouringCells.get(direction);
    }

    public void put(MazeDirection direction, MazeCell cell) {
        neighbouringCells.put(direction, cell);
    }

    public void linkFromNeighbour(MazeCell neighbour) {
        linked.add(neighbour);
    }

    public void unlink(MazeCell link) {
        linked.remove(link);
        link.unlinkFromNeighbour(this);
    }

    public void unlinkFromNeighbour(MazeCell neighbour) {
        linked.remove(neighbour);
    }

    public HashSet<MazeCell> getLinked() {
        return linked;
    }

    public boolean isLinked(MazeCell link) {
        return linked.contains(link);
    }

    public List<MazeCell> getNeighbours() {
        List<MazeCell> list = new ArrayList<>(neighbouringCells.valueSet());
        list.sort(Comparator.comparingInt(MazeCell::getRow)
                .thenComparingInt(MazeCell::getCol));
        return list;
    }

    public List<MazeDirection> getNeighbourDirections() {
        return List.of(neighbouringCells.keySet().toArray(new MazeDirection[0]));
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof MazeCell mazeCell)) return false;

        return this.row == mazeCell.row && this.col == mazeCell.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    public NeighbourMap getNeighbouringCells() {
        return neighbouringCells;
    }

    public StartAndEnd getStartAndEnd() {
        return startAndEnd;
    }

    public void setStartAndEnd(StartAndEnd startAndEnd) {
        this.startAndEnd = startAndEnd;
    }

    public boolean notSkipWall(MazeDirection dir, MazeCell cell) {
        if (startAndEnd == null) return true;

        MazeCell startCell = startAndEnd.startCell();
        MazeCell endCell = startAndEnd.endCell();
        MazeDirection startDirection = startAndEnd.startDirection();
        MazeDirection endDirection = startAndEnd.endDirection();

        if (startCell != null && cell.getRow() == startCell.getRow() && startCell.getCol() == cell.getCol() && dir == startDirection)
            return false;

        if (endCell != null) {
            return cell.getRow() != endCell.getRow() || endCell.getCol() != cell.getCol() || dir != endDirection;
        }

        return true;
    }

    public StatusCell getStartOrEndStatus(MazeDirection dir) {
        if (startAndEnd == null) return StatusCell.NONE;

        MazeCell startCell = startAndEnd.startCell();
        MazeCell endCell = startAndEnd.endCell();

        if (startCell == null || endCell == null) return StatusCell.NONE;

        if (this == startCell && dir == startAndEnd.startDirection()) {
            return StatusCell.START;
        }

        if (this == endCell && dir == startAndEnd.endDirection()) {
            return StatusCell.END;
        }

        return StatusCell.NONE;
    }

    public boolean isXMarked() {
        return markCount > 0;
    }

    public int getMarkCount() {
        return markCount;
    }

    public MazeCell setMarkCount(int markCount) {
        this.markCount = markCount;
        return this;
    }

    public void addMark() {
        markCount++;
    }

    public void removeMark() {
        markCount--;
        if (markCount < 0) markCount = 0;
    }

    public LevelType getLevelType() {
        return levelType;
    }

    public MazeCell setLevelType(LevelType levelType) {
        this.levelType = levelType;
        return this;
    }

    public enum StatusCell {
        START, END, NONE
    }
}
