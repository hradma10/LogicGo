package cz.logicgo.ui.handlers.mazeGame;

import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.MazeGameController;
import javafx.scene.input.MouseEvent;

public class MouseReleasedMazeHandler extends MazeHandlerBase {

    public MouseReleasedMazeHandler(Maze maze, MazeGameController mazeGameController) {
        super(maze, mazeGameController);
    }

    public void onMouseReleased(MouseEvent mouseEvent) {

    }
}
