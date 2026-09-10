package cz.logicgo.ui.handlers.sudokuGame;

import cz.logicgo.core.gameClasses.keys.KeyEventDTO;
import cz.logicgo.engine.algorithms.sudoku.SudokuGame;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.SudokuGameController;
import javafx.scene.input.KeyEvent;

import static cz.logicgo.ui.utils.KeyEventUtils.fromJavaFx;


public class KeyReleasedSudokuHandler extends SudokuHandlerBase {

    public KeyReleasedSudokuHandler(SudokuGame sudokuGameState, SudokuGameController sudokuGameController) {
        super(sudokuGameState, sudokuGameController);
    }

    public void onKeyReleased(KeyEvent keyEvent) {
        var user = getSudokuGame().getSudoku().getPlayer();
        var hotkeys = user.getSavedHotkeys().values();
        KeyEventDTO keyEventDTO = fromJavaFx(keyEvent);

        if (hotkeys.stream().anyMatch(keyEventDTO::matches)) {
            this.setKeyPressed(keyEvent, false);
        }
    }
}
