package cz.logicgo.ui.controllers.menu;

import cz.logicgo.core.GameUtils;
import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.entity.setting.GameSetting;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.factoryInit.sudoku.SudokuInit;
import cz.logicgo.core.gameClasses.favorites.GameFavorite;
import cz.logicgo.core.gameClasses.favorites.SudokuFavorite;
import cz.logicgo.core.gameClasses.sudoku.misc.SizeRange;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuRegionLayout;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.TabType;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.core.misc.enums.settings.SettingKey;
import cz.logicgo.core.misc.enums.settings.SudokuSettings;
import cz.logicgo.core.misc.enums.settings.modes.CellNotesMode;
import cz.logicgo.core.misc.enums.settings.modes.HighlightMode;
import cz.logicgo.engine.algorithms.settings.DifficultyChoosing;
import cz.logicgo.engine.algorithms.sudoku.custom.CustomLayoutsLoader;
import cz.logicgo.engine.util.generate.RandomizationFactory;
import cz.logicgo.engine.util.generate.SeedCreator;
import cz.logicgo.persistence.services.GameSettingService;
import cz.logicgo.persistence.services.UserSettingsService;
import cz.logicgo.ui.StartOfApp;
import cz.logicgo.ui.controllers.gameControllers.choosers.ChosenPattern;
import cz.logicgo.ui.controllers.helpers.SudokuControlConfig;
import cz.logicgo.ui.controllers.screenControllers.MainScreenController;
import cz.logicgo.ui.controllers.screenControllers.RedrawCanvasFunctions;
import cz.logicgo.ui.controllers.viewer.SudokuCustomViewerController;
import cz.logicgo.ui.factories.SudokuGameFactory;
import cz.logicgo.ui.misc.TabState;
import cz.logicgo.ui.misc.tabChoosingClasses.FavoriteGameWrapper;
import cz.logicgo.ui.misc.tabChoosingClasses.LoadedGameWrapper;
import cz.logicgo.ui.misc.tabChoosingClasses.TabChoiceWrapper;
import cz.logicgo.ui.renderers.sudoku.ConstraintRenderer;
import cz.logicgo.ui.renderers.sudoku.SudokuRenderer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.controlsfx.control.SegmentedButton;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static cz.logicgo.core.gameClasses.sudoku.SudokuUtils.flattenBoard;
import static cz.logicgo.core.gameClasses.sudoku.SudokuUtils.intToSizes;
import static cz.logicgo.core.misc.Messages.getFormatted;
import static cz.logicgo.ui.controllers.Openers.darkModeIconSet;
import static cz.logicgo.ui.controllers.helpers.ElementsSetterHelpers.*;


public class SudokuSettingsMenu extends AbstractSettingsMenu<Sudoku> {

    private boolean custom;
    private boolean seeded;
    @FXML
    Canvas canvas;
    SudokuVariant sudokuVariant;
    UserSettingsService userSettingsService = new UserSettingsService();
    private SudokuPatternLayout patternLayout = null;
    private ChoiceBox<SudokuSize> sizeChoiceBox;
    private ChoiceBox<SudokuRegionLayout> regionChoiceBox;
    private SegmentedButton difficultySegmentedButton;
    private ToggleGroup difficultyToggleGroup;
    private CheckBox timerCheckBox;
    private ToggleButton autoCellNotes;
    private ToggleButton manualCellNotes;
    private ToggleButton noCellNotes;
    private SegmentedButton cellNotesSegmentedButton;
    private SegmentedButton highlightSegmentedButton;
    private ToggleButton highlightRegions;
    private ToggleButton highlightNumber;
    private ToggleButton highlightOff;
    private CheckBox higlightConflictsCheckBox;
    private Button openChoice;
    private Label sizeLabel;
    private Label regionLabel;
    private Label difficultyLabel;
    private Label timerLabel;
    private Label highlightSegmentedLabel;
    private Label highlightConflictsLabel;
    private Label cellNotesLabel;
    private Label seedLabel;

    private Label seedValueLabel;
    private Button copySeedButton;
    private SudokuSize supportedSize;
    private Label hintLabel;

