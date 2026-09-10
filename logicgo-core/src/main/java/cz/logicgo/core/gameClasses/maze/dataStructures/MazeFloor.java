package cz.logicgo.core.gameClasses.maze.dataStructures;


import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;

import java.util.Objects;

import static cz.logicgo.core.util.boardConverters.MazeConverters.deserializeSingleGrid;
import static cz.logicgo.core.util.boardConverters.MazeConverters.serializeSingleGrid;


public class MazeFloor {

    private int floorNumber;
    private byte[] gridBytes;
    private MazeGrid mazeGrid;

    private MazeFloor previousFloor;
    private MazeFloor nextFloor;

    public MazeFloor() {
    }

    public MazeFloor(MazeGrid mazeGrid, int floorNumber) {
        this.mazeGrid = mazeGrid;
        this.floorNumber = floorNumber;
        if (this.mazeGrid != null) {
            this.mazeGrid.setFloorNumber(floorNumber);
        }
    }

    public MazeFloor(MazeGrid mazeGrid) {
        this.mazeGrid = mazeGrid;
        if (mazeGrid != null) {
            this.floorNumber = mazeGrid.getFloorNumber();
        }
    }

    public void prepareForSave() {
        if (this.mazeGrid != null) {
            this.mazeGrid.setFloorNumber(this.floorNumber);
            this.gridBytes = serializeSingleGrid(this.mazeGrid);
        }
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public MazeFloor setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
        if (this.mazeGrid != null) {
            this.mazeGrid.setFloorNumber(this.floorNumber);
        }
        return this;
    }

    public byte[] getGridBytes() {
        if ((gridBytes == null || gridBytes.length == 0) && mazeGrid != null) {
            this.gridBytes = serializeSingleGrid(mazeGrid);
        }
        return gridBytes;
    }

    public void setGridBytes(byte[] gridBytes) {
        this.gridBytes = gridBytes;
    }

    public MazeGrid getMazeGrid() {
        if (mazeGrid == null && gridBytes != null && gridBytes.length > 0) {
            this.mazeGrid = deserializeSingleGrid(gridBytes);
            if (this.mazeGrid != null) {
                this.mazeGrid.setFloorNumber(this.floorNumber);
            }
        }
        return mazeGrid;
    }

    public MazeFloor setMazeGrid(MazeGrid mazeGrid) {
        this.mazeGrid = mazeGrid;
        if (this.mazeGrid != null) {
            this.mazeGrid.setFloorNumber(this.floorNumber);
            this.gridBytes = serializeSingleGrid(this.mazeGrid);
        }
        return this;
    }

    public MazeFloor getPreviousFloor() {
        return previousFloor;
    }

    public MazeFloor setPreviousFloor(MazeFloor previousFloor) {
        this.previousFloor = previousFloor;
        return this;
    }

    public MazeFloor getNextFloor() {
        return nextFloor;
    }

    public MazeFloor setNextFloor(MazeFloor nextFloor) {
        this.nextFloor = nextFloor;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MazeFloor mazeFloor)) return false;

        if (floorNumber != mazeFloor.floorNumber) return false;
        return Objects.equals(mazeGrid, mazeFloor.mazeGrid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(floorNumber, mazeGrid);
    }
}
