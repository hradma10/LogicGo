package cz.logicgo.engine.algorithms.mazes.algorithmImplementations;

import cz.logicgo.core.exceptions.parallel.LimitReachedException;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.misc.StepCounter;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.engine.context.MazeBuildContext;

import java.util.List;
import java.util.Random;

import static cz.logicgo.core.gameClasses.maze.MazeUtils.getUnvisitedNeighbors;
import static cz.logicgo.core.gameClasses.maze.MazeUtils.getVisitedNeighbors;


public class HuntAndKillMazeGenerator {

    public static void runOn(MazeGrid grid, Random random) throws ThreadTerminationException, LimitReachedException {
        runOn(grid, random, new MazeBuildContext(new StepCounter()));
    }

    public static void runOn(MazeGrid grid, Random random, MazeBuildContext context) throws ThreadTerminationException, LimitReachedException {
        MazeCell current = null;
        while (current == null) {
            current = grid.getRandomCell(random);
        }

        while (current != null) {
            if (Thread.currentThread().isInterrupted()) {
                throw new ThreadTerminationException();
            }

            List<MazeCell> unvisitedNeighbors = getUnvisitedNeighbors(current.getNeighbours());

            if (!unvisitedNeighbors.isEmpty()) {
                MazeCell neighbor = unvisitedNeighbors.get(random.nextInt(unvisitedNeighbors.size()));
                current.link(neighbor);
                current = neighbor;
                context.stepCounter().increment();
            } else {
                current = null;

                for (MazeCell cell : grid.getFlattenedGrid()) {
                    context.checkContext();

                    if (cell == null) continue;

                    List<MazeCell> visitedNeighbors = getVisitedNeighbors(cell.getNeighbours());
                    if (cell.getLinked().isEmpty() && !visitedNeighbors.isEmpty()) {
                        current = cell;
                        MazeCell neighbor = visitedNeighbors.get(random.nextInt(visitedNeighbors.size()));
                        current.link(neighbor);

                        context.stepCounter().increment();
                        break;
                    }
                }
            }
        }
    }
}
