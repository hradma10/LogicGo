package cz.logicgo.ui.controllers.gameControllers.gameControllers;

import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.entity.setting.GameSetting;
import cz.logicgo.core.gameClasses.shikaku.ShikakuCell;
import cz.logicgo.core.gameClasses.shikaku.ShikakuRectangle;
import cz.logicgo.core.misc.enums.Status;
import cz.logicgo.core.misc.enums.hints.ShikakuHintType;
import cz.logicgo.core.misc.enums.settings.SettingKey;
import cz.logicgo.core.misc.enums.settings.SudokuSettings;
import cz.logicgo.persistence.services.SettingService;
import cz.logicgo.persistence.services.ShikakuService;
import cz.logicgo.ui.commands.Command;
import cz.logicgo.ui.commands.CommandExecutor;
import cz.logicgo.ui.commands.shikakuCommands.AddRectangleCommand;
import cz.logicgo.ui.commands.shikakuCommands.RestartShikakuCommand;
import cz.logicgo.ui.controllers.gameControllers.AbstractGameController;
import cz.logicgo.ui.controllers.popover.HintPopOverShikakuController;
import cz.logicgo.ui.controllers.screenControllers.MainScreenController;
import cz.logicgo.ui.controllers.screenControllers.RedrawCanvasFunctions;
import cz.logicgo.ui.handlers.shikaku.MouseKeyShikakuHandlers;
import cz.logicgo.ui.handlers.shikaku.states.ShikakuCreationState;
import cz.logicgo.ui.misc.NodeSnapshots;
import cz.logicgo.ui.misc.TabState;
import cz.logicgo.ui.misc.windows.AlertBox;
import cz.logicgo.ui.renderers.ShikakuRenderer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.controlsfx.control.PopOver;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static cz.logicgo.core.GameUtils.getGameSettingsAsMap;
import static cz.logicgo.core.GameUtils.getTypedSetting;
import static cz.logicgo.ui.commands.CommandExecutor.*;
import static cz.logicgo.ui.handlers.shikaku.ShikakuHandlerBase.getCellAt;


public class ShikakuGameController extends AbstractGameController<Shikaku> {

    private final SettingService settingService = new SettingService();
    private final ShikakuService shikakuService = new ShikakuService();
    private MouseKeyShikakuHandlers mouseKeyHandlers;

    private boolean hintChoice = false;

    private ShikakuHintType activeHint;

    public ShikakuHintType shikakuHintTypeSetBySettings;

    public ShikakuGameController() {
        this.commandExecutor = new CommandExecutor();
    }

    public boolean isHintChoice() {
        return hintChoice;
    }

    public void setHintChoice(boolean hintChoice) {
        this.hintChoice = hintChoice;
    }

