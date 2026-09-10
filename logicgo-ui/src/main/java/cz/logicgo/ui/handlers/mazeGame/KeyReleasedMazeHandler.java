package cz.logicgo.ui.handlers.mazeGame;

import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.gameClasses.keys.KeyEventDTO;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.MazeGameController;
import javafx.scene.input.KeyEvent;

import static cz.logicgo.ui.utils.KeyEventUtils.fromJavaFx;


public class KeyReleasedMazeHandler extends MazeHandlerBase {

    public KeyReleasedMazeHandler(Maze maze, MazeGameController mazeGameController) {
        super(maze, mazeGameController);
    }

    public void onKeyReleased(KeyEvent keyEvent) {
        var user = getMaze().getPlayer();
        var hotkeys = user.getSavedHotkeys().values();
        KeyEventDTO keyEventDTO = fromJavaFx(keyEvent);

        if (hotkeys.stream().anyMatch(keyEventDTO::matches)) {

        }
    }
}
