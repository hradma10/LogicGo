package cz.logicgo.core.factoryInit.maze;


import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.factoryInit.GameInit;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeAlgorithm;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;

import java.util.EnumMap;

public class MazeInit extends GameInit {

    MazeType mazeType;
    MazeAlgorithm mazeAlgorithm;
    MazeShape mazeShape;
    int width;
    int height;

    private final EnumMap<MazeType, Integer> setElCount = new EnumMap<>(MazeType.class);

    private boolean hasMultipleFloors;
    private boolean[][] mask;
    private final EnumMap<MazeType, Integer> typeCount = new EnumMap<>(MazeType.class);

    public MazeInit(User player) {
        super(player, TypeGame.MAZE);
    }

    public MazeInit() {
        super(null, TypeGame.MAZE);
    }

    public MazeInit setTypeCount(MazeType type, Integer count) {
        this.typeCount.put(type, count);
        return this;
    }

    public EnumMap<MazeType, Integer> getTypeCount() {
        return typeCount;
    }

    public MazeInit(long id, User player) {
        super(id, player);
    }

    public MazeType getMazeType() {
        return mazeType;
    }

    public MazeInit setMazeType(MazeType mazeType) {
        this.mazeType = mazeType;
        return this;
    }

    public MazeAlgorithm getMazeAlgorithm() {
        return mazeAlgorithm;
    }

    public MazeInit setMazeAlgorithm(MazeAlgorithm mazeAlgorithm) {
        this.mazeAlgorithm = mazeAlgorithm;
        return this;
    }

    public MazeShape getMazeShape() {
        return mazeShape;
    }

    public MazeInit setMazeShape(MazeShape mazeShape) {
        this.mazeShape = mazeShape;
        return this;
    }

    @Override
    public MazeInit setId(Long id) {
        super.setId(id);
        return this;
    }

    @Override
    public MazeInit setDifficulty(Difficulty difficulty) {
        super.setDifficulty(difficulty);
        return this;
    }

    @Override
    public MazeInit setSeed(Long seed) {
        super.setSeed(seed);
        return this;
    }

    @Override
    public MazeInit setTypeGame(TypeGame typeGame) {
        super.setTypeGame(typeGame);
        return this;
    }

    public int getWidth() {
        return width;
    }

    public MazeInit setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public MazeInit setHeight(int height) {
        this.height = height;
        return this;
    }

    public boolean isHasMultipleFloors() {
        return hasMultipleFloors;
    }

    public MazeInit setHasMultipleFloors(boolean hasMultipleFloors) {
        this.hasMultipleFloors = hasMultipleFloors;
        return this;
    }

    public boolean[][] getMask() {
        return mask;
    }

    public MazeInit setMask(boolean[][] mask) {
        this.mask = mask;
        return this;
    }
}
