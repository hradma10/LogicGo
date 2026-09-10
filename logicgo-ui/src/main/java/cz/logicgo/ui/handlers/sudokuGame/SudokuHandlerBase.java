package cz.logicgo.ui.handlers.sudokuGame;

import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.engine.algorithms.sudoku.SudokuGame;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.SudokuGameController;
import cz.logicgo.ui.controllers.screenControllers.RedrawCanvasFunctions;
import cz.logicgo.ui.renderers.sudoku.SudokuRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static javafx.scene.input.KeyCode.*;

public class SudokuHandlerBase {

    private final static KeyCode[] supportedKeys;
    private static final Map<KeyCode, Boolean> supportedKeyMap = new HashMap<>();

    public static final Map<KeyCode, Integer> keysToNumber = new HashMap<>();

    public static final HashMap<Integer, String> valueToString;

    static {
        supportedKeys = new KeyCode[]{
                W, S, A, D,

                UP, DOWN, LEFT, RIGHT,

                Z,
                Y,
                H,
                E,
                S,
                DELETE,

                NUMPAD0, NUMPAD1, NUMPAD2, NUMPAD3, NUMPAD4,
                NUMPAD5, NUMPAD6, NUMPAD7, NUMPAD8, NUMPAD9,

                DIGIT0, DIGIT1, DIGIT2, DIGIT3, DIGIT4,
                DIGIT5, DIGIT6, DIGIT7, DIGIT8, DIGIT9
        };

        valueToString = new HashMap<>();
        IntStream.rangeClosed(0, 9).forEach(i -> {
            keysToNumber.put(valueOf("NUMPAD" + i), i);
            keysToNumber.put(valueOf("DIGIT" + i), i);
            valueToString.put(i, String.valueOf(i));
        });
        char c = 'A';
        int i = 10;
        while (c <= 'F') {
            String ch = String.valueOf(c);
            keysToNumber.put(KeyCode.getKeyCode(ch), i);
            valueToString.put(i, ch);
            c++;
            i++;
        }

        keysToNumber.put(KeyCode.DELETE, 0);
        keysToNumber.put(KeyCode.BACK_SPACE, 0);
    }

    final private SudokuGame sudokuGame;
    final private SudokuGameController sudokuGameController;

    public SudokuHandlerBase(SudokuGame sudokuGame, SudokuGameController sudokuGameController) {
        this.sudokuGame = sudokuGame;
        this.sudokuGameController = sudokuGameController;
        Arrays.stream(supportedKeys).forEach(keyCode -> supportedKeyMap.put(keyCode, false));
    }

    static {

    }

    public Optional<SudokuCell> getCellAtPosition(double x, double y) {
        Canvas canvas = sudokuGameController.getPrimaryCanvas();

        double relativeX = x - canvas.getLayoutX();
        double relativeY = y - canvas.getLayoutY();

        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        if (relativeX < 0 || relativeY < 0 || relativeX >= canvasWidth || relativeY >= canvasHeight) {
            return Optional.empty();
        }

        int gridSize = sudokuGame.getSudokuValidator().getGridSize();
        double cellSize = canvasWidth / gridSize;

        int col = (int) (relativeX / cellSize);
        int row = (int) (relativeY / cellSize);

        col = Math.min(col, gridSize - 1);
        row = Math.min(row, gridSize - 1);

        return Optional.ofNullable(sudokuGame.getSudoku().getSudokuCell(row, col));
    }

    public static KeyCode[] getSupportedKeys() {
        return supportedKeys;
    }

    static Map<KeyCode, Boolean> getSupportedKeyMap() {
        return supportedKeyMap;
    }

    public boolean isKeyPressed(KeyEvent keyEvent) {
        return supportedKeyMap.getOrDefault(keyEvent.getCode(), false);
    }

    public void setKeyPressed(KeyEvent keyEvent, boolean state) {
        supportedKeyMap.put(keyEvent.getCode(), state);
    }

    public SudokuGame getSudokuGame() {
        return sudokuGame;
    }

    public SudokuGameController getSudokuGameController() {
        return sudokuGameController;
    }

    public void selectCellOnCanvas(SudokuCell cell) {
        cell.setDrawToPrimary(false);
        cell.setSelected(true);
    }


    public void unselectCellOnCanvas(SudokuCell cell) {
        cell.setDrawToPrimary(true);
        cell.setSelected(false);
    }

    public void redrawCanvases(boolean drawPrimary, boolean drawSecondary) {
        SudokuGameController con = getSudokuGameController();

        con.setCandidates();

        if (drawPrimary) {
            Canvas mainCanvas = con.getPrimaryCanvas();

            SudokuRenderer.renderFullBoard(mainCanvas, sudokuGame.getSudoku(), false);
        }

        if (drawSecondary) {
            Canvas secondaryCanvas = con.getSecondaryCanvas();
            RedrawCanvasFunctions.clearCanvasTransparent(secondaryCanvas);
        }
    }

}
