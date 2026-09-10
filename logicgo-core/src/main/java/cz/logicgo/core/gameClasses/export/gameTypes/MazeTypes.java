package cz.logicgo.core.gameClasses.export.gameTypes;


import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeAlgorithm;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;

import java.util.Map;

public record MazeTypes(
        Difficulty difficulty,
        int count,
        MazeType mazeType,
        MazeShape mazeShape,
        MazeAlgorithm mazeAlgorithm,
        Integer width,
        Integer height,
        boolean[][] mask,
        Map<MazeType, Integer> typeCounts
) implements GameMode {

    @Override
    public Difficulty getDifficulty() {
        return difficulty;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public TypeGame getTypeGame() {
        return TypeGame.MAZE;
    }
}
