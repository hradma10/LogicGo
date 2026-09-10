package cz.logicgo.core.gameClasses.favorites;

import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeAlgorithm;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Objects;

public final class MazeFavorite extends GameFavorite {

    private MazeType mazeType;
    private MazeShape mazeShape;
    private boolean[][] mask;
    private MazeAlgorithm mazeAlgorithm;
    private boolean hasMultipleFloors;
    private int floorCount;

    @Override
    public TypeGame getTypeGame() {
        return TypeGame.MAZE;
    }

    private final EnumMap<MazeType, Integer> setElCount = new EnumMap<>(MazeType.class);

    final private EnumMap<MazeType, Integer> typeCount = new EnumMap<>(MazeType.class);

    public MazeFavorite() {
        super();
    }

    public MazeShape getMazeShape() {
        return mazeShape;
    }

    public void setMazeShape(MazeShape mazeShape) {
        this.mazeShape = mazeShape;
    }

    public MazeAlgorithm getMazeAlgorithm() {
        return mazeAlgorithm;
    }

    public void setMazeAlgorithm(MazeAlgorithm mazeAlgorithm) {
        this.mazeAlgorithm = mazeAlgorithm;
    }

    public boolean isHasMultipleFloors() {
        return hasMultipleFloors;
    }

    public int getFloorCount() {
        return floorCount;
    }

    public MazeFavorite setFloorCount(int floorCount) {
        this.floorCount = floorCount;
        return this;
    }

    public void setHasMultipleFloors(boolean hasMultipleFloors) {
        this.hasMultipleFloors = hasMultipleFloors;
    }

    public boolean[][] getMask() {
        return mask;
    }

    public MazeFavorite setMask(boolean[][] mask) {
        this.mask = mask;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MazeFavorite that)) return false;
        if (!super.equals(o)) return false;
        return isHasMultipleFloors() == that.isHasMultipleFloors() &&
                getMazeType() == that.getMazeType() &&
                getMazeShape() == that.getMazeShape() &&
                getMazeAlgorithm() == that.getMazeAlgorithm() &&
                Objects.deepEquals(getMask(), that.getMask()) &&
                Objects.equals(getTypeCount(), that.getTypeCount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                getMazeType(),
                getMazeShape(),
                Arrays.deepHashCode(getMask()),
                getMazeAlgorithm(),
                isHasMultipleFloors(),
                getTypeCount()

        );
    }


    public MazeType getMazeType() {
        return mazeType;
    }

    public MazeFavorite setMazeType(MazeType mazeType) {
        this.mazeType = mazeType;
        return this;
    }

    public EnumMap<MazeType, Integer> getTypeCount() {
        return typeCount;
    }

    public EnumMap<MazeType, Integer> getSetElCount() {
        return setElCount;
    }
}
