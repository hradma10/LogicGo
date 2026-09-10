package cz.logicgo.ui.controllers.screenControllers;

import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.misc.enums.TabType;
import cz.logicgo.persistence.services.UserService;
import cz.logicgo.ui.StartOfApp;
import cz.logicgo.ui.controllers.ControllerClosable;
import cz.logicgo.ui.controllers.exportControllers.ExportMultipleGamesController;
import cz.logicgo.ui.controllers.gameControllers.AbstractGameController;
import cz.logicgo.ui.controllers.gameControllers.GameChoiceController;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.BridgeGameController;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.MazeGameController;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.ShikakuGameController;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.SudokuGameController;
import cz.logicgo.ui.controllers.listControllers.ItemsViewController;
import cz.logicgo.ui.controllers.menu.*;
import cz.logicgo.ui.controllers.settingsControllers.SettingsWindowController;
import cz.logicgo.ui.misc.OpenConfigManager;
import cz.logicgo.ui.misc.TabState;
import cz.logicgo.ui.misc.tabChoosingClasses.LoadedGameWrapper;
import cz.logicgo.ui.misc.tabChoosingClasses.TabChoiceWrapper;
import cz.logicgo.ui.misc.windows.AlertBox;
import cz.logicgo.ui.utils.GameUiUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.controlsfx.control.PopOver;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static cz.logicgo.core.misc.Messages.getFormatted;
import static cz.logicgo.ui.misc.SvgLoader.loadSvgToButton;


public class MainScreenController implements Initializable {

    private final Map<Tab, TabState> tabStateMap = new HashMap<>();
    final private Map<UUID, Tab> idToMap = new HashMap<>();
    private final Map<TabType, Parent> preloadedViews = new HashMap<>();
    private final Map<TabType, Object> preloadedControllers = new HashMap<>();
    private final Set<TabType> initializedWorkspaces = new HashSet<>();
    private final Map<String, ExportTaskWrapper> globalRunningTasks = new LinkedHashMap<>();
    private ExecutorService exportQueueExecutor = Executors.newSingleThreadExecutor();
    public Label lblNotificationBadge;
    @FXML
    public TabPane tabPane;
    public Label mainScreenTextHelp;
    public BorderPane root;
    Tab mainTab;
    UserService userService = new UserService();
    boolean skipSave = false;
    private User user;
    private Stage stage;
    @FXML
    private Button btnHome;
    @FXML
    private Button btnGameList;
    @FXML
    private Button btnHistory;
    @FXML
    private Button btnSettings;
    @FXML
    private Button btnExport;
    @FXML
    private Button btnRunningExports;
    private PopOver exportsPopOver;

    public MainScreenController() {

    }

    public long getActiveExportTasksCount() {
        return globalRunningTasks.values().stream()
                .map(ExportTaskWrapper::getTask)
                .filter(t -> t.isRunning() || t.getState() == javafx.concurrent.Worker.State.READY)
                .count();
    }

    public boolean isSkipSave() {
        return skipSave;
    }

    public MainScreenController setSkipSave(boolean skipSave) {
        this.skipSave = skipSave;
        return this;
    }