    public void initialize(Shikaku shikakuGame, MainScreenController mainScreenController, TabState tabState) {
        this.gameInstance = shikakuGame;
        this.mainScreenController = mainScreenController;
        this.tabState = tabState;

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

        javafx.application.Platform.runLater(() -> {
            resizeCanvas();
            setGameParameters(shikakuGame);
            commandExecutor.loadShikakuCommands(shikakuGame);

            saveGame(true);
            tabState.setIdOfGame(shikakuGame.getId());
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

    @Override
    protected void setGameParameters(Shikaku shikaku) {
        mouseKeyHandlers = new MouseKeyShikakuHandlers(shikaku, this);

        Map<SettingKey, GameSetting> settings = getGameSettingsAsMap(shikaku.getSettings());
        boolean shouldTimerExist = getTypedSetting(settings, SudokuSettings.TIMER);

        if (!shouldTimerExist) {
            buttonPause.setVisible(false);
            buttonPause.setManaged(false);
            if (countdownTimer != null) {
                countdownTimer.setVisible(false);
                countdownTimer.setManaged(false);
            }
        } else {
            buttonPause.setVisible(true);
            buttonPause.setManaged(true);
            buttonPause.setDisable(false);
            startTimer();
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
        for (Canvas c : new Canvas[]{primaryCanvas, secondaryCanvas}) {
            c.setWidth(actualCanvasWidth);
            c.setHeight(actualCanvasHeight);
            c.setLayoutX(0);
            c.setLayoutY(0);
        }

        redrawCanvases();

        redrawCanvases();
    }

    public void redrawCanvases() {
        ShikakuRenderer.render(primaryCanvas, gameInstance);

        RedrawCanvasFunctions.clearCanvasTransparent(secondaryCanvas);

        if (!correctHintRects.isEmpty() || !wrongHintRects.isEmpty()) {
            ShikakuRenderer.renderHintsOverlay(secondaryCanvas, gameInstance, correctHintRects, wrongHintRects);
        }

        drawShikakuPreview();
    }

    private void drawShikakuPreview() {
        if (mouseKeyHandlers == null) return;

        ShikakuCreationState creationState = this.getCreationState();
        ShikakuCell startCell = creationState.getStartCell();
        ShikakuCell endCell = creationState.getCurrentEndCell();

        if (startCell == null || endCell == null) return;

        GraphicsContext gc = secondaryCanvas.getGraphicsContext2D();

        double cellW = secondaryCanvas.getWidth() / gameInstance.getWidth();
        double cellH = secondaryCanvas.getHeight() / gameInstance.getHeight();
        double cellSize = Math.min(cellW, cellH);

        int minCol = Math.min(startCell.getCol(), endCell.getCol());
        int maxCol = Math.max(startCell.getCol(), endCell.getCol());
        int minRow = Math.min(startCell.getRow(), endCell.getRow());
        int maxRow = Math.max(startCell.getRow(), endCell.getRow());

        int colsCount = maxCol - minCol + 1;
        int rowsCount = maxRow - minRow + 1;

        double padding = cellSize * 0.05;
        double x = minCol * cellW + padding;
        double y = minRow * cellH + padding;
        double w = colsCount * cellW - (padding * 2);
        double h = rowsCount * cellH - (padding * 2);
        double cornerRadius = cellSize * 0.12;

        gc.save();

        gc.setFill(Color.web("#3B82F6", 0.25));
        gc.fillRoundRect(x, y, w, h, cornerRadius, cornerRadius);

        gc.setStroke(Color.web("#2563EB"));
        gc.setLineWidth(Math.max(2.5, cellSize * 0.06));
        gc.strokeRoundRect(x, y, w, h, cornerRadius, cornerRadius);

        int area = colsCount * rowsCount;
        if (area > 1) {
            gc.setFill(Color.web("#1E3A8A"));
            double fontSize = cellSize * 0.45;
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, fontSize));
            gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
            gc.setTextBaseline(javafx.geometry.VPos.CENTER);

            String sizeText = String.valueOf(area);
            gc.fillText(sizeText, x + (w / 2.0), y + (h / 2.0));
        }

        gc.restore();
    }

    @Override
    protected void handleHoverPreview(double timeSeconds) {
        RedrawCanvasFunctions.clearCanvasTransparent(secondaryCanvas);

        drawShikakuPreview();

        if (isHintChoice() && currentMouseX != null && currentMouseY != null) {
            getCellAt(this, gameInstance, currentMouseX, currentMouseY).ifPresent(hoveredCell -> {

                if (activeHint == ShikakuHintType.CHECK_VALIDITY) {
                    Optional<ShikakuRectangle> clickedRect = gameInstance.getRectangles().stream()
                            .filter(r -> hoveredCell.getRow() >= r.getMinRow() &&
                                    hoveredCell.getRow() <= r.getMaxRow() &&
                                    hoveredCell.getCol() >= r.getMinCol() &&
                                    hoveredCell.getCol() <= r.getMaxCol())
                            .findFirst();

                    if (clickedRect.isPresent()) {
                        ShikakuRenderer.drawPulsatingRectangle(secondaryCanvas, timeSeconds, gameInstance.getWidth(), gameInstance.getHeight(), clickedRect.get());
                    } else {
                        ShikakuRenderer.drawPulsatingCell(secondaryCanvas, timeSeconds, gameInstance.getWidth(), gameInstance.getHeight(), hoveredCell);
                    }
                } else {
                    ShikakuRenderer.drawPulsatingCell(secondaryCanvas, timeSeconds, gameInstance.getWidth(), gameInstance.getHeight(), hoveredCell);
                }
            });
        }
    }

    public void updateMousePosition(Double x, Double y) {
        this.currentMouseX = x;
        this.currentMouseY = y;
    }

    private Double currentMouseX = null;
    private Double currentMouseY = null;


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
            shikakuService.saveGame(gameInstance);
            tabState.setIdOfGame(gameInstance.getId());
            tabState.unsetChangePending();
        }
    }

    public void showPopOver() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cz/logicgo/ui/windows/popovers/HintPopOverShikaku.fxml"));
            Parent content = loader.load();
            HintPopOverShikakuController controller = loader.getController();

