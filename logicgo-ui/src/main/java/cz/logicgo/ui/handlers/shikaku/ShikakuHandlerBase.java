package cz.logicgo.ui.handlers.shikaku;

import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.gameClasses.shikaku.ShikakuCell;
import cz.logicgo.core.gameClasses.shikaku.ShikakuRectangle;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.ShikakuGameController;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static javafx.scene.input.KeyCode.*;

public class ShikakuHandlerBase {

    private final static KeyCode[] supportedKeys = {
            W, S, A, D, UP, DOWN, LEFT, RIGHT, Z, Y, H, E, KeyCode.S, DELETE
    };
    private static final Map<KeyCode, Boolean> supportedKeyMap = new HashMap<>();

    private final Shikaku shikakuGame;
    private final ShikakuGameController shikakuGameController;

    public ShikakuHandlerBase(Shikaku shikakuGame, ShikakuGameController shikakuGameController) {
        this.shikakuGame = shikakuGame;
        this.shikakuGameController = shikakuGameController;
        Arrays.stream(supportedKeys).forEach(keyCode -> supportedKeyMap.put(keyCode, false));
    }

    public static Optional<ShikakuCell> getCellAt(ShikakuGameController con, Shikaku shikaku, double x, double y) {
        Canvas canvas = con.getPrimaryCanvas();

        double cellW = canvas.getWidth() / shikaku.getWidth();
        double cellH = canvas.getHeight() / shikaku.getHeight();

        int col = (int) (x / cellW);
        int row = (int) (y / cellH);

        if (row >= 0 && row < shikaku.getHeight() && col >= 0 && col < shikaku.getWidth()) {
            return Optional.of(shikaku.getCell(row, col));
        }
        return Optional.empty();
    }

    public Optional<ShikakuRectangle> getRectangleAt(ShikakuCell cell) {
        for (ShikakuRectangle rect : shikakuGame.getRectangles()) {
            if (rect.contains(cell)) {
                return Optional.of(rect);
            }
        }
        return Optional.empty();
    }

    public boolean isKeyPressed(KeyEvent keyEvent) {
        return supportedKeyMap.getOrDefault(keyEvent.getCode(), false);
    }

    public void setKeyPressed(KeyEvent keyEvent, boolean state) {
        supportedKeyMap.put(keyEvent.getCode(), state);
    }

    public ShikakuGameController getShikakuGameController() {
        return shikakuGameController;
    }

    public Shikaku getShikakuGame() {
        return shikakuGame;
    }
}
