package cz.logicgo.engine.algorithms.mazes.algorithmImplementations;


import cz.logicgo.core.exceptions.parallel.LimitReachedException;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.misc.StepCounter;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.RectangularDirection;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.engine.context.MazeBuildContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SidewinderMazeGenerator {


    public static void runOn(MazeGrid grid, Random random) throws ThreadTerminationException, LimitReachedException {
        runOn(grid, random, new MazeBuildContext(new StepCounter()));
    }

    public static void runOn(MazeGrid grid, Random random, MazeBuildContext context) throws ThreadTerminationException, LimitReachedException {

        for (int rowIndex = 0; rowIndex < grid.getRowCount(); rowIndex++) {
            List<MazeCell> row = grid.getRow(rowIndex);
            List<MazeCell> run = new ArrayList<>();

            for (MazeCell cell : row) {
                run.add(cell);

                context.checkContext();

                MazeCell northNeighbour = cell.getNeighbourFromDirection(RectangularDirection.NORTH);
                MazeCell eastNeighbour = cell.getNeighbourFromDirection(RectangularDirection.EAST);

                boolean northBoundary = northNeighbour == null;
                boolean eastBoundary = eastNeighbour == null;

                boolean shouldClose = eastBoundary || (!northBoundary && random.nextBoolean());

                if (shouldClose) {
                    MazeCell member = run.get(random.nextInt(run.size()));
                    MazeCell membersNorthNeighbour = member.getNeighbourFromDirection(RectangularDirection.NORTH);
                    if (membersNorthNeighbour != null) {
                        member.link(membersNorthNeighbour);
                        context.stepCounter().increment();
                    }
                    run.clear();
                } else {
                    cell.link(eastNeighbour);
                    context.stepCounter().increment();
                }
            }
        }

    }
}
