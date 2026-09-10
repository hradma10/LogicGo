package cz.logicgo.engine.algorithms.mazes.solvers;


import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.engine.context.MazeBuildContext;

import java.util.*;

public class MazeValidityChecker {

    public static HashMap<MazeCell, Integer> checkValidity(MazeGrid mazeGrid, MazeBuildContext stepCounter) throws ThreadTerminationException {
        HashMap<MazeCell, Integer> distances = new HashMap<>(mazeGrid.size(), 1);
        ArrayDeque<MazeCell> toVisit = new ArrayDeque<>();

        MazeCell startCell = mazeGrid.getStartCell();

        if (startCell == null) {
            startCell = mazeGrid.getFlattenedGrid().stream()
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        if (startCell == null) {

            return distances;
        }

        toVisit.add(startCell);
        distances.put(startCell, 0);

        while (!toVisit.isEmpty()) {
            if (Thread.currentThread().isInterrupted()) {
                throw new ThreadTerminationException();
            }
            if (stepCounter != null) stepCounter.stepCounter().increment();

            MazeCell cell = toVisit.pop();
            int distance = distances.get(cell);

            for (MazeCell neighbor : cell.getLinked()) {
                if (!distances.containsKey(neighbor)) {
                    distances.put(neighbor, distance + 1);
                    toVisit.add(neighbor);
                }
            }
        }

        return distances;
    }

    public static List<MazeCell> findPath(MazeCell start, MazeCell end, MazeBuildContext stepCounter) throws ThreadTerminationException {
        HashMap<MazeCell, MazeCell> predecessors = new HashMap<>();
        ArrayDeque<MazeCell> queue = new ArrayDeque<>();

        queue.add(start);
        predecessors.put(start, null);

        boolean found = false;

        while (!queue.isEmpty()) {
            if (Thread.currentThread().isInterrupted()) {
                throw new ThreadTerminationException();
            }
            if (stepCounter != null) stepCounter.stepCounter().increment();

            MazeCell current = queue.poll();

            if (current.equals(end)) {
                found = true;
                break;
            }

            for (MazeCell neighbor : current.getLinked()) {
                if (!predecessors.containsKey(neighbor)) {
                    predecessors.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        if (!found) {
            return Collections.emptyList();
        }

        return reconstructPath(predecessors, end);
    }

    private static List<MazeCell> reconstructPath(Map<MazeCell, MazeCell> predecessors, MazeCell end) {
        List<MazeCell> path = new ArrayList<>();
        MazeCell current = end;

        while (current != null) {
            path.add(current);
            current = predecessors.get(current);
        }

        Collections.reverse(path);
        return path;
    }
}
