package cz.logicgo.engine.algorithms.mazes.algorithmImplementations;


import cz.logicgo.core.exceptions.parallel.LimitReachedException;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.misc.StepCounter;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.RectangularDirection;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.engine.context.MazeBuildContext;

import java.util.*;
import java.util.function.BiConsumer;

public class EllersMazeGenerator {

    private static class RowState {
        private final Map<Integer, Stack<MazeCell>> cellsInSet;
        private final HashMap<Integer, Integer> setForCell;
        private int nextSet;

        RowState(int nextSet) {
            this.nextSet = nextSet;
            cellsInSet = new HashMap<>();
            setForCell = new HashMap<>();
        }

        RowState() {
            this(0);
        }

        public void record(int set, MazeCell mazeCell) {
            setForCell.put(mazeCell.getCol(), set);
            cellsInSet.computeIfAbsent(set, _ -> new Stack<>()).push(mazeCell);
        }

        public int setForCell(MazeCell mazeCell) {
            if (!setForCell.containsKey(mazeCell.getCol())) {
                record(nextSet, mazeCell);
                nextSet++;
            }
            return setForCell.get(mazeCell.getCol());
        }

        public void merge(int winner, int loser) {
            Stack<MazeCell> loserSet = cellsInSet.get(loser);
            if (loserSet == null) {
                return;
            }
            for (MazeCell mazeCell : loserSet) {
                setForCell.put(mazeCell.getCol(), winner);
                cellsInSet.get(winner).push(mazeCell);
            }
            cellsInSet.remove(loser);
        }

        public RowState next() {
            return new RowState(nextSet);
        }

        public void eachSet(BiConsumer<Integer, Stack<MazeCell>> consumer) {
            for (Map.Entry<Integer, Stack<MazeCell>> entry : cellsInSet.entrySet()) {
                consumer.accept(entry.getKey(), entry.getValue());
            }
        }
    }

    public static void runOn(MazeGrid grid, Random random) throws ThreadTerminationException, LimitReachedException {
        runOn(grid, random, new MazeBuildContext(new StepCounter()));
    }

    public static void runOn(MazeGrid grid, Random random, MazeBuildContext context) throws ThreadTerminationException, LimitReachedException {
        RowState rowState = new RowState();

        for (List<MazeCell> row : grid.getRows()) {
            for (MazeCell mazeCell : row) {
                context.stepCounter().increment();

                context.checkContext();

                MazeCell westNeighbour = mazeCell.getNeighbourFromDirection(RectangularDirection.WEST);

                if (westNeighbour == null) {
                    continue;
                }

                int set = rowState.setForCell(mazeCell);
                int priorSet = rowState.setForCell(westNeighbour);

                boolean shouldLink = set != priorSet && (mazeCell.getNeighbourFromDirection(RectangularDirection.SOUTH) == null || random.nextBoolean());

                if (shouldLink) {
                    mazeCell.link(westNeighbour);
                    rowState.merge(priorSet, set);
                }
            }


            if (row.getFirst().getNeighbourFromDirection(RectangularDirection.SOUTH) != null) {
                RowState nextRowState = rowState.next();

                RowState finalRowState = rowState;

                try {
                    rowState.eachSet((set, cells) -> {
                        List<MazeCell> shuffledCells = new ArrayList<>(cells);
                        Collections.shuffle(shuffledCells, random);

                        for (int i = 0; i < shuffledCells.size(); i++) {
                            context.stepCounter().increment();

                            try {
                                context.checkContext();
                            } catch (LimitReachedException | ThreadTerminationException e) {
                                throw new RuntimeException(e);
                            }

                            MazeCell cell = shuffledCells.get(i);
                            boolean linkSouth = i == 0 || random.nextInt(3) == 0;

                            var southNeighbour = cell.getNeighbourFromDirection(RectangularDirection.SOUTH);

                            if (linkSouth && southNeighbour != null) {
                                cell.link(southNeighbour);
                                nextRowState.record(finalRowState.setForCell(cell), southNeighbour);
                            }
                        }
                    });
                } catch (Exception e) {
                    throw new ThreadTerminationException();
                }


                rowState = nextRowState;
            }
        }

    }
}