    private CheckBox hintCheckBox;
    private Button btnClearPattern;
    private HBox patternButtonsBox;

    private final GameSettingService gameSettingService = new GameSettingService();

    @Override
    protected void showRealPreview(Sudoku game) {
        if (game == null || canvas == null) return;
        RedrawCanvasFunctions.drawSudokuThumbnail(canvas, game);
    }

    private void setUpGridColumns() {
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(150);
        col1.setHalignment(HPos.RIGHT);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setMinWidth(250);
        col2.setHgrow(Priority.ALWAYS);

        gridPane.getColumnConstraints().addAll(col1, col2);
    }

    public void initialize(TabState state, User user, MainScreenController mainController, TabChoiceWrapper gameSettings) {
        super.initialize(state, user, mainController);
        this.isInitializing = true;

        SudokuInit init = (SudokuInit) gameSettings.toGameInit();
        if (init == null) {
            throw new IllegalArgumentException("Unsupported or empty game settings wrapper: " + gameSettings);
        }

        this.sudokuVariant = init.getSudokuVariant();
        this.seeded = gameSettings.isFixed();
        this.custom = this.seeded || (gameSettings instanceof FavoriteGameWrapper);

        setUpGridColumns();

        if (canvas != null) {
            canvas.widthProperty().addListener((obs, oldVal, newVal) -> updateCanvasPreview());
            canvas.heightProperty().addListener((obs, oldVal, newVal) -> updateCanvasPreview());
        }

        Map<SettingKey, Object> defaultSettings = userSettingsService.loadAllSettings(user);

        initControls(defaultSettings);
        addListeners();

        if (this.custom) {
            if (this.seeded && init.getSeed() != null) {
                this.specialSeed = init.getSeed();
                this.customSeed = init.getSeed();
                state.setSeed(String.valueOf(specialSeed));
            }

            selectSegmentedButtonByUserData(difficultySegmentedButton, init.getDifficulty());

            if (sizeChoiceBox != null && init.getSudokuSize() != null) {
                sizeChoiceBox.getSelectionModel().select(init.getSudokuSize());
            }

            if (regionChoiceBox != null) {
                populateRegionChoices(sizeChoiceBox != null ? sizeChoiceBox.getValue() : SudokuSize.NINE);
                if (init.getRegionLayout() != null) {
                    selectRegionLayout(init.getRegionLayout());
                } else if (!regionChoiceBox.getItems().isEmpty()) {
                    regionChoiceBox.getSelectionModel().selectFirst();
                }
            }

            if (init.getPatternLayout() != null) {
                this.patternLayout = init.getPatternLayout();
            }

            if (this.seeded) {
                difficultySegmentedButton.setDisable(true);
                if (sizeChoiceBox != null) sizeChoiceBox.setDisable(true);
                if (regionChoiceBox != null) regionChoiceBox.setDisable(true);
                if (openChoice != null) openChoice.setDisable(true);
                if (btnClearPattern != null) btnClearPattern.setDisable(true);
                if (patternButtonsBox != null) patternButtonsBox.setDisable(true);
            }
        }

        buildSpecificLayout();
        this.isInitializing = false;
        settingsChanged();
    }

    @Override
    protected GameFavorite createFavoriteFromCurrentSettings() {
        SudokuInit config = createConfig();

        SudokuFavorite fav = new SudokuFavorite();
        fav.setDifficulty(config.getDifficulty());
        fav.setWidth(config.getSudokuSize().getGridSize());
        fav.setHeight(config.getSudokuSize().getGridSize());
        fav.setVariant(config.getSudokuVariant());
        fav.setSize(config.getSudokuSize());
        fav.setRegionLayout(config.getRegionLayout());
        fav.setPatternLayout(config.getPatternLayout());
        return fav;
    }

