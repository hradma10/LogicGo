package cz.logicgo.ui.controllers.exportControllers;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;
import cz.logicgo.core.entity.export.ExportDetail;
import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.gameClasses.export.ExportDetailsLocal;
import cz.logicgo.core.gameClasses.export.gameTypes.GameMode;
import cz.logicgo.core.gameClasses.export.gameTypes.MazeTypes;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.core.misc.enums.PageLayout;
import cz.logicgo.core.misc.enums.PreviewType;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.engine.export.ruleGen.RulesGen;
import cz.logicgo.engine.util.generate.RandomizationFactory;
import cz.logicgo.persistence.services.ExportedGameService;
import cz.logicgo.persistence.services.GameService;
import cz.logicgo.persistence.services.SettingService;
import cz.logicgo.persistence.services.UserSettingsService;
import cz.logicgo.ui.controllers.ControllerClosable;
import cz.logicgo.ui.controllers.exportControllers.cart.CartItemCell;
import cz.logicgo.ui.controllers.exportControllers.exportTabs.*;
import cz.logicgo.ui.controllers.listControllers.ExportedItemsCell;
import cz.logicgo.ui.controllers.screenControllers.MainScreenController;
import cz.logicgo.ui.misc.ExportFileService;
import cz.logicgo.ui.misc.TabState;
import cz.logicgo.ui.misc.windows.AlertBox;
import cz.logicgo.ui.renderers.BridgeRenderer;
import cz.logicgo.ui.renderers.ShikakuRenderer;
import cz.logicgo.ui.renderers.sudoku.ConstraintRenderer;
import cz.logicgo.ui.renderers.sudoku.SudokuRenderer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static cz.logicgo.core.misc.Messages.getFormatted;
import static cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape.HEXAGONAL;
import static cz.logicgo.ui.controllers.exportControllers.ExportUtils.getSaveFilePath;
import static cz.logicgo.ui.controllers.helpers.ElementsSetterHelpers.setItemsLayoutChoiceBox;
import static cz.logicgo.ui.controllers.helpers.ElementsSetterHelpers.setItemsPreviewTypeChoiceBox;
import static cz.logicgo.ui.controllers.listControllers.ItemsViewController.generateGameTitle;
import static cz.logicgo.ui.renderers.maze.MazeRenderer.renderGrid;
import static cz.logicgo.ui.renderers.maze.MazeRenderer.renderPath;
import static cz.logicgo.core.misc.formatter.FavoriteFormatter.generateExportDescription;


public class ExportMultipleGamesController implements ControllerClosable {
    public static final int MAX_GENERATED = 200;
    private static final int ITEMS_PER_PAGE = 10;
    private static final ExecutorService VT_EXECUTOR =
            Executors.newVirtualThreadPerTaskExecutor();
    private final int[] progressCount = new int[2];

    @FXML
    private Canvas canvas;
    @FXML
    private StackPane canvasContainer;
    final private UserSettingsService userSettingsService = new UserSettingsService();
    @FXML
    public Pagination pagination;
    @FXML
    public Button okButton;
    @FXML
    public TabPane gameTabPane;
    @FXML
    public Tab sudokuTab;
    @FXML
    public Tab bridgeTab;
    @FXML
    public Tab mazeTab;
    @FXML
    public Tab shikakuTab;
    @FXML
    public Tab multiTab;
    @FXML
    public CheckBox generateHelpCheckBox;
    boolean removeFromList = true;
    ExportDetail currentExportDetail;
    Task<List<Game>> task;
    SettingService settingService = new SettingService();
    GameService gameService = new GameService();
    ExportedGameService exportedGameService = new ExportedGameService();
    @FXML
    private VBox historySidebar;
    @FXML
    private ListView<ExportDetail> exportedListView;
    private MainScreenController mainScreenController;
    private TabState tabState;
    private User user;
    private List<ExportDetail> allEntries;
    private SudokuTabController sudokuController;
    private BridgeTabController bridgeController;
    private ShikakuTabController shikakuController;
    private MazeTabController mazeController;
    private FavoriteTabController favoriteTabController;
    @FXML
    private Label previewTypeLabel;
    @FXML
    private ChoiceBox<PreviewType> previewTypeChoiceBox;
    @FXML
    private final ObservableList<ExportItem> list = FXCollections.observableArrayList();
    @FXML
    private ChoiceBox<PageLayout> layoutChoiceBox;
    @FXML
    private ListView<ExportItem> exportListView;

