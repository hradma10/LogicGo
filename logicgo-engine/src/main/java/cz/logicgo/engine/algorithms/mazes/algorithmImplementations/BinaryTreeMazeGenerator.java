package cz.logicgo.engine.algorithms.mazes.algorithmImplementations;


import cz.logicgo.core.exceptions.parallel.LimitReachedException;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.misc.StepCounter;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.RectangularDirection;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.engine.context.MazeBuildContext;

import java.util.Random;

public class BinaryTreeMazeGenerator {

    public static void runOn(MazeGrid grid, Random random) throws ThreadTerminationException, LimitReachedException {
        runOn(grid, random, new MazeBuildContext(new StepCounter()));
    }

    public static void runOn(MazeGrid grid, Random random, MazeBuildContext context) throws ThreadTerminationException, LimitReachedException {
        if (grid.getMazeShape() != MazeShape.RECTANGULAR) return;
        for (MazeCell cell : grid.getFlattenedGrid()) {
            context.stepCounter().increment();

            context.checkContext();

            MazeCell north = cell.getNeighbourFromDirection(RectangularDirection.NORTH);
            MazeCell east = cell.getNeighbourFromDirection(RectangularDirection.EAST);

            int count = 0;
            if (north != null) count++;
            if (east != null) count++;

            if (count == 0) continue;

            int choice = random.nextInt(count);
            MazeCell chosen = (choice == 0 && north != null) ? north : east;
            cell.link(chosen);
        }
    }
}
