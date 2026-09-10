package cz.logicgo.ui.controllers.gameControllers.gameControllers;

import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.entity.setting.GameSetting;
import cz.logicgo.core.gameClasses.maze.MazeUtils;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.gameModes.*;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.core.gameClasses.maze.dataStructures.path.PlayerPath;
import cz.logicgo.core.misc.enums.Status;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.hints.MazeHintType;
import cz.logicgo.core.misc.enums.settings.MazeSettings;
import cz.logicgo.core.misc.enums.settings.SettingKey;
import cz.logicgo.persistence.services.MazeService;
import cz.logicgo.ui.commands.Command;
import cz.logicgo.ui.commands.CommandExecutor.SavedStacks;
import cz.logicgo.ui.commands.mazeCommands.MoveLevelCommand;
import cz.logicgo.ui.commands.mazeCommands.RestartMazeCommand;
import cz.logicgo.ui.controllers.gameControllers.AbstractGameController;
import cz.logicgo.ui.controllers.popover.HintPopOverMazeController;
import cz.logicgo.ui.controllers.screenControllers.MainScreenController;
import cz.logicgo.ui.handlers.mazeGame.MouseKeyMazeHandlers;
import cz.logicgo.ui.misc.NodeSnapshots;
import cz.logicgo.ui.misc.TabState;
import cz.logicgo.ui.misc.windows.AlertBox;
import cz.logicgo.ui.renderers.maze.MazeRenderer;
import cz.logicgo.ui.renderers.maze.MazeStrokeUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import org.controlsfx.control.PopOver;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static cz.logicgo.core.GameUtils.getGameSettingsAsMap;
import static cz.logicgo.core.misc.Messages.getFormatted;


public class MazeGameController extends AbstractGameController<Maze> {

    private final MazeService mazeService = new MazeService();
    private final List<MazeCell> hoveredMazeCells = new ArrayList<>();
    private MouseKeyMazeHandlers mouseKeyHandlers;
    private boolean hintChoice = false;
    private MazeHintType activeHint;
    @FXML
    private Button buttonFloorUp;
    @FXML
    private Button buttonFloorDown;
    @FXML
    private Label labelCurrentFloor;
    @FXML
    private Label labelModeInfo;
    @FXML
    private VBox patternPreviewBox;
    @FXML
    private Canvas patternCanvas;

    public boolean isHintChoice() {
        return hintChoice;
    }

    public void setHintChoice(boolean hintChoice) {
        this.hintChoice = hintChoice;
    }

    public MazeHintType getActiveHint() {
        return activeHint;
    }

    public void setActiveHint(MazeHintType activeHint) {
        this.activeHint = activeHint;
    }

    public void setHoveredMazeCells(MazeCell... cells) {
        hoveredMazeCells.clear();
        if (cells != null) {
            hoveredMazeCells.addAll(List.of(cells));
        }
    }

    @Override
    protected void handleHoverPreview(double timeSeconds) {
        var currentGrid = gameInstance.getMazeGridFloors().get(gameInstance.getCurrentFloor()).getMazeGrid();

        secondaryCanvas.getGraphicsContext2D().clearRect(0, 0, secondaryCanvas.getWidth(), secondaryCanvas.getHeight());
        MazeRenderer.renderPath(secondaryCanvas, currentGrid.getPath(), currentGrid, true);

        if (!hoveredMazeCells.isEmpty() && isHintChoice()) {
            GraphicsContext gc = secondaryCanvas.getGraphicsContext2D();
            double alpha = (Math.sin(timeSeconds * 5) + 1) / 2 * 0.4 + 0.2;
            gc.setFill(Color.rgb(255, 165, 0, alpha));

            int cols = currentGrid.getColCount();
            int rows = currentGrid.getRowCount();
            double cellW, cellH;

            if (currentGrid.getMazeShape() == MazeShape.HEXAGONAL) {
                double aw = 1.0 + 0.75 * (cols - 1);
                double ah = rows + (cols > 1 ? 0.5 : 0.0);
                cellW = secondaryCanvas.getWidth() / aw;
                cellH = secondaryCanvas.getHeight() / ah;
            } else {
                cellW = secondaryCanvas.getWidth() / cols;
                cellH = secondaryCanvas.getHeight() / rows;
            }

            for (MazeCell cell : hoveredMazeCells) {
                double cx, cy;
                if (currentGrid.getMazeShape() == MazeShape.HEXAGONAL) {
                    double bSize = cellH / 2.0;
                    cx = (cellW / 2.0) + (cell.getCol() * 0.75 * cellW);
                    cy = (cellH / 2.0) + (cell.getRow() * cellH);
                    if (cell.getCol() % 2 != 0) cy += bSize;
                } else {
                    cx = (cell.getCol() * cellW) + (cellW / 2.0);
                    cy = (cell.getRow() * cellH) + (cellH / 2.0);
                }

                double size = cellW * 0.6;
                gc.fillOval(cx - size / 2, cy - size / 2, size, size);
            }
        }
    }

