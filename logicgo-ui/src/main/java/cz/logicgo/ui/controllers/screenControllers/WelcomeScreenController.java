package cz.logicgo.ui.controllers.screenControllers;

import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.factoryInit.GameInit;
import cz.logicgo.core.factoryInit.bridge.BridgeInit;
import cz.logicgo.core.factoryInit.maze.MazeInit;
import cz.logicgo.core.factoryInit.shikaku.ShikakuInit;
import cz.logicgo.core.factoryInit.sudoku.SudokuInit;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.core.misc.enums.*;
import cz.logicgo.core.misc.enums.gameTypes.bridge.BridgeType;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeAlgorithm;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.core.misc.enums.gameTypes.shikaku.ShikakuType;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.engine.algorithms.sudoku.custom.CustomLayoutsLoader;
import cz.logicgo.engine.util.generate.RandomizationFactory;
import cz.logicgo.engine.util.generate.SeedCreator;
import cz.logicgo.persistence.services.GameService;
import cz.logicgo.ui.StartOfApp;
import cz.logicgo.ui.controllers.ControllerClosable;
import cz.logicgo.ui.factories.BridgeGameFactory;
import cz.logicgo.ui.factories.MazeGameFactory;
import cz.logicgo.ui.factories.ShikakuGameFactory;
import cz.logicgo.ui.factories.SudokuGameFactory;
import cz.logicgo.ui.misc.tabChoosingClasses.LoadedGameWrapper;
import cz.logicgo.ui.misc.tabChoosingClasses.NewGameWrapper;
import cz.logicgo.ui.misc.windows.AlertBox;
import cz.logicgo.ui.renderers.BridgeRenderer;
import cz.logicgo.ui.renderers.ShikakuRenderer;
import cz.logicgo.ui.renderers.maze.MazeRenderer;
import cz.logicgo.ui.renderers.sudoku.SudokuRenderer;
import cz.logicgo.core.misc.formatter.FavoriteFormatter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

import static cz.logicgo.core.misc.Messages.getFormatted;
import static cz.logicgo.engine.util.generate.RandomizationFactory.genGameTypeBasedOnSeed;
import static cz.logicgo.engine.util.generate.RandomizationFactory.generateDailySeed;
import static cz.logicgo.ui.misc.SvgLoader.loadSvgToPane;


public class WelcomeScreenController implements Initializable, ControllerClosable {

    final private GameService gameService = new GameService();
    @FXML
    public Label welcomeLabel;
    @FXML
    public VBox lastPlayedBox;
    public VBox settingsButton;
    public VBox historyButton;
    public VBox gameButton;
    public VBox exportButton;
    public Canvas dailyGameCanvas;
    public Button dailyGameButton;
    public GridPane gridPane;
    public Label appTitle;
    public Label lastPlayedGameTime;
    public Button resumeLastGameButton;
    public Label lastPlayedGameTitle;
    public Label playBoxLabel;
    public Label historyBoxLabel;
    public Label settingsBoxLabel;
    public Label exportBoxLabel;
    public Label dailyGameTitle;
    public StackPane playIconPane;
    public StackPane historyIconPane;
    public StackPane settingsIconPane;
    public StackPane exportIconPane;
    public Pane dailyGamePane;
    @FXML
    public Button switchProfileButton;
    MainScreenController mainScreenController;
    private Game dailyGame;
    private long seedOfDailyGame;
    private Game lastPlayedGame;
    private User user;
    private Stage stage;

    public void initialize(User user, MainScreenController mainScreenController) {
        this.user = user;
        this.mainScreenController = mainScreenController;

        setIcons();
        setTextToFields(user);

        dailyGamePane.setMaxWidth(200);
        dailyGamePane.setMinWidth(200);
        dailyGamePane.setMinHeight(200);
        dailyGamePane.setMaxHeight(200);
        dailyGamePane.setPrefWidth(200);
        dailyGamePane.setPrefHeight(200);

        dailyGameCanvas.widthProperty().bind(dailyGamePane.widthProperty());
        dailyGameCanvas.heightProperty().bind(dailyGamePane.heightProperty());

        javafx.application.Platform.runLater(() -> {
            if (gridPane.getScene() != null) {
                gridPane.getScene().setOnKeyPressed(event -> {
                    if (event.getCode() == javafx.scene.input.KeyCode.ENTER || event.getCode() == javafx.scene.input.KeyCode.SPACE) {
                        if (lastPlayedGame != null) {
                            resumeLastGame();
                        } else if (dailyGameButton != null && !dailyGameButton.isDisable()) {
                            playDailyGameWrapper();
                        }
                    }
                });
            }
        });
    }

