package cz.logicgo.ui.controllers.listControllers;

import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.SortOption;
import cz.logicgo.core.misc.enums.Status;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.bridge.BridgeType;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.core.misc.enums.gameTypes.shikaku.ShikakuType;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.core.misc.interfaces.Translatable;
import cz.logicgo.persistence.filter.FilterProperties;
import cz.logicgo.persistence.services.GameService;
import cz.logicgo.ui.controllers.ControllerClosable;
import cz.logicgo.ui.controllers.listControllers.history.CachedGameItemCell;
import cz.logicgo.ui.controllers.listControllers.history.GameHistoryEntry;
import cz.logicgo.ui.controllers.screenControllers.MainScreenController;
import cz.logicgo.ui.misc.ImagePreLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.util.List;

import static cz.logicgo.core.misc.Messages.getFormatted;
import static cz.logicgo.ui.misc.NodeSnapshots.loadThumbnail;
import static cz.logicgo.core.misc.formatter.FavoriteFormatter.format;


public class ItemsViewController implements ControllerClosable {

    private static final int ITEMS_PER_PAGE = 20;
    public VBox dynamicFiltersContainer;
    public Label labelTitle;

    @FXML
    private Label labelGameType, labelStatus, labelDifficulty;
    @FXML
    private Label labelSort, labelDescending;
    private Label labelSudokuSize, labelSudokuVariant;
    private Label labelBridgeType;
    private Label labelMazeType, labelMazeShape;
    private Label labelShikakuType;

    final private Label xLabel = new Label("x");

    private final TextField widthTextField = new TextField();
    private final TextField heightTextField = new TextField();
    private final ChoiceBox<SudokuSize> sudokuSizeChoiceBox = new ChoiceBox<>();
    private final ChoiceBox<SudokuVariant> sudokuVariantChoiceBox = new ChoiceBox<>();
    private final ChoiceBox<BridgeType> bridgeTypeChoiceBox = new ChoiceBox<>();
    private final ChoiceBox<ShikakuType> shikakuTypeChoiceBox = new ChoiceBox<>();
    private final ChoiceBox<MazeType> mazeTypeChoiceBox = new ChoiceBox<>();
    private final ChoiceBox<MazeShape> mazeShapeChoiceBox = new ChoiceBox<>();

    public void setUpLabels() {
        labelTitle.setText(getFormatted("history.filter.title"));
        labelGameType.setText(getFormatted("history.filter.gameType"));
        labelStatus.setText(getFormatted("history.filter.status"));
        labelDifficulty.setText(getFormatted("history.filter.difficulty"));
        labelSort.setText(getFormatted("history.filter.sort"));
        labelDescending.setText(getFormatted("history.filter.descending"));
    }

    MainScreenController mainScreenController;
    GameService gameService = new GameService();
    User user;
    FilterProperties filterProperties;
    private List<GameHistoryEntry> allEntries;

    @FXML
    private Pagination pagination;
    @FXML
    private ListView<GameHistoryEntry> historyListView;
    @FXML
    private VBox filterSidebar;

    @FXML
    private ChoiceBox<Status> statusChoiceBox;
    @FXML
    private ChoiceBox<TypeGame> gameTypeChoiceBox;
    @FXML
    private ChoiceBox<Difficulty> difficultyChoiceBox;
    @FXML
    private ChoiceBox<SortOption> sortOptionsChoiceBox;
    @FXML
    private CheckBox sortCheckbox;

    @FXML
    public void initialize(User user, MainScreenController mainScreenController) {
        this.user = user;
        this.mainScreenController = mainScreenController;

        filterProperties = new FilterProperties(user).setSortOption(SortOption.LAST_PLAYED).setStatus(Status.IN_PROGRESS);
        List<Game> games = gameService.getGamesByFilter(filterProperties);
        this.allEntries = convertToEntries(games);

        setUpLabels();

        historyListView.setCellFactory(list -> new CachedGameItemCell(user, mainScreenController, this));
        historyListView.setMinHeight(Region.USE_PREF_SIZE);
        historyListView.setPrefHeight(600);
        historyListView.setMaxHeight(Region.USE_PREF_SIZE);

        setupPagination();

        setupFilterComponents();
    }

