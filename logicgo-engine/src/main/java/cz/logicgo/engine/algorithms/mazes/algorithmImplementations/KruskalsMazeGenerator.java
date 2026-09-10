package cz.logicgo.engine.algorithms.mazes.algorithmImplementations;

import cz.logicgo.core.exceptions.parallel.LimitReachedException;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.misc.StepCounter;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.RectangularDirection;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.engine.context.MazeBuildContext;

import java.util.*;

public class KruskalsMazeGenerator {


    private static class KruskalState {
        private final HashMap<MazeCell, HashSet<MazeCell>> setsForCell;

        public KruskalState(MazeGrid grid) {
            setsForCell = new HashMap<>();
            for (MazeCell cell : grid.getFlattenedGrid()) {
                if (cell == null) continue;
                HashSet<MazeCell> set = new HashSet<>();
                set.add(cell);
                setsForCell.put(cell, set);
            }
        }

        public boolean canMerge(MazeCell left, MazeCell right) {
            if (left == null || right == null) return false;
            HashSet<MazeCell> setLeft = setsForCell.get(left);
            HashSet<MazeCell> setRight = setsForCell.get(right);

            if (setLeft == null || setRight == null) return false;

            return !setLeft.equals(setRight);
        }

        public void merge(MazeCell left, MazeCell right) {
            left.link(right);

            HashSet<MazeCell> winner = setsForCell.get(left);
            HashSet<MazeCell> loser = setsForCell.get(right);

            if (loser != null && winner != null) {
                for (MazeCell cell : loser) {
                    winner.add(cell);
                    setsForCell.put(cell, winner);
                }
            }
        }
    }

    private record NeighbourPair(MazeCell left, MazeCell right) {
    }

    public static void runOn(MazeGrid grid, Random random) throws ThreadTerminationException, LimitReachedException {
        runOn(grid, random, new MazeBuildContext(new StepCounter()));
    }

    public static void runOn(MazeGrid grid, Random random, MazeBuildContext context) throws ThreadTerminationException, LimitReachedException {
        KruskalState state = new KruskalState(grid);
        List<NeighbourPair> neighbours = new ArrayList<>();

        for (MazeCell cell : grid.getFlattenedGrid()) {
            context.stepCounter().increment();
            if (cell == null) continue;

            context.checkContext();

            MazeCell eastNeighbour = cell.getNeighbourFromDirection(RectangularDirection.EAST);
            MazeCell southNeighbour = cell.getNeighbourFromDirection(RectangularDirection.SOUTH);

            if (eastNeighbour != null) neighbours.add(new NeighbourPair(cell, eastNeighbour));
            if (southNeighbour != null) neighbours.add(new NeighbourPair(cell, southNeighbour));
        }

        Collections.shuffle(neighbours, random);

        while (!neighbours.isEmpty()) {

            context.checkContext();

            context.stepCounter().increment();
            NeighbourPair pair = neighbours.removeLast();
            MazeCell left = pair.left();
            MazeCell right = pair.right();

            if (state.canMerge(left, right)) {
                state.merge(left, right);
            }
        }
    }
}
