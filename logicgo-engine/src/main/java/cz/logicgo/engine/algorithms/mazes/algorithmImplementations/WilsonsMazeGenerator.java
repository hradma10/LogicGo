package cz.logicgo.engine.algorithms.mazes.algorithmImplementations;

import cz.logicgo.core.exceptions.parallel.LimitReachedException;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.misc.StepCounter;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.engine.context.MazeBuildContext;

import java.util.*;

public class WilsonsMazeGenerator {

    public static void runOn(MazeGrid grid, Random random) throws ThreadTerminationException, LimitReachedException {
        runOn(grid, random, new MazeBuildContext(new StepCounter()));
    }

    public static void runOn(MazeGrid grid, Random random, MazeBuildContext context) throws ThreadTerminationException, LimitReachedException {
        List<MazeCell> validCellsList = grid.getFlattenedGrid().stream()
                .filter(Objects::nonNull)
                .filter(c -> c.getNeighbours() != null && !c.getNeighbours().isEmpty())
                .toList();

        if (validCellsList.isEmpty()) return;

        List<MazeCell> unvisitedList = new ArrayList<>(validCellsList);
        Set<MazeCell> unvisitedSet = new HashSet<>(validCellsList);

        MazeCell first = unvisitedList.remove(random.nextInt(unvisitedList.size()));
        unvisitedSet.remove(first);

        while (!unvisitedList.isEmpty()) {

            context.checkContext();

            MazeCell cell = unvisitedList.get(random.nextInt(unvisitedList.size()));
            List<MazeCell> path = new ArrayList<>(List.of(cell));

            while (unvisitedSet.contains(cell)) {
                List<MazeCell> neighbours = cell.getNeighbours();

                if (neighbours == null || neighbours.isEmpty()) {
                    break;
                }

                cell = neighbours.get(random.nextInt(neighbours.size()));


                context.stepCounter().increment();

                int position = path.indexOf(cell);
                if (position == -1) {
                    path.add(cell);
                } else {
                    path = path.subList(0, position + 1);
                }
            }

            for (int i = 0; i < path.size() - 1; i++) {
                MazeCell cellOnPath = path.get(i);
                cellOnPath.link(path.get(i + 1));

                unvisitedList.remove(cellOnPath);
                unvisitedSet.remove(cellOnPath);

                context.stepCounter().increment();
            }

            MazeCell finalCellOnPath = path.getLast();
            unvisitedList.remove(finalCellOnPath);
            unvisitedSet.remove(finalCellOnPath);
        }
    }
}
