package cz.logicgo.engine.algorithms.mazes.algorithmImplementations;


import cz.logicgo.core.exceptions.parallel.LimitReachedException;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.misc.StepCounter;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.RectangularDirection;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.engine.context.MazeBuildContext;

import java.util.Random;

public class RecursiveDivisionMazeGenerator {

    public static void runOn(MazeGrid grid, Random random) throws ThreadTerminationException, LimitReachedException {
        runOn(grid, random, new MazeBuildContext(new StepCounter()));
    }

    public static void runOn(MazeGrid grid, Random random, MazeBuildContext context) throws ThreadTerminationException, LimitReachedException {

        for (MazeCell cell : grid.getFlattenedGrid()) {
            for (var neighbor : cell.getNeighbours()) {
                cell.link(neighbor);
            }
        }

        divide(0, 0, grid.getRowCount(), grid.getColCount(), grid, random, context);

    }

    public static void divide(int row, int col, int height, int width, MazeGrid mazeGrid, Random random, MazeBuildContext context) throws ThreadTerminationException, LimitReachedException {
        if (height <= 1 || width <= 1 || (height < 5 && width < 5 && random.nextInt(4) == 0)) return;

        if (height > width) {
            divideHorizontally(row, col, height, width, mazeGrid, random, context);
        } else {
            divideVertically(row, col, height, width, mazeGrid, random, context);
        }
    }

    public static void divideHorizontally(int row, int col, int height, int width, MazeGrid mazeGrid, Random random, MazeBuildContext context) throws ThreadTerminationException, LimitReachedException {
        int divideSouthOf = random.nextInt(height - 1);
        int passageAt = random.nextInt(width);

        for (int i = 0; i < width; i++) {
            if (i == passageAt) continue;


            context.checkContext();

            MazeCell cell = mazeGrid.getCell(row + divideSouthOf, col + i);
            cell.unlink(cell.getNeighbourFromDirection(RectangularDirection.SOUTH));

            context.stepCounter().increment();
        }

        divide(row, col, divideSouthOf + 1, width, mazeGrid, random, context);
        divide(row + divideSouthOf + 1, col, height - divideSouthOf - 1, width, mazeGrid, random, context);
    }

    public static void divideVertically(int row, int col, int height, int width, MazeGrid mazeGrid, Random random, MazeBuildContext context) throws ThreadTerminationException, LimitReachedException {
        int divideEastOf = random.nextInt(width - 1);
        int passageAt = random.nextInt(height);

        for (int i = 0; i < height; i++) {
            if (i == passageAt) continue;

            context.checkContext();

            MazeCell cell = mazeGrid.getCell(row + i, col + divideEastOf);
            cell.unlink(cell.getNeighbourFromDirection(RectangularDirection.EAST));
            context.stepCounter().increment();
        }

        divide(row, col, height, divideEastOf + 1, mazeGrid, random, context);
        divide(row, col + divideEastOf + 1, height, width - divideEastOf - 1, mazeGrid, random, context);
    }
}
