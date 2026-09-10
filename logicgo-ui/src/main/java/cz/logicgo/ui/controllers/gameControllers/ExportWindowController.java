package cz.logicgo.ui.controllers.gameControllers;

import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.core.misc.enums.PreviewType;
import cz.logicgo.persistence.services.UserService;
import cz.logicgo.ui.controllers.exportControllers.ExportUtils;
import cz.logicgo.ui.controllers.screenControllers.MainScreenController;
import cz.logicgo.ui.misc.TabState;
import cz.logicgo.ui.renderers.BridgeRenderer;
import cz.logicgo.ui.renderers.ShikakuRenderer;
import cz.logicgo.ui.renderers.maze.MazeRenderer;
import cz.logicgo.ui.renderers.sudoku.ConstraintRenderer;
import cz.logicgo.ui.renderers.sudoku.SudokuRenderer;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.controlsfx.control.SegmentedButton;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static cz.logicgo.core.GameUtils.createNewInstance;
import static cz.logicgo.core.gameClasses.sudoku.SudokuUtils.hasOutsideClues;
import static cz.logicgo.core.misc.Messages.getFormatted;
import static cz.logicgo.ui.controllers.exportControllers.ExportUtils.getSaveFilePath;
import static cz.logicgo.ui.controllers.exportControllers.ExportUtils.render;
import static cz.logicgo.ui.controllers.helpers.ElementsSetterHelpers.setItemsPreviewTypeChoiceBox;
import static cz.logicgo.ui.controllers.listControllers.ItemsViewController.generateGameTitle;
import static cz.logicgo.core.misc.formatter.FavoriteFormatter.generateExportDescription;
import static cz.logicgo.ui.utils.GameUiUtils.openFile;


public class ExportWindowController implements Initializable {

    public Button buttonPrint;
    public Button buttonExport;
    public Canvas canvas;
    public VBox rightVbox;

    @FXML
    public Canvas clueCanvas;

    @FXML
    Pane pane;
    private Stage stage;

    private Game origGame;
    private Game emptyGame;
    private Game game;
    private User user;

    UserService userService = new UserService();

    private MainScreenController mainScreenController;

    Stage mainStage;

    private Label warningLabel;
    private Label exportTypeLabel;
    private Label typeLabel;
    private ToggleButton currentGameButton;
    private ToggleButton emptyGameButton;
    private SegmentedButton exportTypeSegmentedButton;
    private ChoiceBox<PreviewType> previewTypeChoiceBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void initialize(Game exported, User user, Stage mainStage, MainScreenController mainScreenController) {
        this.mainScreenController = mainScreenController;
        this.origGame = createNewInstance(exported);
        this.emptyGame = createNewInstance(exported);
        this.mainStage = mainStage;
        this.game = emptyGame;
        switch (emptyGame) {
            case Sudoku sudoku -> sudoku.setBoard(sudoku.getStartingBoard());
            case Bridge bridge -> bridge.setIslandBridges(new ArrayList<>());
            case Maze maze -> maze.getMazeGrid().getPath().getActivePath().clear();
            case Shikaku shikaku -> shikaku.setRectangles(new ArrayList<>());
            default -> {
            }
        }

        this.user = user;

        ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> {
            if (pane.getWidth() > 0 && pane.getHeight() > 0) {
                updateLayoutAndRedraw(game);
            }
        };
        pane.widthProperty().addListener(resizeListener);
        pane.heightProperty().addListener(resizeListener);

        setUp();

        updateLayoutAndRedraw(game);
    }

    @FXML
    private CheckBox generateHelpCheckBox;