    public void clearHintVisuals() {
        redrawCanvases();
    }

    public void initialize(Maze maze, MainScreenController mainScreenController, TabState tabState) {
        this.gameInstance = maze;
        this.mainScreenController = mainScreenController;
        this.tabState = tabState;

        buildToolbar(gameInstance);
        gamePane.setMinSize(0, 0);
        commandExecutor.loadMazeCommands(maze);

        setGameParameters(maze);

        refreshDataComponents();

        tabState.setChangePending();
        tabState.doneProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                saveGame(true);
                tabState.markSavingFinished();
            }
        });

        gamePane.widthProperty().addListener(this::changeBoard);
        gamePane.heightProperty().addListener(this::changeBoard);

        redrawCanvases();

        tabState.setIdOfGame(maze.getId());
        Tab tab = tabState.getAssociatedTab();
        tab.setClosable(true);
        tab.setOnCloseRequest((event) -> {
            tab.getTabPane().setTabDragPolicy(TabPane.TabDragPolicy.FIXED);
            event.consume();
            onExit();
        });

        Platform.runLater(() -> {
            resizeCanvas();
            saveGame(true);

            gamePane.setFocusTraversable(true);
            gamePane.requestFocus();
        });
    }

    @Override
    protected void setGameParameters(Maze maze) {
        Map<SettingKey, GameSetting> settings = getGameSettingsAsMap(maze.getSettings());

        maze.setMazeSettingToMazeGrids(MazeSettings.SHOW_TRAVEL_PATH);

        initFloorPath();

        mouseKeyHandlers = new MouseKeyMazeHandlers(maze, this);
        startTimer();
    }

    private boolean isFloorCompleted(int delta) {
        MazeGrid currentGrid = gameInstance.getMazeGridFloors().get(gameInstance.getCurrentFloor()).getMazeGrid();
        MazeCell currentCell = currentGrid.getPath().getActivePath().peek();

        if (currentCell == null) return false;

        if (delta == 1) {
            return currentCell == currentGrid.getEndCell();
        } else {
            return currentCell == currentGrid.getStartCell();
        }
    }

    @Override
    protected void resizeCanvas() {
        double paneWidth = gamePane.getWidth();
        double paneHeight = gamePane.getHeight();

        if (paneWidth == 0 || paneHeight == 0) return;

        double padding = 20.0;
        double availableWidth = paneWidth - padding;
        double availableHeight = paneHeight - padding;

        if (availableWidth <= 0 || availableHeight <= 0) return;

        int cols = gameInstance.getWidth();
        int rows = gameInstance.getHeight();

        double aw = 1.0;
        double ah = 1.0;
        double aspect = 1.0;

        switch (gameInstance.getMazeShape()) {
            case RECTANGULAR -> {
                aw = cols;
                ah = rows;
                aspect = 1.0;
            }
            case HEXAGONAL -> {
                aw = 1.0 + 0.75 * (cols - 1);
                ah = rows + (cols > 1 ? 0.5 : 0.0);
                aspect = Math.sqrt(3) / 2.0;
            }
        }

        double maxCellW = Math.min(availableWidth / aw, availableHeight / (ah * aspect));

        final double MIN_CELL_W = 15.0;
        final double MAX_CELL_W = 65.0;

        double cellW = maxCellW;

        if (cellW > MAX_CELL_W) cellW = MAX_CELL_W;
        if (cellW < MIN_CELL_W) cellW = MIN_CELL_W;

        double cellH = cellW * aspect;

        double actualCanvasWidth = cellW * aw;
        double actualCanvasHeight = cellH * ah;

        primaryCanvas.setWidth(actualCanvasWidth);
        primaryCanvas.setHeight(actualCanvasHeight);
        secondaryCanvas.setWidth(actualCanvasWidth);
        secondaryCanvas.setHeight(actualCanvasHeight);

        gamePane.setPrefSize(actualCanvasWidth, actualCanvasHeight);

        double offsetX = Math.max(0, (paneWidth - actualCanvasWidth) / 2.0);
        double offsetY = Math.max(0, (paneHeight - actualCanvasHeight) / 2.0);

        primaryCanvas.setLayoutX(offsetX);
        primaryCanvas.setLayoutY(offsetY);
        secondaryCanvas.setLayoutX(offsetX);
        secondaryCanvas.setLayoutY(offsetY);

        redrawCanvases();
    }

    public boolean cannotMove(int delta) {
        int targetFloor = gameInstance.getCurrentFloor() + delta;
        int maxFloor = gameInstance.getMazeGridFloors().size() - 1;

        return targetFloor < 0 || targetFloor > maxFloor;
    }

    @FXML
    public void onFloorUpPressed() {
        if (cannotMove(-1)) return;

        boolean wasCompleted = isFloorCompleted(-1);

        Command command = new MoveLevelCommand(gameInstance, -1);
        commandExecutor.execute(command);

        tabState.setChangePending();

        if (wasCompleted) {
            initFloorPath();
        }

        refreshDataComponents();
        redrawCanvases();
    }

    @FXML
    public void onFloorDownPressed() {
        if (cannotMove(1)) return;

        boolean wasCompleted = isFloorCompleted(1);

        Command command = new MoveLevelCommand(gameInstance, 1);
        commandExecutor.execute(command);

        tabState.setChangePending();

        if (wasCompleted) {
            initFloorPath();
        }

        refreshDataComponents();
        redrawCanvases();
    }

    public void refreshDataComponents() {
        if (gameInstance.isHasMultipleFloors()) {
            labelCurrentFloor.setVisible(true);
            buttonFloorUp.setVisible(true);
            buttonFloorDown.setVisible(true);
            labelCurrentFloor.setText(getFormatted("maze.mode.level", gameInstance.getCurrentFloor() + 1));
        } else {
            labelCurrentFloor.setVisible(false);
            buttonFloorUp.setVisible(false);
            buttonFloorDown.setVisible(false);
        }
        updateGameModeInfo();
    }

    private void updateGameModeInfo() {
        if (labelModeInfo == null) return;

        MazeGrid grid = gameInstance.getMazeGridFloors().get(gameInstance.getCurrentFloor()).getMazeGrid();
        boolean infoSet = false;

        if (patternPreviewBox != null) patternPreviewBox.setVisible(false);

        label:
        for (var mod : grid.getModifiers()) {
            switch (mod) {
                case WallModifier wallMod -> {
                    int walls = MazeUtils.countWalls(grid, wallMod);
                    labelModeInfo.setText(getFormatted("maze.mode.walls", walls, wallMod.getTrueWayWallCount()));
                    labelModeInfo.setVisible(true);
                    infoSet = true;
                    break label;
                }
                case CheckpointModifier orderedMod -> {
                    int collected = MazeUtils.countCollectedOrderedCheckpoints(grid, orderedMod);
                    int total = orderedMod.getCheckpoints().size();
                    labelModeInfo.setText(getFormatted("maze.mode.ordered_checkpoints", collected, total));
                    labelModeInfo.setVisible(true);
                    infoSet = true;
                    break label;
                }
                case TollModifier tollMod -> {
                    int coins = MazeUtils.countCurrentCoins(grid, tollMod);
                    labelModeInfo.setText(getFormatted("maze.mode.toll", coins));
                    labelModeInfo.setVisible(true);
                    infoSet = true;
                    break label;
                }
                case ExactStepsModifier exactStepsMod -> {
                    int steps = MazeUtils.countSteps(grid, exactStepsMod);
                    int total = exactStepsMod.targetSteps();
                    labelModeInfo.setText(getFormatted("maze.mode.exact_steps", steps, total));
                    labelModeInfo.setVisible(true);
                    infoSet = true;
                    break label;
                }
                case PatternModifier patternMod -> {
                    int progress = MazeUtils.countPatternProgress(grid, patternMod);
                    labelModeInfo.setText("");
                    labelModeInfo.setVisible(false);
                    infoSet = true;

                    if (patternPreviewBox != null && patternCanvas != null) {
                        patternPreviewBox.setVisible(true);
                        GraphicsContext pGc = patternCanvas.getGraphicsContext2D();
                        pGc.clearRect(0, 0, patternCanvas.getWidth(), patternCanvas.getHeight());

                        pGc.setFill(Color.web("#334155", 0.08));
                        pGc.fillRoundRect(2, 2, 56, 56, 12, 10);
                        pGc.setStroke(Color.web("#E2E8F0"));
                        pGc.setLineWidth(1.5);
                        pGc.strokeRoundRect(2, 2, 56, 56, 12, 10);

                        double cx = patternCanvas.getWidth() / 2.0;
                        double cy = patternCanvas.getHeight() / 2.0;
                        double shapeSize = 16.0;

                        pGc.save();
                        pGc.setLineWidth(3.0);
                        pGc.setLineCap(StrokeLineCap.ROUND);
                        pGc.setLineJoin(StrokeLineJoin.ROUND);

                        MazeStrokeUtils.strokePatternStuff(
                                pGc,
                                progress,
                                cx,
                                cy,
                                shapeSize
                        );

                        pGc.restore();
                    }
                    break label;
                }
                case null, default -> {
                }
            }
        }

        if (!infoSet) {
            labelModeInfo.setVisible(false);
        }
    }

    public void redrawCanvases() {
        redrawCanvases(true, true);
    }

    public void redrawCanvases(boolean primary, boolean secondary) {
        var currentGrid = gameInstance.getMazeGridFloors().get(gameInstance.getCurrentFloor()).getMazeGrid();
        int currentFloor = gameInstance.getCurrentFloor();
        int totalFloors = gameInstance.getMazeGridFloors().size();

        if (primary) {
            primaryCanvas.getGraphicsContext2D().clearRect(0, 0, primaryCanvas.getWidth(), primaryCanvas.getHeight());
            MazeRenderer.renderGrid(primaryCanvas, currentGrid, currentFloor, totalFloors, false, gameInstance.getPlayer());
        }

        if (secondary) {
            secondaryCanvas.getGraphicsContext2D().clearRect(0, 0, secondaryCanvas.getWidth(), secondaryCanvas.getHeight());
            MazeRenderer.renderPath(secondaryCanvas, currentGrid.getPath(), currentGrid, false);
        }

        updateGameModeInfo();
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
            mazeService.saveGame(gameInstance);
            tabState.setIdOfGame(gameInstance.getId());
            tabState.unsetChangePending();
        }
    }

    public void initFloorPath() {
        if (gameInstance == null) return;
        MazeGrid grid = gameInstance.getMazeGridFloors().get(gameInstance.getCurrentFloor()).getMazeGrid();

        if (grid.getPath() != null && !grid.getPath().getActivePath().isEmpty()) {
            return;
        }

        MazeCell start = grid.getStartCell();
        if (start == null && gameInstance.getStartCell() != null) {
            MazeCell globalStart = gameInstance.getStartCell();
            start = grid.getCell(globalStart.getRow(), globalStart.getCol());
        }

        if (start != null && grid.getPath() != null) {
            grid.getPath().move(start);
        }
    }

    @Override
    public void gameFinished() {
        gameInstance.setStatus(Status.FINISHED);
        pause();
        buttonPause.setDisable(true);
        buttonHint.setDisable(true);
        tabState.markSavingFinished();
        AlertBox.YouWonWindow(gameInstance.getElapsedTime());
        saveGame(true);
    }

    @Override
    protected void onUndo() {
        commandExecutor.undo();
        tabState.setChangePending();
        redrawCanvases();
    }

    @Override
    protected void onRedo() {
        commandExecutor.redo();
        tabState.setChangePending();
        redrawCanvases();
    }

    @Override
    public void openHint() {
        hintChoice = true;
        showPopOver();
    }

    public void showPopOver() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cz/logicgo/ui/windows/popovers/HintPopOverMaze.fxml"));
            Parent content = loader.load();

            HintPopOverMazeController controller = loader.getController();

            PopOver popOver = new PopOver(content);
            controller.initialize(mainScreenController);
            content.getStyleClass().add("root");
            popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
            popOver.setAutoHide(true);
            popOver.setDetachable(false);

            controller.setOnSelect(type -> {
                gameInstance.setMazeSettingToMazeGrids(type);

                redrawCanvases(false, true);

                startHintFeedbackTimer(3.0, () -> {
                    gameInstance.getMazeGridFloors().forEach(floor ->
                            floor.getMazeGrid().getPath().setMazeHintType(null)
                    );
                    redrawCanvases(false, true);
                });

                popOver.hide();
            });

            popOver.show(buttonHint);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void onRestart() {
        List<PlayerPath> oldPaths = gameInstance.getMazeGridFloors().stream()
                .map(floor -> floor.getMazeGrid().getPath())
                .toList();

        List<PlayerPath> newPaths = Stream.generate(PlayerPath::new).limit(oldPaths.size()).toList();

        IntStream.range(0, oldPaths.size()).forEach(i -> {
            var oldPath = oldPaths.get(i);
            var newPath = newPaths.get(i);
            newPath.getSolutionPath().addAll(oldPath.getSolutionPath());
        });

        Command command = new RestartMazeCommand(gameInstance, oldPaths, newPaths);
        commandExecutor.execute(command);
        redrawCanvases();
    }

    @FXML
    public void onMousePressed(MouseEvent mouseEvent) {
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
    public void onKeyReleased(KeyEvent keyEvent) {
        mouseKeyHandlers.onKeyReleased(keyEvent);
    }

    public MouseKeyMazeHandlers getMouseKeyHandlers() {
        return mouseKeyHandlers;
    }


    @Override
    public void terminateAllActiveActions() {
        super.terminateAllActiveActions();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
