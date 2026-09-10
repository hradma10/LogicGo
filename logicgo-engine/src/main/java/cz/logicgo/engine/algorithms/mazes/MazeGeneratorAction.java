package cz.logicgo.engine.algorithms.mazes;

import cz.logicgo.core.exceptions.parallel.LimitReachedException;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.engine.context.MazeBuildContext;

import java.util.Random;

@FunctionalInterface
public interface MazeGeneratorAction {
    void runOn(MazeGrid grid, Random random, MazeBuildContext context)
            throws ThreadTerminationException, LimitReachedException;
}