    private void setUp() {
        warningLabel = new Label(getFormatted("game.export.warning"));
        warningLabel.setWrapText(true);
        warningLabel.setStyle("-fx-text-fill: red; -fx-font-style: italic;");
        warningLabel.setVisible(false);
        warningLabel.setMinHeight(60);
        warningLabel.setPrefHeight(60);

        exportTypeLabel = new Label(getFormatted("game.export.exportType"));

        currentGameButton = new ToggleButton(getFormatted("game.export.segmented.current"));
        emptyGameButton = new ToggleButton(getFormatted("game.export.segmented.empty"));
        currentGameButton.setPrefSize(120, 35);
        emptyGameButton.setPrefSize(120, 35);

        exportTypeSegmentedButton = new SegmentedButton(emptyGameButton, currentGameButton);
        exportTypeSegmentedButton.getStyleClass().add("segmented-button");
        setUpSegmentedButtonHandlingExport(exportTypeSegmentedButton, emptyGameButton);

        HBox exportRow = new HBox(15, exportTypeLabel, exportTypeSegmentedButton);
        exportRow.setAlignment(Pos.CENTER_LEFT);

        typeLabel = new Label(getFormatted("export.type.label"));
        typeLabel.setWrapText(true);

        previewTypeChoiceBox = new ChoiceBox<>();
        setItemsPreviewTypeChoiceBox(previewTypeChoiceBox);

        HBox typeRow = new HBox(15, typeLabel, previewTypeChoiceBox);

        generateHelpCheckBox.setText(getFormatted("export.checkbox.help"));

        buttonPrint.setText(getFormatted("export.generateButton.print"));
        buttonExport.setText(getFormatted("export.generateButton.export"));

        rightVbox.setSpacing(20);
        rightVbox.getChildren().clear();
        rightVbox.getChildren().addAll(typeRow, exportRow, generateHelpCheckBox, warningLabel);
    }

    private void setUpSegmentedButtonHandlingExport(SegmentedButton segmentedButton, ToggleButton defaultButton) {
        defaultButton.setSelected(true);

        segmentedButton.getToggleGroup().selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || oldValue == newValue) {
                oldValue.setSelected(true);
                return;
            }