    private void initControls(Map<SettingKey, Object> settings) {
        SizeRange sizes = DifficultyChoosing.getValidSizeRange(sudokuVariant);
        List<SudokuSize> sizeInstances = intToSizes(sizes);

        if (sudokuVariant != SudokuVariant.CLASSIC) {
            sizeInstances.remove(SudokuSize.SEVEN);
        }

        sizeLabel = new Label(getFormatted("sudokuSettings.size"));

        sizeChoiceBox = new ChoiceBox<>();
        sizeChoiceBox.setItems(FXCollections.observableArrayList(sizeInstances));

        supportedSize = SudokuSize.NINE;
        if (sizeInstances.contains(SudokuSize.NINE)) {
            sizeChoiceBox.getSelectionModel().select(SudokuSize.NINE);
        } else if (!sizeInstances.isEmpty()) {
            sizeChoiceBox.getSelectionModel().selectFirst();
        }

        sizeChoiceBox.setDisable(sizeInstances.size() <= 1);
        supportedSize = sizeChoiceBox.getValue() != null ? sizeChoiceBox.getValue() : SudokuSize.NINE;

        switch (sudokuVariant) {
            case PATTERNED -> {
                openChoice = new Button(getFormatted("sudokuSettings.open_pattern"));
                patternLayout = CustomLayoutsLoader.getPatternsForSize(supportedSize.getGridSize()).getFirst();

                btnClearPattern = new Button(getFormatted("removeButton") != null ? getFormatted("removeButton") : "Odstranit vzor");
                btnClearPattern.setOnAction(e -> {
                    SudokuSize size = sizeChoiceBox != null ? sizeChoiceBox.getValue() : SudokuSize.NINE;
                    if (size == null) size = SudokuSize.NINE;
                    patternLayout = CustomLayoutsLoader.getDefaultPatternLayoutForSize(size.getGridSize());
                    isChangingConfigDirectly = true;
                    settingsChanged();
                    isChangingConfigDirectly = false;
                });
                patternButtonsBox = new HBox(10, openChoice, btnClearPattern);
            }
            case null, default -> {
                regionLabel = new Label(getFormatted("sudokuSettings.region"));
                regionChoiceBox = new ChoiceBox<>();
                populateRegionChoices(supportedSize);
            }
        }

        var config = new SudokuControlConfig()
                .setSizeBox(sizeChoiceBox)
                .setVariantBox(null)
                .setRegionBox(regionChoiceBox)
                .setCanvas(canvas)
                .setSupportedVariants(sudokuVariant);
        setupSudokuControls(config, sudokuVariant);

        difficultyLabel = new Label(getFormatted("gameSettings.difficulty"));
        difficultySegmentedButton = createDifficultySegmentedButton();
        difficultyToggleGroup = difficultySegmentedButton.getToggleGroup();

        selectSegmentedButtonByUserData(difficultySegmentedButton, Difficulty.MEDIUM);

        hintLabel = new Label(getFormatted("gameSettings.hints.label"));
        hintCheckBox = new CheckBox();
        boolean hintsOn = (Boolean) settings.getOrDefault(SudokuSettings.HINTS_ON, true);
        hintCheckBox.setSelected(hintsOn);

        highlightSegmentedLabel = new Label(getFormatted("sudokuSettings.highlight"));
        highlightRegions = new ToggleButton(getFormatted("sudokuSettings.highlight.regions"));
        highlightNumber = new ToggleButton(getFormatted("sudokuSettings.highlight.number"));
        highlightOff = new ToggleButton(getFormatted("sudokuSettings.highlight.off"));

        highlightSegmentedButton = new SegmentedButton(highlightRegions, highlightNumber, highlightOff);
        highlightSegmentedButton.getStyleClass().add("segmented-button");
        setUpSegmentedButtonHandling(highlightSegmentedButton, highlightOff);

        highlightConflictsLabel = new Label(getFormatted("sudokuSettings.highlight.conflicts"));
        higlightConflictsCheckBox = new CheckBox();

        timerLabel = new Label(getFormatted("gameSettings.timer"));
        timerCheckBox = new CheckBox();
        timerCheckBox.setSelected((Boolean) settings.getOrDefault(SudokuSettings.TIMER, false));

        cellNotesLabel = new Label(getFormatted("sudokuSettings.cellNotes"));
        autoCellNotes = new ToggleButton(getFormatted("sudokuSettings.cellNotes.auto"));
        manualCellNotes = new ToggleButton(getFormatted("sudokuSettings.cellNotes.manual"));
        noCellNotes = new ToggleButton(getFormatted("sudokuSettings.cellNotes.off"));

        cellNotesSegmentedButton = new SegmentedButton(autoCellNotes, manualCellNotes, noCellNotes);
        cellNotesSegmentedButton.getStyleClass().add("segmented-button");
        setUpSegmentedButtonHandling(cellNotesSegmentedButton, noCellNotes);

        seedLabel = new Label(getFormatted("gameSettings.seed"));
        seedValueLabel = new Label();
        seedValueLabel.setStyle("-fx-font-weight: bold;");

        copySeedButton = new Button(getFormatted("gameSettings.copy"));
        copySeedButton.setOnAction(e -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(seedValueLabel.getText());
            Clipboard.getSystemClipboard().setContent(content);
        });

