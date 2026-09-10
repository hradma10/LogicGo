package cz.logicgo.core.gameClasses.maze.dataStructures.helpers;


import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.gameModes.MazeModifier;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.MazeDirection;

import java.util.List;
import java.util.Random;

public class MazeGridConstructor {

    private int rowCount;
    private int colCount;
    private MazeCell[][] grid;
    private MazeShape mazeShape;
    private MazeType mazeType;
    private List<MazeModifier> modifiers;
    private MazeCell startCell;
    private MazeCell endCell;
    private MazeDirection startDirection;
    private MazeDirection endDirection;
    private Random randomInstance;
    private int[][] mask;
    private byte[] directions;

    public int getRowCount() {
        return rowCount;
    }

    public MazeGridConstructor setRowCount(int rowCount) {
        this.rowCount = rowCount;
        return this;
    }

    public int getColCount() {
        return colCount;
    }

    public MazeGridConstructor setColCount(int colCount) {
        this.colCount = colCount;
        return this;
    }

    public MazeCell[][] getGrid() {
        return grid;
    }

    public MazeGridConstructor setGrid(MazeCell[][] grid) {
        this.grid = grid;
        return this;
    }

    public MazeShape getMazeShape() {
        return mazeShape;
    }

    public MazeGridConstructor setMazeShape(MazeShape mazeShape) {
        this.mazeShape = mazeShape;
        return this;
    }

    public MazeType getMazeType() {
        return mazeType;
    }

    public MazeGridConstructor setMazeType(MazeType mazeType) {
        this.mazeType = mazeType;
        return this;
    }

    public List<MazeModifier> getModifiers() {
        return modifiers;
    }

    public MazeGridConstructor setModifiers(List<MazeModifier> modifiers) {
        this.modifiers = modifiers;
        return this;
    }

    public MazeCell getStartCell() {
        return startCell;
    }

    public MazeGridConstructor setStartCell(MazeCell startCell) {
        this.startCell = startCell;
        return this;
    }

    public MazeCell getEndCell() {
        return endCell;
    }

    public MazeGridConstructor setEndCell(MazeCell endCell) {
        this.endCell = endCell;
        return this;
    }

    public MazeDirection getStartDirection() {
        return startDirection;
    }

    public MazeGridConstructor setStartDirection(MazeDirection startDirection) {
        this.startDirection = startDirection;
        return this;
    }

    public MazeDirection getEndDirection() {
        return endDirection;
    }

    public MazeGridConstructor setEndDirection(MazeDirection endDirection) {
        this.endDirection = endDirection;
        return this;
    }

    public Random getRandomInstance() {
        return randomInstance;
    }

    public MazeGridConstructor setRandomInstance(Random randomInstance) {
        this.randomInstance = randomInstance;
        return this;
    }

    public int[][] getMask() {
        return mask;
    }

    public MazeGridConstructor setMask(int[][] mask) {
        this.mask = mask;
        return this;
    }

    public byte[] getDirections() {
        return directions;
    }

    public MazeGridConstructor setDirections(byte[] directions) {
        this.directions = directions;
        return this;
    }
}