    @FXML
    public void logoutUser() {
        try {
            Stage currentStage = this.getStage();

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

    private void applyIcons() {
        loadSvgToButton(btnHome, "/cz/logicgo/ui/images/svg/home.svg");
        loadSvgToButton(btnGameList, "/cz/logicgo/ui/images/svg/play.svg");
        loadSvgToButton(btnHistory, "/cz/logicgo/ui/images/svg/history.svg");
        loadSvgToButton(btnSettings, "/cz/logicgo/ui/images/svg/settings.svg");
        loadSvgToButton(btnExport, "/cz/logicgo/ui/images/svg/file-export.svg");
        loadSvgToButton(btnRunningExports, "/cz/logicgo/ui/images/svg/download.svg");
    }

    private void applyTooltips() {
        addTooltip(btnHome, "tooltip.game.home_button");
        addTooltip(btnGameList, "tooltip.game.game_list_button");
        addTooltip(btnHistory, "tooltip.game.history_button");
        addTooltip(btnSettings, "tooltip.game.settings_button");
        addTooltip(btnExport, "tooltip.game.export_button");
        addTooltip(btnRunningExports, "tooltip.game.file_export");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    private void addTooltip(Node node, String key) {
        if (node == null) return;
        node.setOnMouseEntered(_ -> this.setTextToLabel(getFormatted(key)));
        node.setOnMouseExited(_ -> this.unsetTextToLabel());
    }

    @FXML
    public void onRunningExportsAction() {
        if (exportsPopOver == null) {
            exportsPopOver = new PopOver();
            exportsPopOver.setArrowLocation(PopOver.ArrowLocation.LEFT_CENTER);
            exportsPopOver.setDetachable(false);

            exportsPopOver.getRoot().setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        }

        if (exportsPopOver.isShowing()) {
            exportsPopOver.hide();
        } else {
            exportsPopOver.setContentNode(createExportsMenuContent());
            exportsPopOver.show(btnRunningExports);
        }
    }

    public void cancelAllExportTasks() {
        for (ExportTaskWrapper wrapper : globalRunningTasks.values()) {
            if (wrapper.getTask() != null) {
                wrapper.getTask().cancel(true);
            }
        }
        globalRunningTasks.clear();
        exportQueueExecutor.shutdownNow();
        recreateExportExecutor();
        Platform.runLater(this::updateNotificationBadge);
    }

    private void recreateExportExecutor() {
        try {
            if (!exportQueueExecutor.isTerminated()) {
                exportQueueExecutor.awaitTermination(100, java.util.concurrent.TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        exportQueueExecutor = Executors.newSingleThreadExecutor();
    }

    private VBox createExportsMenuContent() {
        VBox container = new VBox(12);
        container.setPadding(new javafx.geometry.Insets(15));
        container.setPrefWidth(420);
        container.setStyle("-fx-background-color: #2b2d30; -fx-background-radius: 6px;");

        if (globalRunningTasks.isEmpty()) {
            Label noExportsLabel = new Label(getFormatted("export.noRunningTasks"));
            noExportsLabel.setStyle("-fx-text-fill: #888888; -fx-font-style: italic;");
            container.getChildren().add(noExportsLabel);
            return container;
        }

        for (var entry : new ArrayList<>(globalRunningTasks.values())) {
            javafx.concurrent.Task<?> task = entry.getTask();

            HBox row = new HBox(12);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            HBox.setHgrow(row, Priority.ALWAYS);

            VBox textLabelBox = new VBox(4);
            textLabelBox.setPrefWidth(160);

            Label titleLabel = new Label(entry.getTitle());
            titleLabel.setStyle("-fx-text-fill: #dfe1e5; -fx-font-weight: bold; -fx-font-size: 13px;");
            titleLabel.setWrapText(true);

            Label detailLabel = new Label();
            detailLabel.setStyle("-fx-text-fill: #b9bed1; -fx-font-size: 11px;");
            detailLabel.setWrapText(true);

            task.stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.READY) {
                    detailLabel.textProperty().unbind();
                    detailLabel.setText(getFormatted("export.waiting"));
                } else if (newState == javafx.concurrent.Worker.State.RUNNING) {
                    detailLabel.textProperty().bind(task.messageProperty());
                } else {
                    detailLabel.textProperty().unbind();
                    detailLabel.setText("");
                }
            });

            if (task.getState() == javafx.concurrent.Worker.State.READY) {
                detailLabel.setText(getFormatted("export.waiting"));
            } else if (task.getState() == javafx.concurrent.Worker.State.RUNNING) {
                detailLabel.textProperty().bind(task.messageProperty());
            } else {
                detailLabel.setText("");
            }

            textLabelBox.getChildren().addAll(titleLabel, detailLabel);

            Node progressNode = null;
            if (task.isRunning() || task.getState() == javafx.concurrent.Worker.State.READY) {
                ProgressBar bar = new ProgressBar();
                bar.setPrefWidth(80);
                bar.progressProperty().bind(task.progressProperty());
                progressNode = bar;
            } else {
                Pane spacer = new Pane();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                progressNode = spacer;
            }

            HBox actionBox = new HBox(6);
            actionBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

            if (task.getState() == javafx.concurrent.Worker.State.READY || task.isRunning()) {
                Button cancelBtn = new Button(getFormatted("export.generateButton.cancel"));
                cancelBtn.getStyleClass().add("btn-delete");
                cancelBtn.setOnAction(e -> {
                    task.cancel(true);

                    globalRunningTasks.remove(entry.getId());

                    updateNotificationBadge();
                    if (exportsPopOver != null && exportsPopOver.isShowing()) {
                        exportsPopOver.setContentNode(createExportsMenuContent());
                    }
                });
                actionBox.getChildren().add(cancelBtn);

            } else if (task.getState() == javafx.concurrent.Worker.State.SUCCEEDED) {
                Button openBtn = new Button(getFormatted("export.generation.open"));
                openBtn.getStyleClass().add("btn-primary");
                openBtn.setOnAction(e -> {
                    String path = entry.getPath();
                    if (path != null && !path.isEmpty() && !path.isBlank()) {
                        GameUiUtils.openFile(path);
                    }
                });

                Button deleteBtn = new Button("✕");
                deleteBtn.getStyleClass().add("btn-delete");
                deleteBtn.setPadding(new javafx.geometry.Insets(4, 9, 4, 9));
                deleteBtn.setOnAction(e -> {
                    globalRunningTasks.remove(entry.getId());
                    updateNotificationBadge();
                    if (exportsPopOver != null && exportsPopOver.isShowing()) {
                        exportsPopOver.setContentNode(createExportsMenuContent());
                    }
                });

                actionBox.getChildren().addAll(openBtn, deleteBtn);

            } else {
                Button deleteBtn = new Button("✕");
                deleteBtn.getStyleClass().add("btn-delete");
                deleteBtn.setPadding(new javafx.geometry.Insets(4, 9, 4, 9));
                deleteBtn.setOnAction(e -> {
                    globalRunningTasks.remove(entry.getId());
                    updateNotificationBadge();
                    if (exportsPopOver != null && exportsPopOver.isShowing()) {
                        exportsPopOver.setContentNode(createExportsMenuContent());
                    }
                });
                actionBox.getChildren().add(deleteBtn);
            }

            row.getChildren().addAll(textLabelBox, progressNode, actionBox);
            container.getChildren().add(row);
        }

        return container;
    }

    public void registerExportTask(String description, javafx.concurrent.Task<?> task, String exportPath) {
        if (task == null) return;

        String uniqueId = UUID.randomUUID().toString();
        ExportTaskWrapper wrapper = new ExportTaskWrapper(uniqueId, description, task, exportPath);
        globalRunningTasks.put(uniqueId, wrapper);

        task.stateProperty().addListener((obs, oldState, newState) -> {
            Platform.runLater(() -> {
                updateNotificationBadge();
                if (exportsPopOver != null && exportsPopOver.isShowing()) {
                    exportsPopOver.setContentNode(createExportsMenuContent());
                }
            });
        });

        exportQueueExecutor.submit(task);
        Platform.runLater(this::updateNotificationBadge);
    }

    private void updateNotificationBadge() {
        if (lblNotificationBadge == null) return;

        long activeCount = globalRunningTasks.values().stream()
                .map(ExportTaskWrapper::getTask)
                .filter(t -> t.isRunning() || t.getState() == javafx.concurrent.Worker.State.READY)
                .count();

        if (activeCount > 0) {
            lblNotificationBadge.setText(String.valueOf(activeCount));
            lblNotificationBadge.setVisible(true);
        } else {
            lblNotificationBadge.setVisible(false);
        }
    }

    private String getResourcePathForPreloaded(TabType type){
        return switch (type) {
            case MAIN -> "/cz/logicgo/ui/windows/welcome_screen.fxml";
            case GAME_LIST -> "/cz/logicgo/ui/windows/gameChoice/game_choice.fxml";
            case HISTORY -> "/cz/logicgo/ui/windows/list/history_view.fxml";
            case SETTINGS -> "/cz/logicgo/ui/windows/settings/settings_screen.fxml";
            case EXPORT_MULTIPLE -> "/cz/logicgo/ui/windows/exportMultiple/export_multiple_games.fxml";
            default -> null;
        };
    }

    public void preloadWorkspaceViews() {
        List<TabType> statics = List.of(TabType.MAIN, TabType.GAME_LIST, TabType.HISTORY, TabType.SETTINGS, TabType.EXPORT_MULTIPLE);

        for (TabType type : statics) {
            Platform.runLater(() -> {
                try {
                    String resourcePath = getResourcePathForPreloaded(type);

                    if (resourcePath != null && !preloadedViews.containsKey(type)) {
                        FXMLLoader loader = new FXMLLoader(StartOfApp.class.getResource(resourcePath));
                        Parent content = loader.load();
                        content.getStyleClass().add("root");

                        preloadedViews.put(type, content);
                        preloadedControllers.put(type, loader.getController());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void switchToWorkspace(TabType tabType) {
        try {
            Tab workspaceTab = null;
            try {
                workspaceTab = tabPane.getTabs().getFirst();
            } catch (NoSuchElementException e) {
                workspaceTab = null;
            }

            if (workspaceTab == null) {
                Tab initialTab = new Tab(tabType.getTranslation());
                initialTab.setClosable(false);
                initialTab.setUserData(tabType);
                tabPane.getTabs().addFirst(initialTab);
                workspaceTab = initialTab;
            }

            if (workspaceTab.getUserData() == tabType && workspaceTab.getContent() != null) {
                tabPane.getSelectionModel().select(workspaceTab);
                return;
            }

            Parent content;
            Object controller;

            if (preloadedViews.containsKey(tabType)) {
                content = preloadedViews.get(tabType);
                controller = preloadedControllers.get(tabType);
            } else {
                String resourcePath = getResourcePathForPreloaded(tabType);

                FXMLLoader loader = new FXMLLoader(StartOfApp.class.getResource(resourcePath));
                content = loader.load();
                content.getStyleClass().add("root");
                controller = loader.getController();

                preloadedViews.put(tabType, content);
                preloadedControllers.put(tabType, controller);
            }

            TabState tabState = new TabState(workspaceTab);
            tabState.setAssociatedController((ControllerClosable) controller);

            if (!initializedWorkspaces.contains(tabType)) {
                switch (controller) {
                    case WelcomeScreenController c -> {
                        c.initialize(user, this);
                        c.setStage(stage);
                    }
                    case GameChoiceController c -> c.initialize(user, this);
                    case ItemsViewController c -> c.initialize(user, this);
                    case SettingsWindowController c -> c.initialize(user, this);
                    case ExportMultipleGamesController c -> c.initialize(user, this, tabState);
                    default -> {
                    }
                }
                initializedWorkspaces.add(tabType);
            }

            if (controller instanceof ControllerClosable controllerClosable) {
                controllerClosable.refreshContent();
            }


            workspaceTab.setContent(content);
            workspaceTab.setText(tabType.getTranslation());
            workspaceTab.setUserData(tabType);

            tabStateMap.put(workspaceTab, tabState);
            idToMap.put(tabState.getId(), workspaceTab);

            tabPane.getSelectionModel().select(workspaceTab);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onHomeAction() {
        switchToWorkspace(TabType.MAIN);
    }

    @FXML
    public void onGameListAction() {
        switchToWorkspace(TabType.GAME_LIST);
    }

    @FXML
    public void onHistoryAction() {
        switchToWorkspace(TabType.HISTORY);
    }

    @FXML
    public void onSettingsAction() {
        switchToWorkspace(TabType.SETTINGS);
    }

    @FXML
    public void onExportAction() {
        switchToWorkspace(TabType.EXPORT_MULTIPLE);
    }

    public Tab getMainTab() {
        return mainTab;
    }

    public void initialize(User loggedUser) throws IOException {
        this.user = loggedUser;

        applyIcons();
        applyTooltips();
        preloadWorkspaceViews();
        openMain();

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                TabState state = tabStateMap.get(newTab);
                if (state != null) {
                    switch (state.getAssociatedController()) {
                        case WelcomeScreenController welcomeController -> welcomeController.refreshContent();
                        case null, default -> {
                        }
                    }
                }
            }
        });
        tabPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);


        tabPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getSource() == tabPane && event.getTarget() != tabPane) {
                return;
            }

            Tab selected = tabPane.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Node content = selected.getContent();
                if (content == null) return;
                switch ((TabType) selected.getUserData()) {
                    case SUDOKU, MAZE, BRIDGE -> {
                        Node target = content.lookup("#gamePane");

                        if (target instanceof Pane pane) {
                            pane.fireEvent(event);
                        }
                        event.consume();
                    }
                    default -> event.consume();
                }
            }
        });

        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode code = event.getCode();

            if ((event.isControlDown() && (code == KeyCode.TAB || code == KeyCode.PAGE_DOWN || code == KeyCode.PAGE_UP || code == KeyCode.RIGHT || code == KeyCode.LEFT))
                    || (event.isAltDown() && (code == KeyCode.RIGHT || code == KeyCode.LEFT))) {
                event.consume();
            }
        });

        stage.setOnCloseRequest(event -> {
            event.consume();

            long activeExports = getActiveExportTasksCount();

            if (activeExports > 0) {
                boolean forceClose = AlertBox.initCloseWithRunningExports(activeExports);
                if (!forceClose) {
                    return;
                }
                cancelAllExportTasks();
            }

            if (getActiveTabStates().isEmpty() || AlertBox.initCloseApp()) {
                close();
            }
        });
    }

    public Tab openTab(TabType tabType) throws IOException {
        return openTab(tabType, null);
    }

    public void switchToTab(Tab tab) {
        for (Tab t : tabPane.getTabs()) {
            if (t == tab) {
                tabPane.getSelectionModel().select(tab);
            }
        }
    }

    public boolean checkNotOpen(TabType tabType) {
        for (Tab t : tabPane.getTabs()) {
            if (t.getUserData() == tabType) {
                tabPane.getSelectionModel().select(t);
                return false;
            }
        }
        return true;
    }

    public int closeTab(Tab tab) {
        int index = tabPane.getTabs().indexOf(tab);
        if (index != -1) {
            tabPane.getTabs().remove(tab);
        }
        TabState state = tabStateMap.remove(tab);
        state.terminateActions();
        return index;
    }

    public void closeTabWithId(UUID id) {
        Tab tab = idToMap.get(id);
        closeTab(tab);
    }

    public void close() {
        cancelAllExportTasks();

        ArrayList<TabState> tabStates = new ArrayList<>();

        if (!skipSave) {
            tabStates = getActiveTabStates();

            for (TabState tabState : tabStates) {
                tabState.markSavingStarted();
                tabState.terminateActions();
            }
        }

        watchUntilAllDone(() -> {
            if (skipSave) {
                preloadedControllers.clear();
                preloadedViews.clear();
                logoutUser();
            } else {
                try {
                    user.setLastLogged(LocalDateTime.now());
                    userService.updateUser(user);
                } catch (Exception _) {}

                try {
                    OpenConfigManager.writeWindowPosition(stage);
                } catch (Exception _) {}

                stage.close();
                Platform.exit();
                System.exit(0);
            }
        }, tabStates);
    }

    public void setTextToLabel(String text) {
        mainScreenTextHelp.setText(text);
    }

    public void unsetTextToLabel() {
        mainScreenTextHelp.setText("");
    }

    public void closeCurrentAndReplace(TabType tabType, TabChoiceWrapper tabChoiceWrapper) throws IOException {
        Tab current = tabPane.getSelectionModel().getSelectedItem();
        if (current == null) return;

        int index = closeTab(current);
        if (index > tabPane.getTabs().size()) {
            index = tabPane.getTabs().size();
        }

        openTab(tabType, tabChoiceWrapper, index);
    }

    public Tab openTab(TabType tabType, TabChoiceWrapper tabChoiceWrapper) throws IOException {
        return openTab(tabType, tabChoiceWrapper, null);
    }

    public void openMain() throws IOException {
        switchToWorkspace(TabType.MAIN);
    }

    public Tab openTab(TabType tabType, TabChoiceWrapper tabChoiceWrapper, Integer index) throws IOException {

        if (!tabPane.getTabs().isEmpty()) {
            switch (tabType) {
                case MAIN, GAME_LIST, HISTORY, SETTINGS, EXPORT_MULTIPLE -> {
                    switchToWorkspace(tabType);
                    return tabPane.getTabs().getFirst();
                }
            }

            Long targetGameId = null;
            if (tabChoiceWrapper instanceof LoadedGameWrapper(Game game) && game != null) {
                targetGameId = game.getId();
            }

            if (targetGameId != null) {
                for (Map.Entry<Tab, TabState> entry : tabStateMap.entrySet()) {
                    if (targetGameId.equals(entry.getValue().getIdOfGame())) {
                        tabPane.getSelectionModel().select(entry.getKey());
                        return entry.getKey();
                    }
                }
            }
        }

        if (tabPane.getTabs().size() > 12) {
            AlertBox.showBasicInfoBox("Příliš mnoho otevřených oken", "Před otevřením další hry prosím zavřete některé ze stávajících her.", "", Alert.AlertType.WARNING);
            return null;
        }

        Tab tab;
        TabState tabState;

        switch (tabType) {
            case SUDOKU_SETTINGS, MAZE_SETTINGS, BRIDGE_SETTINGS, SHIKAKU_SETTINGS -> {
                String resourcePath = "/cz/logicgo/ui/windows/gameChoice/game_settings_menu.fxml";
                FXMLLoader loader = new FXMLLoader(StartOfApp.class.getResource(resourcePath));

                Object controller = switch (tabType) {
                    case SUDOKU_SETTINGS -> new SudokuSettingsMenu();
                    case MAZE_SETTINGS -> new MazeSettingsMenu();
                    case BRIDGE_SETTINGS -> new BridgeSettingsMenu();
                    case SHIKAKU_SETTINGS -> new ShikakuSettingsMenu();
                    default -> throw new IllegalStateException();
                };

                loader.setController(controller);
                Parent content = loader.load();
                content.getStyleClass().add("root");

                tab = new Tab(tabType.getTranslation(), content);
                tabState = new TabState(tab);
                tabState.setAssociatedController((ControllerClosable) controller);

                if (controller instanceof AbstractSettingsMenu<?> menuController) {
                    menuController.initialize(tabState, user, this, tabChoiceWrapper);
                    tab.setClosable(true);
                }
            }

            case SUDOKU, MAZE, BRIDGE, SHIKAKU -> {
                String resourcePath = switch (tabType) {
                    case SUDOKU -> "/cz/logicgo/ui/windows/sudoku_game.fxml";
                    case MAZE -> "/cz/logicgo/ui/windows/maze_game.fxml";
                    case BRIDGE -> "/cz/logicgo/ui/windows/bridge_game.fxml";
                    case SHIKAKU -> "/cz/logicgo/ui/windows/shikaku_game.fxml";
                    default -> throw new IllegalStateException();
                };

                FXMLLoader loader = new FXMLLoader(StartOfApp.class.getResource(resourcePath));
                Parent content = loader.load();
                content.getStyleClass().add("root");

                tab = new Tab(tabType.getTranslation(), content);
                tabState = new TabState(tab);
                Object controller = loader.getController();
                tabState.setAssociatedController((ControllerClosable) controller);

                if (tabChoiceWrapper instanceof LoadedGameWrapper(Game game)) {
                    switch (controller) {
                        case SudokuGameController c when game instanceof Sudoku s -> c.initialize(s, this, tabState);
                        case MazeGameController c when game instanceof Maze m -> c.initialize(m, this, tabState);
                        case BridgeGameController c when game instanceof Bridge b -> c.initialize(b, this, tabState);
                        case ShikakuGameController c when game instanceof Shikaku sh -> c.initialize(sh, this, tabState);
                        default -> throw new IllegalArgumentException();
                    }

                    if (controller instanceof AbstractGameController<?> gameController) {
                        gameController.setStage(stage);
                    }
                    tab.setClosable(true);
                }
            }

            default -> {
                return null;
            }
        }

        tabStateMap.put(tab, tabState);
        idToMap.put(tabState.getId(), tab);
        tab.setUserData(tabType);

        if (index != null && index >= 0 && index <= tabPane.getTabs().size()) {
            tabPane.getTabs().add(index, tab);
        } else {
            tabPane.getTabs().add(tab);
        }
        tabPane.getSelectionModel().select(tab);

        final TabState finalTabState = tabState;
        final Tab finalTab = tab;

        tab.setOnCloseRequest(event -> {
            if (finalTabState.getAssociatedController() != null) {
                finalTabState.getAssociatedController().terminateAllActiveActions();
            }

            if (!finalTabState.isDone()) {
                event.consume();
                boolean close = AlertBox.initCloseGame();
                if (close) {
                    this.closeTab(finalTab);
                }
            } else {
                tabStateMap.remove(finalTab);
            }
        });

        return tab;
    }

    public ArrayList<TabState> getActiveTabStates() {
        return tabStateMap.values()
                .stream().filter(TabState::isChangePending)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private boolean allDone(ArrayList<TabState> tabStates) {
        return tabStates.stream().allMatch(TabState::isDone);
    }

    public void watchUntilAllDone(Runnable onAllDone, ArrayList<TabState> tabStates) {
        if (allDone(tabStates)) {
            onAllDone.run();
            return;
        }

        for (TabState state : tabStates) {
            state.doneProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    if (allDone(tabStates)) {
                        onAllDone.run();
                    }
                }
            });
        }
    }

    public User getUser() {
        return user;
    }

    private void setUser(User user) {
        this.user = user;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Map<UUID, Tab> getIdToMap() {
        return idToMap;
    }

    public static class ExportTaskWrapper {
        private final String id;
        private final String title;
        private final javafx.concurrent.Task<?> task;
        private String detailedStatus = "";
        private String path = "";

        public ExportTaskWrapper(String id, String title, javafx.concurrent.Task<?> task, String path) {
            this.id = id;
            this.title = title;
            this.path = path;
            this.task = task;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public javafx.concurrent.Task<?> getTask() {
            return task;
        }

        public String getDetailedStatus() {
            return detailedStatus;
        }

        public void setDetailedStatus(String status) {
            this.detailedStatus = status;
        }

        public String getPath() {
            return path;
        }

        public ExportTaskWrapper setPath(String path) {
            this.path = path;
            return this;
        }
    }
}
