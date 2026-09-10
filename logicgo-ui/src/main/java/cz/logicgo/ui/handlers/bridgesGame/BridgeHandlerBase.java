package cz.logicgo.ui.handlers.bridgesGame;

import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.gameClasses.bridge.BridgeElement;
import cz.logicgo.core.gameClasses.bridge.Island;
import cz.logicgo.core.gameClasses.bridge.IslandBridge;
import cz.logicgo.core.model.games.drawable.bounds.Point;
import cz.logicgo.engine.algorithms.drawing.CohenSutherlandAlgorithm;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.BridgeGameController;
import cz.logicgo.ui.controllers.screenControllers.RedrawCanvasFunctions;
import cz.logicgo.ui.handlers.bridgesGame.states.BridgeCreationState;
import cz.logicgo.ui.renderers.BridgeRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.*;

import static javafx.scene.input.KeyCode.*;

public class BridgeHandlerBase {

    private final static KeyCode[] supportedKeys;
    private static final Map<KeyCode, Boolean> supportedKeyMap = new HashMap<>();
    private final BridgeRenderer renderer = new BridgeRenderer();

    static {
        supportedKeys = new KeyCode[]{
                W, S, A, D,
                KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT,
                KeyCode.Z, KeyCode.Y, KeyCode.H, KeyCode.E, KeyCode.S, KeyCode.DELETE,
        };
    }

    final private Bridge bridgeGame;
    final private BridgeGameController bridgeGameController;
    final private BridgeCreationState bridgeCreationState;

    public BridgeHandlerBase(Bridge bridgeGame, BridgeGameController bridgeGameController) {
        this.bridgeGame = bridgeGame;
        this.bridgeGameController = bridgeGameController;
        this.bridgeCreationState = new BridgeCreationState();
        Arrays.stream(supportedKeys).forEach(keyCode -> supportedKeyMap.put(keyCode, false));
    }


    public Optional<BridgeElement> getElementAt(double x, double y) {
        Canvas canvas = bridgeGameController.getPrimaryCanvas();
        double cellW = canvas.getWidth() / bridgeGame.getWidth();
        double cellH = canvas.getHeight() / bridgeGame.getHeight();


        int col = (int) (x / cellW);
        int row = (int) (y / cellH);

        for (Island island : bridgeGame.getIslands()) {
            if (island.getRow() == row && island.getCol() == col) {
                return Optional.of(island);
            }
        }

        double t = 15.0;
        double xMin = x - t;
        double yMin = y - t;
        double xMax = x + t;
        double yMax = y + t;

        for (IslandBridge bridge : bridgeGame.getIslandBridges()) {
            double x1 = (bridge.getStartIsland().getCol() * cellW) + (cellW / 2);
            double y1 = (bridge.getStartIsland().getRow() * cellH) + (cellH / 2);
            double x2 = (bridge.getEndIsland().getCol() * cellW) + (cellW / 2);
            double y2 = (bridge.getEndIsland().getRow() * cellH) + (cellH / 2);

            if (CohenSutherlandAlgorithm.isIntersecting(xMin, yMin, xMax, yMax,
                    new Point(x1, y1), new Point(x2, y2))) {
                return Optional.of(bridge);
            }
        }

        return Optional.empty();
    }

    public void selectIsland(Island island) {
        island.setSelected(true);
    }

    public void unselectIsland(Island island) {
        island.setSelected(false);
    }

    public void redrawCanvases(boolean drawPrimary, boolean drawSecondary) {
        if (drawPrimary) {
            BridgeRenderer.render(bridgeGameController.getPrimaryCanvas(), bridgeGame);
        }

        if (drawSecondary) {
            RedrawCanvasFunctions.clearCanvasTransparent(bridgeGameController.getSecondaryCanvas());
        }
    }

    protected boolean ifAlreadyExists(Bridge bridge, Island startIsland, Island endIsland) {
        if (bridge.getIslandBridges() == null || startIsland == null || endIsland == null) {
            return false;
        }

        for (IslandBridge existingBridge : bridge.getIslandBridges()) {
            Island s = existingBridge.getStartIsland();
            Island e = existingBridge.getEndIsland();

            boolean matchForward = isSameIsland(s, startIsland) && isSameIsland(e, endIsland);
            boolean matchBackward = isSameIsland(s, endIsland) && isSameIsland(e, startIsland);

            if (matchForward || matchBackward) {
                return true;
            }
        }
        return false;
    }

    protected boolean isSameIsland(Island i1, Island i2) {
        return Objects.equals(i1.getRow(), i2.getRow()) && Objects.equals(i1.getCol(), i2.getCol());
    }

    public boolean isKeyPressed(KeyEvent keyEvent) {
        return supportedKeyMap.getOrDefault(keyEvent.getCode(), false);
    }

    public void setKeyPressed(KeyEvent keyEvent, boolean state) {
        supportedKeyMap.put(keyEvent.getCode(), state);
    }

    public BridgeGameController getBridgeGameController() {
        return bridgeGameController;
    }

    public Bridge getBridgeGame() {
        return bridgeGame;
    }

    public BridgeCreationState getBridgeCreationState() {
        return bridgeCreationState;
    }
}
