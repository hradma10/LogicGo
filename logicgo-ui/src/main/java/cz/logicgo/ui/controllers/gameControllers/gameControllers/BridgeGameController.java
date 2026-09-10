package cz.logicgo.ui.controllers.gameControllers.gameControllers;

import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.entity.setting.GameSetting;
import cz.logicgo.core.gameClasses.bridge.BridgeElement;
import cz.logicgo.core.gameClasses.bridge.Island;
import cz.logicgo.core.gameClasses.bridge.IslandBridge;
import cz.logicgo.core.misc.enums.Status;
import cz.logicgo.core.misc.enums.hints.BridgeHintType;
import cz.logicgo.core.misc.enums.settings.BridgeSettings;
import cz.logicgo.core.misc.enums.settings.SettingKey;
import cz.logicgo.persistence.services.BridgeService;
import cz.logicgo.persistence.services.SettingService;
import cz.logicgo.ui.commands.Command;
import cz.logicgo.ui.commands.CommandExecutor;
import cz.logicgo.ui.commands.CommandExecutor.SavedStacks;
import cz.logicgo.ui.commands.bridgeCommands.AddOneBridgeCommand;
import cz.logicgo.ui.commands.bridgeCommands.RestartBridgesCommandOne;
import cz.logicgo.ui.controllers.gameControllers.AbstractGameController;
import cz.logicgo.ui.controllers.popover.HintPopOverBridgeController;
import cz.logicgo.ui.controllers.screenControllers.MainScreenController;
import cz.logicgo.ui.controllers.screenControllers.RedrawCanvasFunctions;
import cz.logicgo.ui.handlers.bridgesGame.MouseKeyBridgeHandlers;
import cz.logicgo.ui.misc.NodeSnapshots;
import cz.logicgo.ui.misc.TabState;
import cz.logicgo.ui.misc.windows.AlertBox;
import cz.logicgo.ui.renderers.BridgeRenderer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.controlsfx.control.PopOver;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static cz.logicgo.core.GameUtils.*;


public class BridgeGameController extends AbstractGameController<Bridge> {

    private final SettingService settingService = new SettingService();
    private final BridgeService bridgeService = new BridgeService();
    private MouseKeyBridgeHandlers mouseKeyHandlers;
    private boolean[][] islandsPosition;

    private boolean hintChoice = false;
    private BridgeHintType activeHint;
    private final List<BridgeElement> hoveredElements = new ArrayList<>();

    public boolean isHintChoice() {
        return hintChoice;
    }

    public void setHintChoice(boolean hintChoice) {
        this.hintChoice = hintChoice;
    }

    public BridgeHintType getActiveHint() {
        return activeHint;
    }

    public void setActiveHint(BridgeHintType activeHint) {
        this.activeHint = activeHint;
    }

    public BridgeHintType bridgeHintTypeSetBySettings;

    public void setHoveredElements(BridgeElement... elements) {
        hoveredElements.clear();
        if (elements != null) {
            hoveredElements.addAll(List.of(elements));
        }
    }

    @Override
    public void openHint() {
        hintChoice = true;
        showPopOver();
    }

    public void showPopOver() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cz/logicgo/ui/windows/popovers/HintPopOverBridge.fxml"));
            Parent content = loader.load();
            HintPopOverBridgeController controller = loader.getController();

            PopOver popOver = new PopOver(content);
            controller.initialize(mainScreenController);
            content.getStyleClass().add("root");
            popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
            popOver.setAutoHide(true);
            popOver.setDetachable(false);

