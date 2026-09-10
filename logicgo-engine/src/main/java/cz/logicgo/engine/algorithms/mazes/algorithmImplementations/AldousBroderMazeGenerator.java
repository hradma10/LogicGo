package cz.logicgo.engine.algorithms.mazes.algorithmImplementations;


import cz.logicgo.core.exceptions.parallel.LimitReachedException;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.misc.StepCounter;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.engine.context.MazeBuildContext;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class AldousBroderMazeGenerator {

    public static void runOn(MazeGrid grid, Random random) throws ThreadTerminationException, LimitReachedException {
        runOn(grid, random, new MazeBuildContext(new StepCounter()));
    }

    public static void runOn(MazeGrid grid, Random random, MazeBuildContext context) throws ThreadTerminationException, LimitReachedException {
        List<MazeCell> validCells = grid.getFlattenedGrid().stream()
                .filter(Objects::nonNull)
                .toList();

        if (validCells.isEmpty()) return;

        MazeCell cell = validCells.get(random.nextInt(validCells.size()));
        int unvisited = validCells.size() - 1;

        while (unvisited > 0) {
            context.stepCounter().increment();

            context.checkContext();

            List<MazeCell> neighbours = cell.getNeighbours();
            if (neighbours.isEmpty()) break;

            MazeCell neighbor = neighbours.get(random.nextInt(neighbours.size()));

            if (neighbor.getLinked().isEmpty()) {
                cell.link(neighbor);
                unvisited--;
            }

            cell = neighbor;
        }
    }
}
