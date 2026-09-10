package cz.logicgo.ui.handlers.mazeGame;

import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.MazeGameController;
import javafx.scene.input.MouseEvent;

public class MousePressedMazeHandler extends MazeHandlerBase {

    public MousePressedMazeHandler(Maze maze, MazeGameController mazeGameController) {
        super(maze, mazeGameController);
    }

    public void onMousePressed(MouseEvent mouseEvent) {
    }

}