        CellNotesMode savedNotesMode = (CellNotesMode) settings.getOrDefault(SudokuSettings.CELL_NOTES_MODE, CellNotesMode.NONE);
        switch (savedNotesMode) {
            case AUTO -> autoCellNotes.setSelected(true);
            case MANUAL -> manualCellNotes.setSelected(true);
            case NONE -> noCellNotes.setSelected(true);
        }

        HighlightMode savedHighlightMode = (HighlightMode) settings.getOrDefault(SudokuSettings.HIGHLIGHT_MODE, HighlightMode.NONE);
        switch (savedHighlightMode) {
            case REGIONS -> highlightRegions.setSelected(true);
            case SAME_NUMBERS -> highlightNumber.setSelected(true);
            case NONE -> highlightOff.setSelected(true);
        }

        higlightConflictsCheckBox.setSelected((Boolean) settings.getOrDefault(SudokuSettings.HIGHLIGHT_CONFLICTS, false));
    }

    private void populateRegionChoices(SudokuSize size) {
        if (regionChoiceBox == null || size == null) return;

        regionChoiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(SudokuRegionLayout layout) {
                if (layout == null) return getFormatted("choiceNoValue");
                return layout.getTranslatedName();
            }

            @Override
            public SudokuRegionLayout fromString(String string) {
                if (string == null || string.isBlank()) return null;
                return regionChoiceBox.getItems().stream()
                        .filter(l -> l.getTranslatedName().equals(string) || string.equalsIgnoreCase(l.getName()))
                        .findFirst()
                        .orElse(null);
            }
        });

        List<SudokuRegionLayout> regionLayouts;

        switch (sudokuVariant) {
            case IRREGULAR, PATTERNED -> regionLayouts = List.of();
            case CLASSIC -> regionLayouts = CustomLayoutsLoader.getLayoutsForSize(size);
            default -> regionLayouts = List.of(CustomLayoutsLoader.getBasicLayout(size));
        }

        regionChoiceBox.setItems(FXCollections.observableArrayList(regionLayouts));

        if (!regionLayouts.isEmpty()) {
            regionChoiceBox.getSelectionModel().selectFirst();
        }
    }

    private void selectRegionLayout(SudokuRegionLayout target) {
        if (regionChoiceBox == null || target == null) return;

        for (SudokuRegionLayout item : regionChoiceBox.getItems()) {
            if (item.equals(target)
                    || (item.getName() != null && item.getName().equalsIgnoreCase(target.getName()))
                    || (item.getTranslatedName() != null && item.getTranslatedName().equalsIgnoreCase(target.getTranslatedName()))) {
                regionChoiceBox.getSelectionModel().select(item);
                return;
            }
        }

        if (!regionChoiceBox.getItems().contains(target)) {
            regionChoiceBox.getItems().add(target);
        }
        regionChoiceBox.getSelectionModel().select(target);
    }

    private void addListeners() {
        if (sizeChoiceBox != null) {
            sizeChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                if (newV != null && oldV != newV) {
                    patternLayout = CustomLayoutsLoader.getEmptyPatternLayout(newV.getGridSize());
                    populateRegionChoices(newV);
                    buildSpecificLayout();
                }
                isChangingConfigDirectly = true;
                settingsChanged();
                isChangingConfigDirectly = false;
            });
        }

        difficultyToggleGroup.selectedToggleProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                isChangingConfigDirectly = true;
                settingsChanged();
                isChangingConfigDirectly = false;
            }
        });

        if (regionChoiceBox != null) {
            regionChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                isChangingConfigDirectly = true;
                settingsChanged();
                isChangingConfigDirectly = false;
            });
        }

        hintCheckBox.selectedProperty().addListener((obs, oldV, newV) -> {
            isChangingConfigDirectly = true;
            settingsChanged();
            isChangingConfigDirectly = false;
        });

        higlightConflictsCheckBox.selectedProperty().addListener((obs, oldV, newV) -> {
            isChangingConfigDirectly = true;
            settingsChanged();
            isChangingConfigDirectly = false;
        });

        timerCheckBox.selectedProperty().addListener((obs, oldV, newV) -> {
            isChangingConfigDirectly = true;
            settingsChanged();
            isChangingConfigDirectly = false;
        });

        if (openChoice != null) {
            openChoice.setOnAction(e -> {
                try {
                    openPatternWindow();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
    }

    @Override
    protected boolean isConfigValid() {
        return true;
    }

    @Override
    protected Sudoku createGameInBackground() throws Exception {
        return SudokuGameFactory.createGame(createConfig());
    }

    @Override
    protected void onGameGenerated(Sudoku resultGame) {
        if (resultGame == null) {
            resetButtons();
            return;
        }

        Platform.runLater(() -> {
            try {
                SudokuInit config = new SudokuInit();
                config.setPlayer(user);
                createSettings(config);
                List<GameSetting> settings = gameSettingService.prepareGameSettings(resultGame, config.getSettings());
                if (!config.getSettings().isEmpty()) {
                    userSettingsService.saveSettingsFromBuffer(config);
                }
                resultGame.setSettings(settings);
                mainController.closeCurrentAndReplace(TabType.SUDOKU, new LoadedGameWrapper(resultGame));
            } catch (IOException ex) {
                ex.printStackTrace();
                resetButtons();
            }
        });
    }

    @Override
    protected void updateSeedField() {
        if (this.specialSeed != null) {
            this.customSeed = this.specialSeed;
        } else if (this.customSeed == null) {
            this.customSeed = RandomizationFactory.generateSeed();
        }

        var config = createConfig();
        if (config != null) {
            config.setSeed(this.customSeed);
            seedValueLabel.setText(SeedCreator.createSeed(config));
        }
    }

    @Override
    protected void buildSpecificLayout() {
        gridPane.getChildren().clear();
        int row = 0;

        if (sizeLabel != null && sizeChoiceBox != null && sudokuVariant != SudokuVariant.IRREGULAR) {
            if (sizeChoiceBox.getItems() != null && sizeChoiceBox.getItems().size() > 1) {
                addGridRow(row++, sizeLabel, sizeChoiceBox);
            }
        }

        switch (sudokuVariant) {
            case PATTERNED -> {
                if (patternButtonsBox != null) {
                    addGridRow(row++, new Label(getFormatted("sudokuSettings.choose_pattern")), patternButtonsBox);
                }
            }
            case null, default -> {
                if (regionLabel != null && regionChoiceBox != null && regionChoiceBox.getItems() != null) {
                    if (regionChoiceBox.getItems().size() > 1) {
                        addGridRow(row++, regionLabel, regionChoiceBox);
                    }
                }
            }
        }

        if (difficultyLabel != null && difficultySegmentedButton != null) {
            addGridRow(row++, difficultyLabel, difficultySegmentedButton);
        }
        if (hintLabel != null && hintCheckBox != null) {
            addGridRow(row++, hintLabel, hintCheckBox);
        }
        if (highlightSegmentedLabel != null && highlightSegmentedButton != null) {
            addGridRow(row++, highlightSegmentedLabel, highlightSegmentedButton);
        }
        if (highlightConflictsLabel != null && higlightConflictsCheckBox != null) {
            addGridRow(row++, highlightConflictsLabel, higlightConflictsCheckBox);
        }
        if (timerLabel != null && timerCheckBox != null) {
            addGridRow(row++, timerLabel, timerCheckBox);
        }
        if (cellNotesLabel != null && cellNotesSegmentedButton != null) {
            addGridRow(row++, cellNotesLabel, cellNotesSegmentedButton);
        }

        HBox seedActionsBox = new HBox(10, seedValueLabel, copySeedButton);
        seedActionsBox.setMaxWidth(Double.MAX_VALUE);
        seedActionsBox.setAlignment(Pos.CENTER_LEFT);

        if (seedLabel != null) {
            GridPane.setValignment(seedLabel, VPos.CENTER);
            addGridRow(row++, seedLabel, seedActionsBox);
        }
    }

    @Override
    void updateCanvasPreview() {
        if (canvas == null) return;

        SudokuSize size = sizeChoiceBox == null ? SudokuSize.NINE : sizeChoiceBox.getValue();
        if (size == null) return;

        double cw = canvas.getWidth();
        double ch = canvas.getHeight();
        if (cw <= 0 || ch <= 0) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, cw, ch);

        var bgTask = backgroundCreationTask.get();
        if (bgTask != null && bgTask.isDone() && !bgTask.isCancelled()) {
            try {
                Sudoku currentPreviewGame = bgTask.get();
                Sudoku sudoku = null;
                if (currentPreviewGame != null) {
                    sudoku = (Sudoku) GameUtils.createNewInstance(currentPreviewGame);
                }
                if (sudoku != null) {
                    Arrays.stream(flattenBoard(sudoku.getBoard())).forEach(cell -> cell.setChangeable(false));
                    boolean hasOutsideClues = currentPreviewGame.getModifiers() != null &&
                            (currentPreviewGame.getModifiers().hasSkyscraper() ||
                                    currentPreviewGame.getModifiers().hasSandwich() ||
                                    currentPreviewGame.getModifiers().hasXSums());

                    if (hasOutsideClues) {
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

                        drawOutsideClues(canvas, sudoku, offsetX, offsetY, gridW, gridH, false);
                    } else {
                        SudokuRenderer.renderFullBoard(canvas, sudoku, false);
                    }
                    return;
                }
            } catch (Exception e) {
            }
        }

        drawInitialEmptyBoard(gc, size);
    }

    private void drawOutsideClues(Canvas targetCanvas, Sudoku sudoku, double offsetX, double offsetY, double gridWidth, double gridHeight, boolean forPrint) {
        if (targetCanvas == null || sudoku.getModifiers() == null) return;

        int size = sudoku.getType().getGridSize();
        double cellW = gridWidth / size;
        double cellH = gridHeight / size;

        ConstraintRenderer.drawOutsideModifiers(
                targetCanvas.getGraphicsContext2D(), sudoku.getModifiers(), size,
                cellW, cellH, offsetX, offsetY, gridWidth, gridHeight, forPrint
        );
    }

    private void drawInitialEmptyBoard(GraphicsContext gc, SudokuSize size) {
        double cw = canvas.getWidth();
        double ch = canvas.getHeight();
        double cellW = cw / size.getGridSize();
        double cellH = ch / size.getGridSize();

        Integer[][] regionsToDraw = (regionChoiceBox != null && regionChoiceBox.getValue() != null)
                ? regionChoiceBox.getValue().getRegions()
                : CustomLayoutsLoader.getBasicLayout(size).getRegions();

        if (regionsToDraw != null) {
            RedrawCanvasFunctions.strokeBoard(
                    regionsToDraw, cellW, cellH,
                    gc, sudokuVariant, CustomLayoutsLoader.getEmptyPatternLayout(size),
                    true
            );
        }
    }

    @Override
    public SudokuInit createConfig() {
        SudokuInit config = new SudokuInit(user);

        config.setSudokuVariant(sudokuVariant);
        createSettings(config);

        SudokuSize sudokuSize = sizeChoiceBox != null ? sizeChoiceBox.getValue() : SudokuSize.NINE;
        if (sudokuSize == null) sudokuSize = SudokuSize.NINE;
        config.setSudokuSize(sudokuSize);

        Toggle diffToggle = difficultyToggleGroup.getSelectedToggle();
        Difficulty diff = diffToggle != null ? (Difficulty) diffToggle.getUserData() : Difficulty.MEDIUM;
        config.setDifficulty(diff);

        if (regionChoiceBox != null && regionChoiceBox.getValue() != null) {
            config.setRegionLayout(regionChoiceBox.getSelectionModel().getSelectedItem());
            config.setPatternLayout(CustomLayoutsLoader.getEmptyPatternLayout(sudokuSize));
        } else {
            switch (sudokuVariant) {
                case PATTERNED -> {
                    if (patternLayout != null) {
                        config.setPatternLayout(patternLayout);
                    } else {
                        config.setPatternLayout(CustomLayoutsLoader.getEmptyPatternLayout(sudokuSize));
                    }
                    config.setRegionLayout(CustomLayoutsLoader.getBasicLayout(sudokuSize));
                }
                case null, default -> {
                    config.setRegionLayout(CustomLayoutsLoader.getBasicLayout(sudokuSize));
                    config.setPatternLayout(CustomLayoutsLoader.getEmptyPatternLayout(sudokuSize));
                }
            }
        }

        config.setSudokuVariant(sudokuVariant);

        Long effectiveSeed = (specialSeed != null) ? specialSeed : customSeed;
        if (effectiveSeed != null) {
            config.setSeed(effectiveSeed);
        }

        return config;
    }

    private void createSettings(SudokuInit config) {
        config.addSetting(SudokuSettings.HINTS_ON, hintCheckBox.isSelected());

        CellNotesMode notesMode;
        if (autoCellNotes.isSelected()) {
            notesMode = CellNotesMode.AUTO;
        } else if (manualCellNotes.isSelected()) {
            notesMode = CellNotesMode.MANUAL;
        } else {
            notesMode = CellNotesMode.NONE;
        }
        config.addSetting(SudokuSettings.CELL_NOTES_MODE, notesMode);

        config.addSetting(SudokuSettings.TIMER, timerCheckBox.isSelected());

        HighlightMode highlightMode;
        if (highlightRegions.isSelected()) {
            highlightMode = HighlightMode.REGIONS;
        } else if (highlightNumber.isSelected()) {
            highlightMode = HighlightMode.SAME_NUMBERS;
        } else {
            highlightMode = HighlightMode.NONE;
        }
        config.addSetting(SudokuSettings.HIGHLIGHT_MODE, highlightMode);

        config.addSetting(SudokuSettings.HIGHLIGHT_CONFLICTS, higlightConflictsCheckBox.isSelected());
    }

    public void openPatternWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader(StartOfApp.class.getResource("/cz/logicgo/ui/windows/gameChoice/viewer.fxml"));

        SudokuCustomViewerController controller = new SudokuCustomViewerController();
        loader.setController(controller);

        Parent content = loader.load();
        content.getStyleClass().add("root");
        Stage stage = new Stage();

        var size = sizeChoiceBox.getValue().getGridSize();
        var chP = new ChosenPattern();

        controller.initialize(user, size, SudokuVariant.PATTERNED, chP);
        controller.setStage(stage);

        stage.setTitle(getFormatted("sudoku.window.pattern"));

        stage.setMinWidth(750);
        stage.setMinHeight(550);
        stage.setMaxWidth(1200);
        stage.setMaxHeight(850);

        stage.setScene(new Scene(content, 750, 550));
        stage.initModality(Modality.APPLICATION_MODAL);
        darkModeIconSet(stage);
        stage.showAndWait();

        if (chP.getPatternLayout() != null) {
            patternLayout = chP.getPatternLayout();
            isChangingConfigDirectly = true;
            settingsChanged();
            isChangingConfigDirectly = false;
        }
    }

    @Override
    public void refreshContent() {
        super.refreshContent();
    }
}
