package cz.logicgo.engine.algorithms.mazes.algorithmImplementations;

import cz.logicgo.core.exceptions.parallel.LimitReachedException;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.misc.StepCounter;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.core.gameClasses.maze.MazeUtils;
import cz.logicgo.engine.context.MazeBuildContext;

import java.util.List;
import java.util.Random;
import java.util.Stack;

public class PrimMazeGenerator {
    public static void runOn(MazeGrid grid, Random random) throws ThreadTerminationException, LimitReachedException {
        runOn(grid, random, new MazeBuildContext(new StepCounter()));
    }

    public static void runOn(MazeGrid grid, Random random, MazeBuildContext context) throws ThreadTerminationException, LimitReachedException {
        MazeCell start = null;
        while (start == null) {
            start = grid.getRandomCell(random);
        }

        Stack<MazeCell> active = new Stack<>();
        active.push(start);

        while (!active.isEmpty()) {

            context.checkContext();

            MazeCell cell = active.get(random.nextInt(active.size()));
            List<MazeCell> neighbours = MazeUtils.getUnvisitedNeighbors(cell.getNeighbours());

            if (!neighbours.isEmpty()) {
                MazeCell neighbour = neighbours.get(random.nextInt(neighbours.size()));
                cell.link(neighbour);

                context.stepCounter().increment();

                active.push(neighbour);
            } else {
                active.remove(cell);
            }
        }
    }
}