            Random random = new Random();
            controller.setOnSelect(type -> {
                this.setActiveHint(type);
                if (type == BridgeHintType.RANDOM_BRIDGE) {
                    Bridge bridge = getGameInstance();
                    List<IslandBridge> currentBridges = bridge.getIslandBridges();

                    List<IslandBridge> missingBridges = bridge.getSolutionBridges().stream()
                            .filter(solBridge -> currentBridges.stream()
                                    .noneMatch(curBridge ->
                                            (curBridge.getStartIsland() == solBridge.getStartIsland() && curBridge.getEndIsland() == solBridge.getEndIsland()) ||
                                                    (curBridge.getStartIsland() == solBridge.getEndIsland() && curBridge.getEndIsland() == solBridge.getStartIsland())))
                            .toList();

                    if (!missingBridges.isEmpty()) {
                        IslandBridge randomMissingBridge = missingBridges.get(random.nextInt(missingBridges.size()));
                        IslandBridge bridgeToInsert = new IslandBridge(randomMissingBridge.getStartIsland(), randomMissingBridge.getEndIsland());
                        bridgeToInsert.setBridgeCount(randomMissingBridge.getBridgeCount());

                        AddOneBridgeCommand command = new AddOneBridgeCommand(bridge, bridgeToInsert);
                        this.getCommandExecutor().execute(command);
                        this.tabState.setChangePending();
                    }

                    this.setHintChoice(false);
                    this.setActiveHint(null);
                    redrawCanvases();
                }
                popOver.hide();
            });

            popOver.show(buttonHint);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BridgeGameController() {
        this.commandExecutor = new CommandExecutor();
    }

    public static boolean[][] createIslandPosition(Bridge bridge) {
        var s = new boolean[bridge.getHeight()][bridge.getWidth()];
        for (Island island : bridge.getIslands()) {
            s[island.getRow()][island.getCol()] = true;
        }
        return s;
    }