            PopOver popOver = new PopOver(content);
            controller.initialize(mainScreenController);
            content.getStyleClass().add("root");
            popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
            popOver.setAutoHide(true);
            popOver.setDetachable(false);

            Random random = new Random();
            controller.setOnSelect(type -> {
                this.setActiveHint(type);

                if (type == ShikakuHintType.INSERT_RANDOM_RECTANGLE) {
                    Shikaku shikaku = getGameInstance();
                    List<ShikakuRectangle> currentRects = shikaku.getRectangles();
                    List<ShikakuRectangle> solutionRects = shikaku.getSolutionRectangles();

                    List<ShikakuRectangle> missingRects = solutionRects.stream()
                            .filter(solRect -> currentRects.stream().noneMatch(curRect ->
                                    curRect.getMinRow() == solRect.getMinRow() &&
                                            curRect.getMinCol() == solRect.getMinCol() &&
                                            curRect.getMaxRow() == solRect.getMaxRow() &&
                                            curRect.getMaxCol() == solRect.getMaxCol()))
                            .toList();

                    if (!missingRects.isEmpty()) {
                        ShikakuRectangle randomMissing = missingRects.get(random.nextInt(missingRects.size()));

                        int newId = shikaku.generateNextRectangleId();

                        var startCell = shikaku.getCell(randomMissing.getMinRow(), randomMissing.getMinCol());
                        var endCell = shikaku.getCell(randomMissing.getMaxRow(), randomMissing.getMaxCol());

                        ShikakuRectangle rectToInsert = new ShikakuRectangle(newId, startCell, endCell);

                        List<ShikakuRectangle> intersectingRectangles = shikaku.getRectangles().stream()
                                .filter(r -> r.intersects(rectToInsert))
                                .toList();

                        Command command = new AddRectangleCommand(shikaku, rectToInsert, intersectingRectangles);
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
        List<ShikakuRectangle> oldRectangles = gameInstance.getRectangles();
        ArrayList<ShikakuRectangle> newRectangles = new ArrayList<>();
        int lastId = gameInstance.getCounter() != null ? gameInstance.getCounter() : 0;
        Command command = new RestartShikakuCommand(gameInstance, oldRectangles, newRectangles, lastId);
        commandExecutor.execute(command);
        redrawCanvases();
    }

    private final ShikakuCreationState creationState = new ShikakuCreationState();

    public ShikakuCreationState getCreationState() {
        return creationState;
    }

    @Override
    public void openHint() {
        hintChoice = true;
        showPopOver();
    }

    @FXML
    public void onMouseMoved(MouseEvent mouseEvent) {
        updateMousePosition(mouseEvent.getX(), mouseEvent.getY());
        mouseKeyHandlers.onMouseMoved(mouseEvent);
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
    public void onMouseDragged(MouseEvent mouseEvent) {
        mouseKeyHandlers.onMouseDragged(mouseEvent);
    }

    @FXML
    public void onKeyReleased(KeyEvent keyEvent) {
        mouseKeyHandlers.onKeyReleased(keyEvent);
    }

    @FXML
    public void onMousePressed(MouseEvent mouseEvent) {
        mouseKeyHandlers.onMousePressed(mouseEvent);
    }

    @Override
    public void terminateAllActiveActions() {
        if (mouseKeyHandlers != null) {
            this.getCreationState().unset();
            redrawCanvases();
        }
        super.terminateAllActiveActions();
    }

    @FXML
    public void onMouseExited(MouseEvent mouseEvent) {
        updateMousePosition(null, null);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public SettingService getSettingService() {
        return settingService;
    }

    public ShikakuHintType getActiveHint() {
        return activeHint;
    }

    public ShikakuGameController setActiveHint(ShikakuHintType activeHint) {
        this.activeHint = activeHint;
        return this;
    }

    private final List<ShikakuRectangle> correctHintRects = new ArrayList<>();
    private final List<ShikakuRectangle> wrongHintRects = new ArrayList<>();

    public List<ShikakuRectangle> getCorrectHintRects() {
        return correctHintRects;
    }

    public List<ShikakuRectangle> getWrongHintRects() {
        return wrongHintRects;
    }

    public void clearHintVisuals() {
        this.correctHintRects.clear();
        this.wrongHintRects.clear();
        this.setHintChoice(false);
        this.setActiveHint(null);

        RedrawCanvasFunctions.clearCanvasTransparent(secondaryCanvas);
        redrawCanvases();
    }
}