    @FXML
    private Label titleLabel;
    @FXML
    private Button historyButton;
    @FXML
    private Label configHeadingLabel;
    @FXML
    private Label documentHeadingLabel;
    @FXML
    private Label typeLabel;
    @FXML
    private Label layoutLabel;
    @FXML
    private Label cartHeadingLabel;
    @FXML
    private Label historyHeadingLabel;

    private Task<Game> previewTask;
    private long lastPreviewUpdate = 0;

    @FXML
    public void onHistoryButtonAction() {
        boolean isVisible = historySidebar.isVisible();
        historySidebar.setVisible(!isVisible);
        historySidebar.setManaged(!isVisible);
    }

    public void addToList(ExportItem item) {
        list.add(item);
    }

    public void removeFromList(ExportItem item) {
        list.remove(item);
    }

    @Override
    public void terminateAllActiveActions() {
        if (task != null) {
            task.cancel(true);
        }
        if (previewTask != null) {
            previewTask.cancel(true);
        }
    }

    public void onMainActionClick() {
        if (list.isEmpty()) return;
        int totalGamesInCart = 0;
        for (ExportItem item : list) {
            GameMode type = item.gameConfig();
            if (type instanceof MazeTypes mazeTypes && mazeTypes.mazeType() == MazeType.MULTI_LEVEL) {
                totalGamesInCart += 1;
            } else {
                totalGamesInCart += type.getCount();
            }
        }
        if (totalGamesInCart > MAX_GENERATED) {
            AlertBox.errorBoxGenerationTooMuch();
            return;
        }
        generateGames();
    }

    private void setUiLocked(boolean locked) {
        Platform.runLater(() -> okButton.setDisable(locked));
    }

