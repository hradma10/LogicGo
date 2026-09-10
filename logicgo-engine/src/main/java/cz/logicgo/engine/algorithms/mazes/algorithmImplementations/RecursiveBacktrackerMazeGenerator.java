package cz.logicgo.engine.algorithms.mazes.algorithmImplementations;


import cz.logicgo.core.exceptions.parallel.LimitReachedException;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.core.misc.StepCounter;
import cz.logicgo.engine.context.MazeBuildContext;

import java.util.List;
import java.util.Random;
import java.util.Stack;

import static cz.logicgo.core.gameClasses.maze.MazeUtils.getUnvisitedNeighbors;


public class RecursiveBacktrackerMazeGenerator {

    public static void runOn(MazeGrid grid, Random random) throws ThreadTerminationException, LimitReachedException {
        runOn(grid, random, new MazeBuildContext(new StepCounter()));
    }

    public static void runOn(MazeGrid grid, Random random, MazeBuildContext context) throws ThreadTerminationException, LimitReachedException {
        Stack<MazeCell> stack = new Stack<>();

        MazeCell startCell = null;
        while (startCell == null) {
            startCell = grid.getRandomCell(random);
        }

        stack.push(startCell);


        while (!stack.isEmpty()) {

            context.checkContext();

            MazeCell current = stack.peek();
            List<MazeCell> neighbours = getUnvisitedNeighbors(current.getNeighbours());

            if (!neighbours.isEmpty()) {
                MazeCell neighbour = neighbours.get(random.nextInt(neighbours.size()));
                current.link(neighbour);
                stack.push(neighbour);

                context.stepCounter().increment();

            } else {
                stack.pop();
            }
        }

    }
}
