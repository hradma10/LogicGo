package cz.logicgo.ui.handlers.mazeGame;

import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.MazeGameController;

public class MazeHandlerBase {


    final private Maze maze;
    final private MazeGameController mazeGameController;

    public MazeHandlerBase(Maze maze, MazeGameController mazeGameController) {
        this.maze = maze;
        this.mazeGameController = mazeGameController;
    }

    protected MazeGrid getGrid() {
        Maze maze = getMaze();
        if (maze.isHasMultipleFloors()) {
            int floor = maze.getCurrentFloor();
            return maze.getMazeGridFloors().get(floor).getMazeGrid();
        }
        return maze.getMazeGrid();
    }

    public Maze getMaze() {
        return maze;
    }

    public MazeGameController getMazeGameController() {
        return mazeGameController;
    }
}