    @FXML
    public void playDailyGameWrapper() {
        if (dailyGameButton.isDisable()) return;
        try {
            playDailyGame();
        } catch (IOException e) {
            e.printStackTrace();
            AlertBox.OkWindowError("Nepodařilo se načíst denní hru.");
        }
    }

    private void setIcons() {
        loadSvgToPane(playIconPane, "/cz/logicgo/ui/images/svg/play.svg");
        loadSvgToPane(historyIconPane, "/cz/logicgo/ui/images/svg/history.svg");
        loadSvgToPane(settingsIconPane, "/cz/logicgo/ui/images/svg/settings.svg");
        loadSvgToPane(exportIconPane, "/cz/logicgo/ui/images/svg/file-export.svg");
    }

    private void setTextToFields(User user) {
        LocalDateTime lastLogged = user.getLastLogged();
        String formattedWelcome;
        if (lastLogged == null) {
            formattedWelcome = getFormatted("welcome.firstTime");
        } else {
            formattedWelcome = getFormatted("welcome.recent");
        }

        welcomeLabel.setText(formattedWelcome);

        playBoxLabel.setText(getFormatted("welcome.box.play"));
        historyBoxLabel.setText(getFormatted("welcome.box.history"));
        settingsBoxLabel.setText(getFormatted("welcome.box.settings"));
        exportBoxLabel.setText(getFormatted("welcome.box.export"));

        setupTooltips();

        refreshContent();
    }

    public void refreshContent() {
        lastPlayedGame = gameService.getLastPlayedNonFinishedGame(user);

        if (lastPlayedGame != null) {
            resumeLastGameButton.setText(getFormatted("welcome.lastGame.buttonContinue"));
            resumeLastGameButton.setDisable(false);

            lastPlayedGameTitle.setText(FavoriteFormatter.format(lastPlayedGame));
            lastPlayedGameTime.setText(getFormatted("welcome.lastGame.playedTime", lastPlayedGame.getFormattedLastPlayed()));
        } else {
            resumeLastGameButton.setText(getFormatted("welcome.lastGame.empty"));
            resumeLastGameButton.setDisable(true);

            lastPlayedGameTitle.setText(getFormatted("welcome.lastGame.nothing"));
            lastPlayedGameTime.setText("");
        }

        genDailyGame();
    }

    public void setupTooltips() {
        addTooltip(gameButton, "tooltip.welcome.play");
        addTooltip(switchProfileButton, "tooltip.welcome.exit");
        addTooltip(settingsButton, "tooltip.welcome.settings");
        addTooltip(historyButton, "tooltip.welcome.history");
        addTooltip(exportButton, "tooltip.welcome.export");
    }

    private void addTooltip(Node node, String key) {
        node.setOnMouseEntered(_ -> mainScreenController.setTextToLabel(getFormatted(key)));
        node.setOnMouseExited(_ -> mainScreenController.unsetTextToLabel());
    }

    public void playDailyGame() throws IOException {
        Game game = gameService.getExistingGameWithSeed(dailyGame.getSeed(), user);
        if (game == null) {
            String seedOfDaily = SeedCreator.createSeed(dailyGame);
            GameInit gameInit = SeedCreator.parseSeed(seedOfDaily, user);
            if (gameInit != null) {
                gameInit.setPlayer(user);
                gameInit.setOpenType(OpenType.SEEDED);
                TabType tabType = switch (gameInit) {
                    case SudokuInit _ -> TabType.SUDOKU_SETTINGS;
                    case BridgeInit _ -> TabType.BRIDGE_SETTINGS;
                    case MazeInit _ -> TabType.MAZE_SETTINGS;
                    case ShikakuInit _ -> TabType.SHIKAKU_SETTINGS;
                    default -> throw new IllegalStateException("Unexpected value: " + gameInit);
                };
                mainScreenController.openTab(tabType, new NewGameWrapper(gameInit));
            }
        } else {
            resumeDailyGame(game);
        }
    }

    @FXML
    public void logoutUser() {
        try {
            Stage currentStage = mainScreenController.getStage();

            FXMLLoader loader = new FXMLLoader(StartOfApp.class.getResource("/cz/logicgo/ui/windows/profile_selector.fxml"));
            Parent root = loader.load();

            ProfileSelectorController controller = loader.getController();
            controller.setStage(currentStage);

            Scene scene = new Scene(root, currentStage.getWidth(), currentStage.getHeight());
            currentStage.setScene(scene);
            currentStage.setTitle("LogicGo - Výběr profilu");

            controller.loadProfiles();

        } catch (Exception e) {
            e.printStackTrace();
            AlertBox.OkWindowError("Nepodařilo se odhlásit.");
        }
    }