    public void initialize(Bridge bridgeGame, MainScreenController mainScreenController, TabState tabState) {
        this.gameInstance = bridgeGame;
        this.mainScreenController = mainScreenController;
        this.tabState = tabState;
        this.islandsPosition = createIslandPosition(bridgeGame);

        Map<SettingKey, GameSetting> settings = getGameSettingsAsMap(gameInstance.getSettings());

        gamePane.setMinSize(0, 0);
        buildToolbar(gameInstance);
        buttonPause.setDisable(true);

        tabState.setChangePending();
        tabState.doneProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                saveGame(true);
                tabState.markSavingFinished();
            }
        });

        gamePane.widthProperty().addListener(this::changeBoard);
        gamePane.heightProperty().addListener(this::changeBoard);

        Platform.runLater(() -> {
            resizeCanvas();
            setGameParameters(bridgeGame);

            commandExecutor.loadBridgesCommands(bridgeGame);

            saveGame(true);
            tabState.setIdOfGame(bridgeGame.getId());
            redrawCanvases();
        });

        Tab tab = tabState.getAssociatedTab();
        tab.setClosable(true);
        tab.setOnCloseRequest((event) -> {
            tab.getTabPane().setTabDragPolicy(TabPane.TabDragPolicy.FIXED);
            event.consume();
            onExit();
        });
    }

    public void clearHintVisuals() {
        for (Island island : gameInstance.getIslands()) {
            island.setIslandColor(null);
        }
        for (IslandBridge bridge : gameInstance.getIslandBridges()) {
            bridge.setLineColor(null);
        }
        redrawCanvases();
    }

    @Override
    protected void setGameParameters(Bridge bridge) {
        mouseKeyHandlers = new MouseKeyBridgeHandlers(bridge, this);

        Map<SettingKey, GameSetting> settings = getGameSettingsAsMap(bridge.getSettings());
        boolean shouldTimerExist = getTypedSetting(settings, BridgeSettings.TIMER);

        if (shouldTimerExist) {
            buttonPause.setVisible(true);
            buttonPause.setManaged(true);
            buttonPause.setDisable(false);
            startTimer();
        } else {
            buttonPause.setVisible(false);
            buttonPause.setManaged(false);
            if (countdownTimer != null) {
                countdownTimer.setVisible(false);
                countdownTimer.setManaged(false);
            }
        }

        redrawCanvases();
        startHoverAnimation();
    }

    @Override
    protected void resizeCanvas() {
        double paneWidth = gamePane.getWidth();
        double paneHeight = gamePane.getHeight();
        if (paneWidth <= 0 || paneHeight <= 0) return;

        double gridCols = gameInstance.getWidth();
        double gridRows = gameInstance.getHeight();
        double padding = 40.0;

        double availableWidth = Math.max(10.0, paneWidth - padding);
        double availableHeight = Math.max(10.0, paneHeight - padding);

        double widthCell = availableWidth / gridCols;
        double heightCell = availableHeight / gridRows;

        double cellSize = Math.max(20.0, Math.min(Math.min(widthCell, heightCell), 70.0));

        double actualCanvasWidth = cellSize * gridCols;
        double actualCanvasHeight = cellSize * gridRows;

        primaryCanvas.setWidth(actualCanvasWidth);
        primaryCanvas.setHeight(actualCanvasHeight);
        secondaryCanvas.setWidth(actualCanvasWidth);
        secondaryCanvas.setHeight(actualCanvasHeight);

        double offsetX = Math.max(0, (paneWidth - actualCanvasWidth) / 2.0);
        double offsetY = Math.max(0, (paneHeight - actualCanvasHeight) / 2.0);

        primaryCanvas.setLayoutX(offsetX);
        primaryCanvas.setLayoutY(offsetY);
        secondaryCanvas.setLayoutX(offsetX);
        secondaryCanvas.setLayoutY(offsetY);

        redrawCanvases();
    }

    @Override
    protected void handleHoverPreview(double timeSeconds) {
        RedrawCanvasFunctions.clearCanvasTransparent(secondaryCanvas);

        drawBridgePreview();

        if (!hoveredElements.isEmpty() && isHintChoice()) {
            double pulse = (Math.sin(timeSeconds * 6) + 1) / 2;
            Color hoverColor = Color.web("#38BDF8", 0.6 + 0.4 * pulse);

            for (BridgeElement element : hoveredElements) {
                if (element instanceof Island island) {
                    GraphicsContext gc = secondaryCanvas.getGraphicsContext2D();
                    double cellW = secondaryCanvas.getWidth() / gameInstance.getWidth();
                    double cellH = secondaryCanvas.getHeight() / gameInstance.getHeight();
                    double cx = (island.getCol() * cellW) + (cellW / 2);
                    double cy = (island.getRow() * cellH) + (cellH / 2);
                    double r = Math.min(cellW, cellH) * 0.42;

                    gc.setStroke(hoverColor);
                    gc.setLineWidth(3.0);
                    gc.strokeOval(cx - r, cy - r, r * 2, r * 2);

                } else if (element instanceof IslandBridge bridge) {
                    String originalColor = bridge.getLineColor();

                    bridge.setLineColor(hoverColor.toString());
                    BridgeRenderer.render(secondaryCanvas, gameInstance);

                    bridge.setLineColor(originalColor);
                }
            }
        }
    }


    private Double currentMouseX = null;
    private Double currentMouseY = null;

    public void redrawCanvases() {
        BridgeRenderer.render(primaryCanvas, gameInstance);
        RedrawCanvasFunctions.clearCanvasTransparent(secondaryCanvas);

        drawBridgePreview();
    }

    private void drawBridgePreview() {
        if (mouseKeyHandlers == null || currentMouseX == null || currentMouseY == null) return;

        Island startIsland = mouseKeyHandlers.getMouseClickedHandler().getBridgeCreationState().getStartIsland();
        if (startIsland == null) return;

        GraphicsContext gc = secondaryCanvas.getGraphicsContext2D();

        double cellW = secondaryCanvas.getWidth() / gameInstance.getWidth();
        double cellH = secondaryCanvas.getHeight() / gameInstance.getHeight();

        double startX = (startIsland.getCol() * cellW) + (cellW / 2);
        double startY = (startIsland.getRow() * cellH) + (cellH / 2);

        Color activeAccent = Color.web("#38BDF8");
        double islandRadius = Math.min(cellW, cellH) * 0.4;

        gc.save();
        gc.setFill(Color.web("#38BDF8", 0.12));
        gc.setStroke(activeAccent);
        gc.setLineWidth(2.5);
        gc.fillOval(startX - islandRadius, startY - islandRadius, islandRadius * 2, islandRadius * 2);
        gc.strokeOval(startX - islandRadius, startY - islandRadius, islandRadius * 2, islandRadius * 2);
        gc.restore();

        double dx = currentMouseX - startX;
        double dy = currentMouseY - startY;
        double projectedX = currentMouseX;
        double projectedY = currentMouseY;

        if (Math.abs(dx) > Math.abs(dy)) {
            projectedY = startY;
        } else {
            projectedX = startX;
        }

        double lineDx = projectedX - startX;
        double lineDy = projectedY - startY;
        double totalLength = Math.sqrt(lineDx * lineDx + lineDy * lineDy);

        if (totalLength <= islandRadius) return;

        double dirX = lineDx / totalLength;
        double dirY = lineDy / totalLength;

        double renderStartX = startX + dirX * islandRadius;
        double renderStartY = startY + dirY * islandRadius;
        double renderEndX = projectedX;
        double renderEndY = projectedY;

        gc.save();
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);

        gc.setStroke(Color.web("#38BDF8", 0.25));
        gc.setLineWidth(6.0);
        gc.strokeLine(renderStartX, renderStartY, renderEndX, renderEndY);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2.5);
        gc.setLineDashes(10, 6);
        gc.strokeLine(renderStartX, renderStartY, renderEndX, renderEndY);

        gc.restore();
    }

    public void updateMousePosition(Double x, Double y) {
        this.currentMouseX = x;
        this.currentMouseY = y;
    }

    @Override
    public void saveGame(boolean newThumbnail) {
        updateElapsedTime();
        try {
            SavedStacks stacks = commandExecutor.serializeCommandsToByteArrays();
            gameInstance.setUndoStack(stacks.undoStack());
            gameInstance.setRedoStack(stacks.redoStack());
            if (newThumbnail) {
                double w = primaryCanvas.getWidth() <= 0 ? 600 : primaryCanvas.getWidth();
                double h = primaryCanvas.getHeight() <= 0 ? 600 : primaryCanvas.getHeight();
                new NodeSnapshots().makeThumbnail(gameInstance, w, h);
            }
        } catch (Exception _) {
        } finally {
            bridgeService.saveGame(gameInstance);
            tabState.setIdOfGame(gameInstance.getId());
            tabState.unsetChangePending();
        }
    }

    @Override
    public void gameFinished() {
        gameInstance.setStatus(Status.FINISHED);
        pause();
        buttonPause.setDisable(true);
        buttonHint.setDisable(true);
        tabState.markSavingFinished();
        saveGame(true);
        AlertBox.YouWonWindow(gameInstance.getElapsedTime());
    }

    @Override
    protected void onUndo() {
        commandExecutor.undo();
        redrawCanvases();
    }

    @Override
    protected void onRedo() {
        commandExecutor.redo();
        redrawCanvases();
    }

    @Override
    public void onRestart() {
        Command command = new RestartBridgesCommandOne(gameInstance, gameInstance.getIslandBridges(), new ArrayList<>());
        commandExecutor.execute(command);
        redrawCanvases();
    }

    @FXML
    public void onMouseClicked(MouseEvent mouseEvent) {
        mouseKeyHandlers.onMouseClicked(mouseEvent);
    }

    @FXML
    public void onKeyPressed(KeyEvent keyEvent) throws IOException {
        mouseKeyHandlers.onKeyPressed(keyEvent);
    }

    @FXML
    public void onMouseReleased(MouseEvent mouseEvent) {
        mouseKeyHandlers.onMouseReleased(mouseEvent);
    }

    @FXML
    public void onMouseMoved(MouseEvent mouseEvent) {
        mouseKeyHandlers.onMouseMoved(mouseEvent);
    }

    @FXML
    public void onMouseDragged(MouseEvent mouseEvent) {
        mouseKeyHandlers.onMouseDragged(mouseEvent);
    }

    @FXML
    public void onKeyReleased(KeyEvent keyEvent) {
        mouseKeyHandlers.onKeyReleased(keyEvent);
    }

    @Override
    public void terminateAllActiveActions() {
        super.terminateAllActiveActions();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public boolean[][] getIslandsPosition() {
        return islandsPosition;
    }

    public BridgeGameController setIslandsPosition(boolean[][] islandsPosition) {
        this.islandsPosition = islandsPosition;
        return this;
    }

    public SettingService getSettingService() {
        return settingService;
    }
}