    public void initialize(User user, MainScreenController mainScreenController, TabState tabState) throws IOException {
        this.user = user;
        this.tabState = tabState;
        this.mainScreenController = mainScreenController;

        exportedListView.setCellFactory(param -> new ExportedItemsCell(user, mainScreenController, this));

        loadTabs();
        setUpLabels();
        setupHistoryList();

        sudokuController.initialize(canvas, user, this);
        mazeController.initialize(canvas, user, this);
        bridgeController.initialize(canvas, this);
        shikakuController.initialize(canvas, this, user);
        favoriteTabController.initialize(canvas, user, this);

        setItemsPreviewTypeChoiceBox(previewTypeChoiceBox);
        setItemsLayoutChoiceBox(layoutChoiceBox);

        exportListView.setItems(list);
        exportListView.setCellFactory(param -> new CartItemCell(this));

        if (canvasContainer != null && canvas != null) {
            canvas.widthProperty().bind(canvasContainer.widthProperty().subtract(10));
            canvas.heightProperty().bind(canvasContainer.heightProperty().subtract(10));

            canvas.widthProperty().addListener((obs, oldV, newV) -> updatePreview());
            canvas.heightProperty().addListener((obs, oldV, newV) -> updatePreview());
        }

        gameTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> updatePreview());
        updatePreview();
    }

    public void updatePreview() {
        long currentReq = System.currentTimeMillis();
        lastPreviewUpdate = currentReq;

        Platform.runLater(() -> {
            VT_EXECUTOR.submit(() -> {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ignored) {}

                if (lastPreviewUpdate != currentReq) return;

                Platform.runLater(() -> {
                    if (previewTask != null && previewTask.isRunning()) {
                        previewTask.cancel(true);
                    }

                    Tab selectedTab = gameTabPane.getSelectionModel().getSelectedItem();
                    if (selectedTab == null) return;

                    GameMode config = null;
                    if (selectedTab == sudokuTab && sudokuController != null) config = sudokuController.getCurrentConfig();
                    else if (selectedTab == bridgeTab && bridgeController != null) config = bridgeController.getCurrentConfig();
                    else if (selectedTab == mazeTab && mazeController != null) config = mazeController.getCurrentConfig();
                    else if (selectedTab == shikakuTab && shikakuController != null) config = shikakuController.getCurrentConfig();
                    else if (selectedTab == multiTab && favoriteTabController != null) config = favoriteTabController.getCurrentConfig();

                    if (config == null) {
                        if (canvas != null) {
                            canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                        }
                        return;
                    }

                    GameMode finalConfig = config;
                    previewTask = new Task<>() {
                        @Override
                        protected Game call() throws Exception {
                            return ExportUtils.generateGameOnly(new ExportDetailsLocal(RandomizationFactory.generateSeed(), finalConfig));
                        }
                    };
                    previewTask.setOnSucceeded(e -> renderPreview(previewTask.getValue()));
                    VT_EXECUTOR.submit(previewTask);
                });
            });
        });
    }

    private void renderPreview(Game game) {
        if (canvas == null) return;
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (game == null) return;

        if (game instanceof Sudoku sudoku) {
            boolean hasOutsideClues = sudoku.getModifiers() != null &&
                    (sudoku.getModifiers().hasSkyscraper() ||
                            sudoku.getModifiers().hasSandwich() ||
                            sudoku.getModifiers().hasXSums());

            if (hasOutsideClues) {
                double cw = canvas.getWidth();
                double ch = canvas.getHeight();
                double offsetX = cw * 0.12;
                double offsetY = ch * 0.12;
                double gridW = cw * 0.76;
                double gridH = ch * 0.76;

                gc.save();
                gc.translate(offsetX, offsetY);
                Canvas virtualCanvas = new Canvas(gridW, gridH);
                SudokuRenderer.renderFullBoard(virtualCanvas, sudoku, false);
                gc.drawImage(virtualCanvas.snapshot(null, null), 0, 0);
                gc.restore();

                ConstraintRenderer.drawOutsideModifiers(
                        gc, sudoku.getModifiers(), sudoku.getType().getGridSize(),
                        gridW / sudoku.getType().getGridSize(), gridH / sudoku.getType().getGridSize(),
                        offsetX, offsetY, gridW, gridH, false
                );
            } else {
                SudokuRenderer.renderFullBoard(canvas, sudoku, true);
            }
        } else if (game instanceof Bridge bridge) {
            BridgeRenderer.render(canvas, bridge, true);
        } else if (game instanceof Shikaku shikaku) {
            ShikakuRenderer.render(canvas, shikaku, true);
        } else if (game instanceof Maze maze) {
            double padding = Math.min(canvas.getWidth(), canvas.getHeight()) * 0.05;
            double availW = canvas.getWidth() - 2 * padding;
            double availH = canvas.getHeight() - 2 * padding;

            int cols = maze.getWidth();
            int rows = maze.getHeight();
            double aw = cols;
            double ah = rows;
            double aspect = 1.0;

            if (maze.getMazeShape() == HEXAGONAL) {
                aw = 1.0 + 0.75 * (cols - 1);
                ah = rows + (cols > 1 ? 0.5 : 0.0);
                aspect = Math.sqrt(3) / 2.0;
            }

            double cellW = Math.min(availW / aw, availH / (ah * aspect));
            double cellH = cellW * aspect;
            double actualW = cellW * aw;
            double actualH = cellH * ah;

            int currentFloor = 0;
            int totalFloors = maze.getMazeGridFloors().size();
            if (totalFloors > 0) {
                MazeGrid grid = maze.getMazeGridFloors().get(currentFloor).getMazeGrid();
                Canvas tempCanvas = new Canvas(actualW, actualH);
                renderGrid(tempCanvas, grid, currentFloor, totalFloors, false, user);

                if (grid.getPath() != null) {
                   renderPath(tempCanvas, grid.getPath(), grid, false);
                }

                double tx = (canvas.getWidth() - actualW) / 2.0;
                double ty = (canvas.getHeight() - actualH) / 2.0;
                SnapshotParameters params = new SnapshotParameters();
                params.setFill(Color.TRANSPARENT);
                WritableImage image = tempCanvas.snapshot(params, null);
                gc.drawImage(image, tx, ty);
            }
        }
    }

    private void loadTabs() throws IOException {
        loadTabContent("/cz/logicgo/ui/windows/exportMultiple/tabs/sudoku_tab.fxml", sudokuTab, c -> sudokuController = (SudokuTabController) c, TypeGame.SUDOKU);
        loadTabContent("/cz/logicgo/ui/windows/exportMultiple/tabs/bridge_tab.fxml", bridgeTab, c -> bridgeController = (BridgeTabController) c, TypeGame.BRIDGE);
        loadTabContent("/cz/logicgo/ui/windows/exportMultiple/tabs/shikaku_tab.fxml", shikakuTab, c -> shikakuController = (ShikakuTabController) c, TypeGame.SHIKAKU);
        loadTabContent("/cz/logicgo/ui/windows/exportMultiple/tabs/maze_tab.fxml", mazeTab, c -> mazeController = (MazeTabController) c, TypeGame.MAZE);
        loadTabContent("/cz/logicgo/ui/windows/exportMultiple/tabs/multi_tab.fxml", multiTab, c -> favoriteTabController = (FavoriteTabController) c, null);
    }

    private void loadTabContent(String fxmlPath, Tab tab, TabControllerConsumer consumer, TypeGame typeGame) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Node content = loader.load();

        tab.setContent(content);
        tab.setClosable(false);
        tab.setUserData(typeGame);
        tab.getContent().getStyleClass().add("root");
        consumer.accept(loader.getController());
    }

    private void setUpLabels() {
        sudokuTab.setText(getFormatted("typeGame.sudoku"));
        mazeTab.setText(getFormatted("typeGame.maze"));
        bridgeTab.setText(getFormatted("typeGame.bridge"));
        multiTab.setText(getFormatted("export.multiTab.name"));
        okButton.setText(getFormatted("export.generateButton.generate"));

        titleLabel.setText(getFormatted("export.multi.title"));
        historyButton.setText(getFormatted("export.multi.btn.history"));
        configHeadingLabel.setText(getFormatted("export.multi.heading.config"));
        documentHeadingLabel.setText(getFormatted("export.multi.heading.document"));

        typeLabel.setText(getFormatted("export.multi.label.type"));
        layoutLabel.setText(getFormatted("export.multi.label.layout"));
        generateHelpCheckBox.setText(getFormatted("export.multi.checkbox.help"));

        cartHeadingLabel.setText(getFormatted("export.multi.heading.cart"));
        historyHeadingLabel.setText(getFormatted("export.multi.heading.history"));
    }

    private void setupHistoryList() {
        refreshHistoryData();
    }

    public void generateGames() {
        List<ExportDetailsLocal> setupModes = new ArrayList<>();

        for (ExportItem item : list) {
            GameMode type = item.gameConfig();
            int count = type.getCount();
            if (type instanceof MazeTypes mazeTypes && mazeTypes.mazeType() == MazeType.MULTI_LEVEL) {
                setupModes.add(new ExportDetailsLocal(RandomizationFactory.generateSeed(), type));
            } else {
                for (int i = 0; i < count; i++) {
                    setupModes.add(new ExportDetailsLocal(RandomizationFactory.generateSeed(), type));
                }
            }
        }
        executeNewExportTask(setupModes);
    }

    public void executeNewExportTask(List<ExportDetailsLocal> gameModes) {
        executeExportTask(gameModes, null);
    }

    public void executeExistingExportTask(ExportDetail exportDetail) {
        executeExportTask(null, exportDetail);
    }

    public void executeExportTask(List<ExportDetailsLocal> gameModes, ExportDetail exportDetail) {
        var path = getSaveFilePath(user, mainScreenController.getStage());
        if (path == null) {
            return;
        }
        list.clear();
        String savePath = path.toString();

        if (savePath.isEmpty()) return;

        var fileName = Path.of(savePath).getFileName().toString();
        Path internalExportPath = Path.of(ExportFileService.appFolderExportsPath, fileName);

        final PreviewType previewType = (gameModes != null) ? previewTypeChoiceBox.getValue() : exportDetail.getPreviewType();
        final PageLayout pageLayout = layoutChoiceBox.getValue();

        boolean wantTasks = (previewType == PreviewType.UNSOLVED || previewType == PreviewType.BOTH);
        boolean wantSolutions = (previewType == PreviewType.BOTH);
        final boolean wantHelp = (generateHelpCheckBox != null && generateHelpCheckBox.isSelected());

        int baseTotal = gameModes != null ? gameModes.size() : exportDetail.getExportedGames().size();
        int progressMultiplier = (wantTasks && wantSolutions) ? 2 : 1;

        tabState.setChangePending();

        progressCount[0] = 0;
        progressCount[1] = baseTotal;

        setUiLocked(true);

        task = new Task<>() {
            @Override
            protected List<Game> call() throws Exception {
                List<Game> gamesToSave;
                int totalGames = baseTotal;

                updateMessage(getFormatted("export.runningTasksTitle") + ": 0 / " + totalGames);
                AtomicInteger currentProgress = new AtomicInteger(0);

                if (gameModes != null) {
                    gamesToSave = gameModes.stream()
                            .map(detail -> {
                                Game generatedGame = null;
                                while (generatedGame == null) {
                                    if (isCancelled()) return null;
                                    generatedGame = ExportUtils.generateGameOnly(detail);
                                }

                                int current = currentProgress.incrementAndGet();
                                updateProgress(current, (long) totalGames * progressMultiplier);
                                updateMessage(getFormatted("export.status.generating", current, totalGames));
                                return generatedGame;
                            })
                            .filter(Objects::nonNull)
                            .toList();
                } else {
                    gamesToSave = new ArrayList<>(exportDetail.getExportedGames());
                    currentProgress.addAndGet(baseTotal);
                    updateProgress(currentProgress.get(), (long) totalGames * progressMultiplier);
                }

                if (isCancelled()) return null;
                Files.createDirectories(internalExportPath.getParent());

                Document document = new Document(PageSize.A4, 36, 36, 36, 36);
                FileOutputStream fos = null;
                PdfWriter writer = null;

                try {
                    fos = new FileOutputStream(internalExportPath.toFile());
                    writer = PdfWriter.getInstance(document, fos);
                    document.open();

                    Font titleFont = ExportUtils.getTitleFont();
                    Font idFont = ExportUtils.getIdFont();
                    ExportUtils.PdfLayoutManager layoutManager = new ExportUtils.PdfLayoutManager(document, titleFont, idFont, pageLayout.getGamesPerPage());

                    if (wantTasks) {
                        for (int i = 0; i < totalGames; i++) {
                            if (isCancelled()) break;
                            Game game = gamesToSave.get(i);
                            String baseTitle = generateGameTitle(game);
                            layoutManager.addGameWithFloors(game, PreviewType.UNSOLVED, baseTitle);

                            int current = currentProgress.incrementAndGet();
                            updateProgress(current, (long) totalGames * progressMultiplier);
                            updateMessage(getFormatted("export.status.writingTasks", i + 1, totalGames));
                        }
                        layoutManager.flush();
                    }
                    if (wantSolutions) {
                        for (int i = 0; i < totalGames; i++) {
                            if (isCancelled()) break;
                            Game game = gamesToSave.get(i);
                            String baseTitle = generateGameTitle(game);
                            layoutManager.addGameWithFloors(game, PreviewType.SOLVED, baseTitle);

                            int current = currentProgress.incrementAndGet();
                            updateProgress(current, (long) totalGames * progressMultiplier);
                            updateMessage(getFormatted("export.status.writingSolutions", i + 1, totalGames));
                        }
                        layoutManager.flush();
                    }

                    if (wantHelp && !isCancelled()) {
                        updateMessage(getFormatted("export.status.writingRules"));
                        document.newPage();
                        RulesGen.appendRulesToDocument(document, gamesToSave);
                    }

                    if (!isCancelled()) {
                        gamesToSave.forEach((g) -> g.setForExport(true));
                    }
                    return gamesToSave;

                } finally {
                    if (document.isOpen()) {
                        document.close();
                    }
                    if (fos != null) {
                        fos.close();
                    }
                }
            }

            @Override
            protected void succeeded() {
                List<Game> exportedGames = getValue();
                if (exportedGames == null) return;

                Path source = internalExportPath;
                Path target = Path.of(savePath);
                ExportFileService.copy(source, target);

                if (currentExportDetail == null) {
                    ExportDetail exportDetail = new ExportDetail();
                    exportDetail.setPathToAssocFileExport(internalExportPath.toString());
                    exportDetail.setPathToAssocFileNormal(savePath);
                    exportDetail.setPreviewType(previewType);
                    exportDetail.setPageLayout(pageLayout);
                    exportDetail.setUser(user);
                    exportDetail.setExportedGames(exportedGames);
                    exportDetail.setLastDownload(ZonedDateTime.now());

                    gameService.saveGames(exportedGames);
                    exportedGameService.saveDetail(exportDetail);
                } else {
                    currentExportDetail.setLastDownload(ZonedDateTime.now());
                    currentExportDetail.setPathToAssocFileExport(internalExportPath.toString());
                    currentExportDetail.setPathToAssocFileNormal(savePath);
                }

                refreshHistoryData();
            }

            @Override
            protected void done() {
                setUiLocked(false);
                tabState.markSavingFinished();
            }

            @Override
            protected void failed() {
                if (getException() != null) getException().printStackTrace();
                setUiLocked(false);
                tabState.markSavingFinished();
            }

            @Override
            protected void cancelled() {
                setUiLocked(false);
                tabState.markSavingFinished();
            }
        };

        String description = generateExportDescription(baseTotal, gameModes, exportDetail, null);
        mainScreenController.registerExportTask(description, task, savePath);

        setUiLocked(false);
    }

    public void refreshHistoryData() {
        VT_EXECUTOR.submit(() -> {
            List<ExportDetail> rawDetails = exportedGameService.findByUser(user);
            List<ExportDetail> entries = convertToEntries(rawDetails);

            Platform.runLater(() -> {
                this.allEntries = entries;
                setupPagination();
            });
        });
    }

    public List<ExportDetail> convertToEntries(List<ExportDetail> exportDetails) {
        if (exportDetails == null) {
            return new ArrayList<>();
        }
        List<ExportDetail> entries = new ArrayList<>(exportDetails);

        entries.sort((o1, o2) -> {
            if (o1.getLastDownload() == null || o2.getLastDownload() == null) return 0;
            return o2.getLastDownload().compareTo(o1.getLastDownload());
        });

        return entries;
    }

    private void setupPagination() {
        if (allEntries == null) {
            pagination.setPageCount(1);
            return;
        }
        int pageCount = (int) Math.ceil((double) allEntries.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(Math.max(pageCount, 1));
        pagination.setPageFactory(this::updatePage);
    }

    private Node updatePage(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, allEntries.size());

        List<ExportDetail> subList = allEntries.subList(fromIndex, toIndex);
        exportedListView.getItems().setAll(subList);

        return new Group();
    }

    @FunctionalInterface
    private interface TabControllerConsumer {
        void accept(Object controller);
    }

    public record ExportItem(
            String description,
            GameMode gameConfig
    ) {
        @Override
        public String toString() {
            return String.format("%s (%dx, %s)", description, gameConfig.getCount(), gameConfig.getDifficulty().getTranslation());
        }
    }


}
