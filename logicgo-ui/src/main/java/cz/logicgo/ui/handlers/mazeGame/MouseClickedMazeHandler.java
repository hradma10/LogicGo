package cz.logicgo.ui.handlers.mazeGame;

import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.MazeGameController;
import javafx.scene.input.MouseEvent;

public class MouseClickedMazeHandler extends MazeHandlerBase {

    public MouseClickedMazeHandler(Maze maze, MazeGameController mazeGameController) {
        super(maze, mazeGameController);
    }

    public void onMouseClicked(MouseEvent mouseEvent) {
        MazeGameController con = getMazeGameController();
        con.stopHintFeedbackTimer();
        con.getGameInstance().getMazeGridFloors().forEach(floor ->
                floor.getMazeGrid().getPath().setMazeHintType(null)
        );
        con.redrawCanvases(false, true);

    }
}
