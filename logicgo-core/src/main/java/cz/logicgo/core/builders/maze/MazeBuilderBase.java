package cz.logicgo.core.builders.maze;


import cz.logicgo.core.builders.GameCreation;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeAlgorithm;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;

import java.util.List;

public abstract sealed class MazeBuilderBase<T extends MazeBuilderBase<T>> extends GameCreation<T> permits MazeCreation {

    private MazeType mazeType;
    private MazeAlgorithm mazeAlgorithm;
    private MazeShape mazeShape;
    private int[][] mask;
    private boolean customMask;
    private int floorCount;
    private List<MazeType> mazeTypes;
    private List<Integer> counts;

    public MazeBuilderBase() {
        this.setTypeGame(TypeGame.MAZE);
    }

    public MazeType getMazeType() {
        return mazeType;
    }

    public T setMazeType(MazeType mazeType) {
        this.mazeType = mazeType;
        return self();
    }

    public MazeAlgorithm getMazeAlgorithm() {
        return mazeAlgorithm;
    }

    public T setMazeAlgorithm(MazeAlgorithm mazeAlgorithm) {
        this.mazeAlgorithm = mazeAlgorithm;
        return self();
    }

    public MazeShape getMazeShape() {
        return mazeShape;
    }

    public T setMazeShape(MazeShape mazeShape) {
        this.mazeShape = mazeShape;
        return self();
    }

    public int[][] getMask() {
        return mask;
    }

    public T setMask(int[][] mask) {
        this.mask = mask;
        return self();
    }

    public int getFloorCount() {
        return floorCount;
    }

    public T setFloorCount(int floorCount) {
        this.floorCount = floorCount;
        return self();
    }

    public List<MazeType> getMazeTypes() {
        return mazeTypes;
    }

    public T setMazeTypes(List<MazeType> mazeTypes) {
        this.mazeTypes = mazeTypes;
        return self();
    }

    public List<Integer> getCounts() {
        return counts;
    }

    public T setCounts(List<Integer> counts) {
        this.counts = counts;
        return self();
    }

    public boolean isCustomMask() {
        return customMask;
    }

    public MazeBuilderBase<T> setCustomMask(boolean customMask) {
        this.customMask = customMask;
        return this;
    }
}