    private void setupFilterComponents() {
        setupTranslatableChoiceBox(statusChoiceBox, Status.values());
        setupTranslatableChoiceBox(gameTypeChoiceBox, TypeGame.values());
        setupTranslatableChoiceBox(difficultyChoiceBox, Difficulty.values());

        setupTranslatableChoiceBox(sudokuSizeChoiceBox, SudokuSize.values());
        setupTranslatableChoiceBox(sudokuVariantChoiceBox, SudokuVariant.values());

        setupTranslatableChoiceBox(bridgeTypeChoiceBox, BridgeType.values());

        setupTranslatableChoiceBox(mazeTypeChoiceBox, MazeType.values());
        setupTranslatableChoiceBox(mazeShapeChoiceBox, MazeShape.values());

        setupTranslatableChoiceBox(shikakuTypeChoiceBox, ShikakuType.values());

        ObservableList<SortOption> sortOptions = FXCollections.observableArrayList();
        sortOptions.addAll(SortOption.values());
        sortOptionsChoiceBox.setItems(sortOptions);
        sortOptionsChoiceBox.setValue(SortOption.LAST_PLAYED);
        sortOptionsChoiceBox.setConverter(createStringConverter());

        gameTypeChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateDynamicFiltersUI(newVal);
            if (oldVal != newVal) clearSpecificFilters();
        });

        loadValuesToUI();
        updateDynamicFiltersUI(gameTypeChoiceBox.getValue());
    }

    @FXML
    public void onFilterButtonAction() {
        boolean isVisible = filterSidebar.isVisible();
        filterSidebar.setVisible(!isVisible);
        filterSidebar.setManaged(!isVisible);
    }

    @FXML
    public void onApplyFilterClick() {
        filterProperties.setSortOption(sortOptionsChoiceBox.getValue())
                .setTypeGame(gameTypeChoiceBox.getValue())
                .setStatus(statusChoiceBox.getValue())
                .setAscending(!sortCheckbox.isSelected())
                .setDifficulty(difficultyChoiceBox.getValue());

        try {
            filterProperties.setWidth(widthTextField.getText().trim().isEmpty() ? null : Integer.parseInt(widthTextField.getText().trim()));
        } catch (NumberFormatException e) {
            filterProperties.setWidth(null);
        }

        try {
            filterProperties.setHeight(heightTextField.getText().trim().isEmpty() ? null : Integer.parseInt(heightTextField.getText().trim()));
        } catch (NumberFormatException e) {
            filterProperties.setHeight(null);
        }

        TypeGame selectedGame = gameTypeChoiceBox.getValue();
        if (selectedGame == TypeGame.SUDOKU) {
            filterProperties.setSudokuSize(sudokuSizeChoiceBox.getValue())
                    .setSudokuVariant(sudokuVariantChoiceBox.getValue());
        } else if (selectedGame == TypeGame.BRIDGE) {
            filterProperties.setBridgeType(bridgeTypeChoiceBox.getValue());
        } else if (selectedGame == TypeGame.MAZE) {
            filterProperties.setMazeType(mazeTypeChoiceBox.getValue())
                    .setMazeShape(mazeShapeChoiceBox.getValue());
        } else if (selectedGame == TypeGame.SHIKAKU) {
            filterProperties.setShikakuType(shikakuTypeChoiceBox.getValue());
        }

        var filteredGames = gameService.getGamesByFilter(filterProperties);
        allEntries = convertToEntries(filteredGames);
        setupPagination();
    }

    @FXML
    public void onClearFilterClick() {
        statusChoiceBox.setValue(null);
        gameTypeChoiceBox.setValue(null);
        difficultyChoiceBox.setValue(null);
        sortOptionsChoiceBox.setValue(SortOption.LAST_PLAYED);
        sortCheckbox.setSelected(false);
        clearSpecificFilters();

        onApplyFilterClick();
    }

    private void loadValuesToUI() {
        if (filterProperties == null) return;

        if (filterProperties.getSortOption() != null) {
            sortOptionsChoiceBox.setValue(filterProperties.getSortOption());
        }

        statusChoiceBox.setValue(filterProperties.getStatus());
        gameTypeChoiceBox.setValue(filterProperties.getTypeGame());
        difficultyChoiceBox.setValue(filterProperties.getDifficulty());

        sortCheckbox.setSelected(filterProperties.isAscending() != null && !filterProperties.isAscending());

        sudokuSizeChoiceBox.setValue(filterProperties.getSudokuSize());
        sudokuVariantChoiceBox.setValue(filterProperties.getSudokuVariant());
        bridgeTypeChoiceBox.setValue(filterProperties.getBridgeType());
        mazeTypeChoiceBox.setValue(filterProperties.getMazeType());
        mazeShapeChoiceBox.setValue(filterProperties.getMazeShape());
        shikakuTypeChoiceBox.setValue(filterProperties.getShikakuType());

        widthTextField.setText(filterProperties.getWidth() != null ? String.valueOf(filterProperties.getWidth()) : "");
        heightTextField.setText(filterProperties.getHeight() != null ? String.valueOf(filterProperties.getHeight()) : "");
    }

    private void clearSpecificFilters() {
        sudokuSizeChoiceBox.setValue(null);
        sudokuVariantChoiceBox.setValue(null);
        bridgeTypeChoiceBox.setValue(null);
        mazeTypeChoiceBox.setValue(null);
        mazeShapeChoiceBox.setValue(null);
        shikakuTypeChoiceBox.setValue(null);

        widthTextField.clear();
        heightTextField.clear();
    }

    private <T extends Translatable> void setupTranslatableChoiceBox(ChoiceBox<T> choiceBox, T[] values) {
        ObservableList<T> options = FXCollections.observableArrayList();
        options.add(null);
        options.addAll(values);
        choiceBox.setItems(options);
        choiceBox.setConverter(createStringConverter());
        choiceBox.setValue(null);
    }

    private void updateDynamicFiltersUI(TypeGame selectedType) {
        dynamicFiltersContainer.getChildren().clear();

        if (selectedType == null) {
            return;
        }

        GridPane grid = new GridPane();
        grid.setHgap(10.0);
        grid.setVgap(12.0);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHalignment(HPos.RIGHT);
        col1.setMinWidth(80);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        int row = 0;
        HBox hBox = new HBox();
        hBox.getChildren().addAll(widthTextField, xLabel, heightTextField);
        switch (selectedType) {
            case SUDOKU -> {
                addFilterRow(grid, row++, "history.filter.sudoku_size", sudokuSizeChoiceBox);
                addFilterRow(grid, row++, "history.filter.sudoku_variant", sudokuVariantChoiceBox);
            }
            case BRIDGE -> {
                addFilterRow(grid, row++, "history.filter.size_bridge", hBox);
                addFilterRow(grid, row++, "history.filter.bridge_type", bridgeTypeChoiceBox);
            }
            case MAZE -> {
                addFilterRow(grid, row++, "history.filter.maze_type", mazeTypeChoiceBox);
                addFilterRow(grid, row++, "history.filter.maze_shape", mazeShapeChoiceBox);
                addFilterRow(grid, row++, "history.filter.size_maze", hBox);
            }
            case SHIKAKU -> {
                addFilterRow(grid, row++, "history.filter.shikaku_type", shikakuTypeChoiceBox);
                addFilterRow(grid, row++, "history.filter.size_shikaku", hBox);
            }
        }

        dynamicFiltersContainer.getChildren().add(grid);
    }

    private void addFilterRow(GridPane grid, int row, String labelKey, Node node) {
        Label label = new Label(getFormatted(labelKey));
        label.setStyle("-fx-font-weight: bold;");
        switch (node) {
            case ChoiceBox<?> choiceBox -> choiceBox.setPrefWidth(180);
            case null, default -> {
            }
        }

        grid.add(label, 0, row);
        grid.add(node, 1, row);
    }

    private <T extends Translatable> StringConverter<T> createStringConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(T t) {
                if (t != null) {
                    return t.getTranslation();
                }
                return getFormatted("choiceNoValue");
            }

            @SuppressWarnings("unchecked")
            @Override
            public T fromString(String string) {
                return null;
            }
        };
    }

    private void setupPagination() {
        int pageCount = (int) Math.ceil((double) allEntries.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(Math.max(pageCount, 1));
        pagination.setPageFactory(this::updatePage);
    }

    private Node updatePage(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, allEntries.size());
        List<GameHistoryEntry> subList = allEntries.subList(fromIndex, toIndex);
        historyListView.getItems().setAll(subList);

        return new Group();
    }

    public List<GameHistoryEntry> convertToEntries(List<Game> games) {
        return games.stream()
                .map(game -> new GameHistoryEntry(
                        game.getId(),
                        game.getGameType(),
                        determineGameImage(game),
                        generateGameTitle(game),
                        game.getDifficulty(),
                        game.getLastPlayed(),
                        game.getStatus()
                ))
                .toList();
    }

    private Image determineGameImage(Game game) {
        Image image = loadThumbnail(game.getThumbnailName());
        if (image != null) {
            return image;
        } else {
            return switch (game) {
                case Sudoku sudoku -> ImagePreLoader.get(sudoku.getVariant());
                case Bridge bridge -> ImagePreLoader.get(bridge.getType());
                case Maze maze -> ImagePreLoader.get(maze.getMazeType());
                case Shikaku shikaku -> ImagePreLoader.get(shikaku.getShikakuType());
                default -> throw new IllegalStateException("Unexpected value: " + game);
            };
        }

    }

    public static String generateGameTitle(Game game) {
        return format(game);
    }

    public void refresh() {
        if (filterProperties == null && user != null) {
            filterProperties = new FilterProperties(user).setSortOption(SortOption.LAST_PLAYED);
        }
        onApplyFilterClick();
    }

    @Override
    public void refreshContent() {
        refresh();
    }

    @Override
    public void terminateAllActiveActions() {

    }
}