    public void genDailyGame() {
        long baseSeed = generateDailySeed();

        if (dailyGame != null && seedOfDailyGame >= baseSeed && seedOfDailyGame < baseSeed + 100) {
            return;
        }

        dailyGameTitle.setText("Generuji...");
        dailyGameButton.setDisable(true);
        dailyGamePane.getChildren().clear();
        javafx.scene.control.ProgressIndicator progress = new javafx.scene.control.ProgressIndicator();
        progress.setMaxSize(40, 40);
        dailyGamePane.getChildren().add(progress);

        javafx.concurrent.Task<Game> dailyTask = new javafx.concurrent.Task<>() {
            @Override
            protected Game call() throws Exception {
                TypeGame typeGame = null;
                int add = 1;
                while (typeGame == null) {
                    typeGame = genGameTypeBasedOnSeed(baseSeed + add);
                    if (typeGame == null) add++;
                }

                long finalDailySeed = baseSeed + add;
                seedOfDailyGame = finalDailySeed;

                Game game = gameService.getExistingGameWithSeed(finalDailySeed, user);

                var init = switch (typeGame) {
                    case SUDOKU -> new SudokuInit();
                    case BRIDGE -> new BridgeInit();
                    case MAZE -> new MazeInit();
                    case SHIKAKU -> new ShikakuInit();
                };

                if (game == null) {
                    game = switch (typeGame) {
                        case MAZE -> {
                            MazeInit mazeInit = (MazeInit) init;
                            MazeType selectedType = RandomizationFactory.generateDailyMazeType(finalDailySeed);

                            MazeAlgorithm alg = selectedType.getSupportedAlgorithms().contains(MazeAlgorithm.RECURSIVE_BACKTRACKER)
                                    ? MazeAlgorithm.RECURSIVE_BACKTRACKER
                                    : selectedType.getSupportedAlgorithms().getFirst();

                            mazeInit.setDifficulty(Difficulty.MEDIUM)
                                    .setMazeAlgorithm(alg)
                                    .setMazeType(selectedType)
                                    .setMazeShape(MazeShape.RECTANGULAR)
                                    .setMask(null)
                                    .setHeight(25)
                                    .setWidth(25)
                                    .setPlayer(user)
                                    .setTypeGame(TypeGame.MAZE)
                                    .setSeed(finalDailySeed);

                            yield MazeGameFactory.createGame(mazeInit);
                        }
                        case BRIDGE -> {
                            BridgeInit bridgeInit = (BridgeInit) init;
                            BridgeType selectedType = BridgeType.CLASSIC;

                            bridgeInit.setDifficulty(Difficulty.HARD)
                                    .setBridgeType(selectedType)
                                    .setSeed(finalDailySeed)
                                    .setWidth(13)
                                    .setHeight(13);
                            yield BridgeGameFactory.createGame(bridgeInit);
                        }
                        case SUDOKU -> {
                            SudokuInit sudokuInit = (SudokuInit) init;
                            SudokuVariant selectedVariant = RandomizationFactory.generateDailySudokuVariant(finalDailySeed);

                            sudokuInit.setSeed(finalDailySeed)
                                    .setDifficulty(Difficulty.MEDIUM)
                                    .setSudokuVariant(selectedVariant)
                                    .setSudokuSize(SudokuSize.NINE)
                                    .setRegionLayout(CustomLayoutsLoader.getBasicLayout(SudokuSize.NINE))
                                    .setPatternLayout(CustomLayoutsLoader.getEmptyPatternLayout(SudokuSize.NINE));
                            yield SudokuGameFactory.createGame(sudokuInit);
                        }
                        case SHIKAKU -> {
                            ShikakuInit shikakuInit = (ShikakuInit) init;
                            ShikakuType selectedType = RandomizationFactory.generateDailyShikakuType(finalDailySeed);
                            int size = selectedType == ShikakuType.OFF_BY_ONE ? 10 : 12;

                            shikakuInit.setSeed(finalDailySeed)
                                    .setDifficulty(Difficulty.HARD)
                                    .setShikakuType(selectedType)
                                    .setWidth(size)
                                    .setHeight(size);
                            yield ShikakuGameFactory.createGame(shikakuInit);
                        }
                    };
                } else {
                    init.setId(game.getId());
                }
                return game;
            }
        };

        dailyTask.setOnSucceeded(_ -> {
            dailyGame = dailyTask.getValue();
            dailyGamePane.getChildren().clear();
            dailyGamePane.getChildren().add(dailyGameCanvas);

            try {
                switch (dailyGame) {
                    case Sudoku sudoku -> SudokuRenderer.renderFullBoard(dailyGameCanvas, sudoku, true);
                    case Bridge bridge -> BridgeRenderer.render(dailyGameCanvas, bridge, true);
                    case Maze maze -> {
                        int currentFloor = 0;
                        int totalFloors = maze.getMazeGridFloors().size();
                        MazeGrid grid = maze.getMazeGridFloors().get(currentFloor).getMazeGrid();

                        MazeRenderer.renderGrid(dailyGameCanvas, grid, currentFloor, totalFloors, false, user);
                        if (grid.getPath() != null) {
                            MazeRenderer.renderPath(dailyGameCanvas, grid.getPath(), grid, false);
                        }
                    }
                    case Shikaku shikaku -> ShikakuRenderer.render(dailyGameCanvas, shikaku);
                    default -> throw new IllegalStateException("Unexpected value: " + dailyGame);
                }

                Game existingDaily = gameService.getExistingGameWithSeed(dailyGame.getSeed(), user);

                if (existingDaily == null) {
                    dailyGameTitle.setText(getFormatted("welcome.dailyGame.title"));
                    dailyGameButton.setText(getFormatted("welcome.dailyGame.button.new"));
                    dailyGameButton.setDisable(false);
                } else if (existingDaily.getStatus() == Status.FINISHED) {
                    dailyGameTitle.setText(getFormatted("welcome.dailyGame.title.finished"));
                    dailyGameButton.setText(getFormatted("welcome.dailyGame.button.finished"));
                    dailyGameButton.setDisable(true);
                } else {
                    dailyGameTitle.setText(getFormatted("welcome.dailyGame.title.inProgress"));
                    dailyGameButton.setText(getFormatted("welcome.dailyGame.button.continue"));
                    dailyGameButton.setDisable(false);
                    dailyGame = existingDaily;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                dailyGameTitle.setText("Chyba zobrazení");
            }
        });

        dailyTask.setOnFailed(e -> {
            dailyTask.getException().printStackTrace();
            dailyGameTitle.setText("Chyba generování");
        });

        Thread th = new Thread(dailyTask);
        th.setDaemon(true);
        th.start();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }


    public void openMenu() throws IOException {
        if (mainScreenController.checkNotOpen(TabType.GAME_LIST)) {
            mainScreenController.openTab(TabType.GAME_LIST);
        }
    }

    public void openHistoryList() throws IOException {
        if (mainScreenController.checkNotOpen(TabType.HISTORY)) {
            mainScreenController.openTab(TabType.HISTORY);
        }
    }

    public void openExportButton() throws IOException {
        if (mainScreenController.checkNotOpen(TabType.EXPORT_MULTIPLE)) {
            mainScreenController.openTab(TabType.EXPORT_MULTIPLE);
        }
    }

    public void openSettingsButton() throws IOException {
        if (mainScreenController.checkNotOpen(TabType.SETTINGS)) {
            mainScreenController.openTab(TabType.SETTINGS);
        }
    }


    public void resumeLastGame() {
        resumeGame(lastPlayedGame);
    }

    public void resumeGame(Game gameWrap) {
        try {
            switch (gameWrap) {
                case Sudoku _ -> {
                    var init = new SudokuInit().setId(gameWrap.getId());
                    var game = SudokuGameFactory.createGame(init);
                    mainScreenController.openTab(TabType.SUDOKU, new LoadedGameWrapper(game));
                }
                case Bridge _ -> {
                    var init = new BridgeInit().setId(gameWrap.getId());
                    var game = BridgeGameFactory.createGame(init);
                    mainScreenController.openTab(TabType.BRIDGE, new LoadedGameWrapper(game));
                }
                case Maze _ -> {
                    var init = new MazeInit().setId(gameWrap.getId());
                    var game = MazeGameFactory.createGame(init);
                    mainScreenController.openTab(TabType.MAZE, new LoadedGameWrapper(game));
                }
                case Shikaku _ -> {
                    var init = new ShikakuInit().setId(gameWrap.getId());
                    var game = ShikakuGameFactory.createGame(init);
                    mainScreenController.openTab(TabType.SHIKAKU, new LoadedGameWrapper(game));
                }
                case null, default -> throw new IllegalStateException("Unexpected value: " + gameWrap);
            }
        } catch (Exception e) {

        }
    }

    public void resumeDailyGame(Game dailyGame) {
        resumeGame(dailyGame);
    }

    @Override
    public void terminateAllActiveActions() {

    }
}