            if (newValue == emptyGameButton) {
                game = emptyGame;
                updateLayoutAndRedraw(game);
                warningLabel.setVisible(false);

            } else if (newValue == currentGameButton) {
                game = origGame;
                updateLayoutAndRedraw(game);
                warningLabel.setVisible(true);
            }
        });
    }

    private void updateLayoutAndRedraw(Game targetGame) {
        if (targetGame == null || pane == null) return;
        double fullWidth = pane.getWidth();
        double fullHeight = pane.getHeight();

        boolean hasOutsideClues = game instanceof Sudoku sudoku && hasOutsideClues(sudoku);

        double paddingX = hasOutsideClues ? fullWidth * 0.05 : 0;
        double paddingY = hasOutsideClues ? fullHeight * 0.05 : 0;

        double gridWidth = fullWidth - 2 * paddingX;
        double gridHeight = fullHeight - 2 * paddingY;

        if (clueCanvas != null) {
            clueCanvas.setWidth(fullWidth);
            clueCanvas.setHeight(fullHeight);
            clueCanvas.setLayoutX(0);
            clueCanvas.setLayoutY(0);

            clueCanvas.getGraphicsContext2D().setFill(Color.web("#1e1e1e"));
            clueCanvas.getGraphicsContext2D().fillRect(0, 0, fullWidth, fullHeight);
        }

        if (canvas != null) {
            canvas.setWidth(gridWidth);
            canvas.setHeight(gridHeight);
            canvas.setLayoutX(paddingX);
            canvas.setLayoutY(paddingY);

            switch (targetGame) {
                case Sudoku sudoku -> SudokuRenderer.renderFullBoard(canvas, sudoku, true);
                case Bridge bridge -> BridgeRenderer.render(canvas, bridge, true);
                case Maze maze -> {
                    int currentFloor = maze.getCurrentFloor();
                    int totalFloors = maze.getMazeGridFloors().size();
                    MazeGrid grid = maze.getMazeGridFloors().get(currentFloor).getMazeGrid();

                    MazeRenderer.renderGrid(canvas, grid, currentFloor, totalFloors, false, user);
                    if (grid.getPath() != null) {
                        MazeRenderer.renderPath(canvas, grid.getPath(), grid, false);
                    }
                }
                case Shikaku shikaku -> ShikakuRenderer.render(canvas, shikaku);
                default -> {
                }
            }
        }

        if (hasOutsideClues && clueCanvas != null) {
            Sudoku sudoku = (Sudoku) targetGame;
            int size = sudoku.getType().getGridSize();
            double cellW = gridWidth / size;
            double cellH = gridHeight / size;

            ConstraintRenderer.drawOutsideModifiers(
                    clueCanvas.getGraphicsContext2D(), sudoku.getModifiers(), size,
                    cellW, cellH, paddingX, paddingY, gridWidth, gridHeight, false
            );
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void printAction() {
        try {
            Path tempPath = Files.createTempFile("logicgo-print-", ".pdf");
            tempPath.toFile().deleteOnExit();
            executeExportTask(true, tempPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportAction() {
        Path path = getSaveFilePath(user, stage);
        if (path != null) {
            executeExportTask(false, path);
        }
    }

    private void executeExportTask(boolean isPrintJob, Path... targetPath) {
        final String[] finalPath = new String[1];
        finalPath[0] = targetPath[0].toString();
        Task<Boolean> task = new Task<>() {

            @Override
            protected Boolean call() throws Exception {
                Platform.runLater(() -> stage.close());

                PreviewType previewType = previewTypeChoiceBox.getValue();
                Game copyOf = game;

                if (exportTypeSegmentedButton.getToggleGroup().getSelectedToggle() != null) {
                    var selected = exportTypeSegmentedButton.getToggleGroup().getSelectedToggle();
                    copyOf = selected == currentGameButton ? origGame : emptyGame;
                }

                ExportUtils.ImageWithGame result = render(copyOf, previewType);
                if (result.image() == null) {
                    return false;
                }

                handleSave(targetPath[0], isPrintJob);
                return true;
            }

            @Override
            protected void succeeded() {
                Boolean success = getValue();
                if (success != null && success && targetPath.length > 0) {
                    String path = targetPath[0].toString();
                    if (isPrintJob) {
                        openFile(path);
                    }
                }
            }

            @Override
            protected void failed() {
                Throwable e = getException();
                if (e != null) {

                    e.printStackTrace();
                }
            }
        };

        String suffix = isPrintJob ? getFormatted("export.task.printSuffix") : getFormatted("export.task.pdfSuffix");
        String desc = generateExportDescription(1, null, null, List.of(game)) + " " + suffix;

        if (mainStage != null && mainScreenController != null) {
            var activeTab = mainScreenController.tabPane.getSelectionModel().getSelectedItem();
            if (activeTab != null) {
                TabState activeState = mainScreenController.getActiveTabStates().stream()
                        .filter(s -> mainScreenController.getIdToMap().get(s.getId()) == activeTab)
                        .findFirst().orElse(null);

                if (activeState != null) {
                    activeState.setChangePending();
                    activeState.setActiveTask(task, desc);

                    task.stateProperty().addListener((obs, oldS, newS) -> {
                        if (newS == Worker.State.SUCCEEDED || newS == Worker.State.FAILED || newS == Worker.State.CANCELLED) {
                            activeState.markSavingFinished();
                        }
                    });
                }
            }
        }
        if (mainScreenController != null) {
            mainScreenController.registerExportTask(desc, task, finalPath[0]);
        }
    }

    private void handleSave(Path path, boolean forPrint) throws Exception {
        String titleText = generateGameTitle(game);
        PreviewType previewType = previewTypeChoiceBox.getValue();
        boolean wantHelp = generateHelpCheckBox.isSelected();

        ExportUtils.saveSingleGameToPdf(path, game, previewType, titleText, forPrint, wantHelp);
    }

    public Game getOrigGame() {
        return origGame;
    }

    public ExportWindowController setOrigGame(Game origGame) {
        this.origGame = origGame;
        return this;
    }
}
