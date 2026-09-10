package cz.logicgo.ui.controllers.menu;

import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.entity.setting.GameSetting;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.factoryInit.maze.MazeInit;
import cz.logicgo.core.gameClasses.favorites.GameFavorite;
import cz.logicgo.core.gameClasses.favorites.MazeFavorite;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.TabType;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeAlgorithm;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.core.misc.enums.settings.MazeSettings;
import cz.logicgo.core.misc.enums.settings.SettingKey;
import cz.logicgo.core.misc.enums.settings.SudokuSettings;
import cz.logicgo.engine.util.generate.RandomizationFactory;
import cz.logicgo.engine.util.generate.SeedCreator;
import cz.logicgo.persistence.services.GameSettingService;
import cz.logicgo.persistence.services.UserSettingsService;
import cz.logicgo.ui.StartOfApp;
import cz.logicgo.ui.controllers.gameControllers.choosers.ChosenMask;
import cz.logicgo.ui.controllers.screenControllers.MainScreenController;
import cz.logicgo.ui.controllers.viewer.MaskViewerController;
import cz.logicgo.ui.factories.MazeGameFactory;
import cz.logicgo.ui.misc.TabState;
import cz.logicgo.ui.misc.tabChoosingClasses.FavoriteGameWrapper;
import cz.logicgo.ui.misc.tabChoosingClasses.LoadedGameWrapper;
import cz.logicgo.ui.misc.tabChoosingClasses.TabChoiceWrapper;
import cz.logicgo.ui.renderers.maze.MazeRenderer;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.SegmentedButton;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

import static cz.logicgo.core.misc.Messages.getFormatted;
import static cz.logicgo.ui.controllers.Openers.darkModeIconSet;
import static cz.logicgo.ui.controllers.helpers.ElementsSetterHelpers.*;


public class MazeSettingsMenu extends AbstractSettingsMenu<Maze> {

    private final ObjectProperty<boolean[][]> customMaskProperty = new SimpleObjectProperty<>();
    @FXML
    Canvas canvas;
    MazeType mazeType;
    private boolean seeded;
    private boolean custom;
    private final boolean isCustomCounts = false;
    private SegmentedButton shapeSegmentedButton;
    private ToggleGroup shapeToggleGroup;
    private SegmentedButton difficultySegmentedButton;
    private ToggleGroup difficultyToggleGroup;
    private ChoiceBox<MazeAlgorithm> mazeAlgorithmChoiceBox;
    private CheckBox timerCheckBox;
    private CheckBox sameDimensionCheckBox;
    private Label maskLabel;
    private Button btnChooseMask;
    private Label floorModeLabel;
    private SegmentedButton floorModeSegmentedButton;
    private ToggleGroup floorModeToggleGroup;
    private Label multiLevelSameLabel;
    private CheckBox multiLevelCheckBox;
    private Label multiLevelTypeLabel;
    private ChoiceBox<MazeType> multiLevelType;
    private Label floorCountLabel;
    private TextField floorCountTextBox;
    private Label classicCountLabel;
    private TextField classicCountField;
    private Label portalCountLabel;
    private TextField portalCountField;
    private Label wallsCountLabel;
    private TextField wallsCountField;
    private Label orderedCheckCountLabel;
    private TextField orderedCheckCountField;
    private TextField oneWayCountField;
    private Label wrapAroundCountLabel;
    private TextField wrapAroundCountField;
    private Label exactStepsCountLabel;
    private TextField exactStepsCountField;
    private Label tollCountLabel;
    private TextField tollCountField;
    private Label patternCountLabel;
    private TextField patternCountField;
    private Label singleCountLabel;
    private TextField singleCountField;
    private Label sameDimLabel;
    private Label dimLabel;
    private final Label xLabel = new Label("x");
    private Label difficultyLabel;
    private Label mazeShapeLabel;
    private Label mazeAlgorithmLabel;
    private Label timerLabel;
    private Label seedLabel;
    private Label showPathLabel;
    private CheckBox showPathCheckBox;
    private HBox dimensionHBox;

    private Slider widthSlider;
    private Label widthValueLabel;
    private Slider heightSlider;
    private Label heightValueLabel;

    private Button btnClearMask;
    private HBox maskButtonsBox;

    private Label seedValueLabel;
    private Button copySeedButton;

    private Label hintLabel;
    private CheckBox hintCheckBox;

    private VBox multiLevelWrapperBox;

    private final UserSettingsService userSettingsService = new UserSettingsService();
    private final GameSettingService gameSettingService = new GameSettingService();


    private boolean[][] getEffectiveMask() {
        int w = (int) widthSlider.getValue();
        int h = sameDimensionCheckBox.isSelected() ? w : (int) heightSlider.getValue();

        boolean[][] userMask = customMaskProperty.get();
        if (userMask != null && userMask.length == h && userMask[0].length == w) {
            return userMask;
        }

        boolean[][] fullMask = new boolean[h][w];
        for (int r = 0; r < h; r++) {
            java.util.Arrays.fill(fullMask[r], true);
        }
        return fullMask;
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

    private void setFloorMode(boolean isRandomMode) {
        if (floorModeSegmentedButton == null) return;

        for (ToggleButton button : floorModeSegmentedButton.getButtons()) {
            if (button.getUserData() != null && button.getUserData().equals(isRandomMode)) {
                button.setSelected(true);
                break;
            }
        }
    }

    public void initialize(TabState state, User user, MainScreenController mainController, TabChoiceWrapper gameSettings) {
        super.initialize(state, user, mainController);
        this.isInitializing = true;

        MazeInit init = (MazeInit) gameSettings.toGameInit();
        if (init == null) {
            throw new IllegalArgumentException("Unsupported or empty game settings wrapper: " + gameSettings);
        }

        this.mazeType = init.getMazeType();
        this.seeded = gameSettings.isFixed();
        this.custom = this.seeded || (gameSettings instanceof FavoriteGameWrapper);
        MazeAlgorithm targetAlgorithm = init.getMazeAlgorithm();

        setUpGridColumns();
        Map<SettingKey, Object> defaultSettings = userSettingsService.loadAllSettings(user);

        initControls(defaultSettings);
        addListeners();

        if (this.custom) {
            if (this.seeded && init.getSeed() != null) {
                this.customSeed = init.getSeed();
                this.specialSeed = init.getSeed();
                state.setSeed(String.valueOf(specialSeed));
            }

            selectToggleByUserData(difficultyToggleGroup, init.getDifficulty());
            selectToggleByUserData(shapeToggleGroup, init.getMazeShape());

            widthSlider.setValue(init.getWidth());
            heightSlider.setValue(init.getHeight());
            sameDimensionCheckBox.setSelected(init.getWidth() == init.getHeight());

            if (init.getMask() != null) {
                customMaskProperty.set(init.getMask());
                if (btnClearMask != null) btnClearMask.setDisable(this.seeded);
                widthSlider.setDisable(true);
                heightSlider.setDisable(true);
                sameDimensionCheckBox.setDisable(true);
            }

            if (init.getMazeType() == MazeType.MULTI_LEVEL && init.isHasMultipleFloors()) {
                boolean isRandom = init.getTypeCount().containsKey(MazeType.MULTI_LEVEL);

                if (isRandom) {
                    setFloorMode(true);
                    multiLevelCheckBox.setSelected(false);
                    floorCountTextBox.setText(String.valueOf(init.getTypeCount().get(MazeType.MULTI_LEVEL)));
                } else {
                    classicCountField.setText(String.valueOf(init.getTypeCount().getOrDefault(MazeType.CLASSIC, 0)));
                    portalCountField.setText(String.valueOf(init.getTypeCount().getOrDefault(MazeType.PORTAL, 0)));
                    wallsCountField.setText(String.valueOf(init.getTypeCount().getOrDefault(MazeType.WALLS, 0)));
                    patternCountField.setText(String.valueOf(init.getTypeCount().getOrDefault(MazeType.PATTERN, 0)));
                    orderedCheckCountField.setText(String.valueOf(init.getTypeCount().getOrDefault(MazeType.CHECKPOINT, 0)));
                    wrapAroundCountField.setText(String.valueOf(init.getTypeCount().getOrDefault(MazeType.WRAP_AROUND, 0)));
                    exactStepsCountField.setText(String.valueOf(init.getTypeCount().getOrDefault(MazeType.EXACT_STEPS, 0)));

                    setFloorMode(false);
                }
            }

            if (this.seeded) {
                difficultySegmentedButton.setDisable(true);
                shapeSegmentedButton.setDisable(true);
                mazeAlgorithmChoiceBox.setDisable(true);
                maskButtonsBox.setDisable(true);
                btnClearMask.setDisable(true);
                widthSlider.setDisable(true);
                heightSlider.setDisable(true);
                sameDimensionCheckBox.setDisable(true);
                if (btnChooseMask != null) btnChooseMask.setDisable(true);
                if (floorModeSegmentedButton != null) floorModeSegmentedButton.setDisable(true);
                if (floorCountTextBox != null) floorCountTextBox.setDisable(true);
            }
        }

        buildSpecificLayout();

        if (targetAlgorithm != null && mazeAlgorithmChoiceBox.getItems().contains(targetAlgorithm)) {
            mazeAlgorithmChoiceBox.getSelectionModel().select(targetAlgorithm);
        }

        this.isInitializing = false;
        settingsChanged();
    }

    private void initControls(Map<SettingKey, Object> settings) {
        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) return change;
            return null;
        };

        maskLabel = new Label(getFormatted("mazeSettings.mazeMask"));
        btnChooseMask = new Button(getFormatted("mazeSettings.chooseMask"));
        btnClearMask = new Button(getFormatted("mazeSettings.cancelMask"));
        btnClearMask.setDisable(true);

        btnChooseMask.setOnAction(_ -> openMaskWindow());
        btnClearMask.setOnAction(_ -> {
            this.customMaskProperty.set(null);
            btnClearMask.setDisable(true);
            if (!seeded) {
                widthSlider.setDisable(false);
                heightSlider.setDisable(false);
                sameDimensionCheckBox.setDisable(false);
            }
            isChangingConfigDirectly = true;
            settingsChanged();
            isChangingConfigDirectly = false;
        });

        maskButtonsBox = new HBox(10, btnChooseMask, btnClearMask);

        mazeShapeLabel = new Label(getFormatted("mazeSettings.mazeShape"));
        shapeToggleGroup = new ToggleGroup();
        shapeSegmentedButton = createMazeShapeSegmentedButton(shapeToggleGroup);
        shapeToggleGroup = shapeSegmentedButton.getToggleGroup();
        selectToggleByUserData(shapeToggleGroup, MazeShape.RECTANGULAR);

        mazeAlgorithmLabel = new Label(getFormatted("mazeSettings.algorithm"));
        mazeAlgorithmChoiceBox = new ChoiceBox<>();

        sameDimLabel = new Label(getFormatted("mazeSettings.checkSameDim"));
        sameDimensionCheckBox = new CheckBox();
        sameDimensionCheckBox.setSelected((Boolean) settings.getOrDefault(MazeSettings.SAME_DIMENSION, false));

        dimLabel = new Label();
        dimensionHBox = new HBox(10);

        widthSlider = new Slider(10, 50, 30);
        widthSlider.setBlockIncrement(1);
        widthSlider.setMajorTickUnit(1);
        widthSlider.setMinorTickCount(0);
        widthSlider.setSnapToTicks(true);
        widthValueLabel = new Label("30");

        heightSlider = new Slider(10, 50, 30);
        heightSlider.setBlockIncrement(1);
        heightSlider.setMajorTickUnit(1);
        heightSlider.setMinorTickCount(0);
        heightSlider.setSnapToTicks(true);
        heightValueLabel = new Label("30");

        singleCountLabel = new Label();
        String text = switch (mazeType) {
            case WALLS, PORTAL -> getFormatted("mazeSettings.regionCount");
            case PATTERN -> getFormatted("mazeSettings.patternCount");
            case CHECKPOINT -> getFormatted("mazeSettings.checkpointCount");
            case WRAP_AROUND -> getFormatted("mazeSettings.wrapCount");
            default -> "";
        };
        singleCountLabel.setText(text);
        singleCountField = new TextField();
        singleCountField.setTextFormatter(new TextFormatter<>(integerFilter));

        multiLevelWrapperBox = new VBox(15);
        multiLevelWrapperBox.setStyle("-fx-border-color: #3f4246; -fx-border-width: 1px; -fx-padding: 15; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-background-color: -color-bg-input;");

        if (mazeType == MazeType.MULTI_LEVEL) {
            floorModeLabel = new Label(getFormatted("mazeSettings.floorMode"));
            ToggleButton btnRandom = new ToggleButton(getFormatted("mazeSettings.randomMode"));
            btnRandom.setUserData(true);
            ToggleButton btnCustom = new ToggleButton(getFormatted("mazeSettings.customCountsMode"));
            btnCustom.setUserData(false);

            floorModeSegmentedButton = new SegmentedButton(btnRandom, btnCustom);
            floorModeToggleGroup = floorModeSegmentedButton.getToggleGroup();
            setUpSegmentedButtonHandling(floorModeSegmentedButton, btnRandom);

            multiLevelSameLabel = new Label(getFormatted("mazeSettings.multiLevelSame"));
            multiLevelCheckBox = new CheckBox();
            multiLevelTypeLabel = new Label(getFormatted("mazeSettings.multiLevelType"));
            multiLevelType = new ChoiceBox<>();
            setItemsMazeTypeChoiceBox(canvas, multiLevelType);

            floorCountLabel = new Label(getFormatted("mazeSettings.floorCount"));
            floorCountTextBox = new TextField("3");
            floorCountTextBox.setTextFormatter(new TextFormatter<>(integerFilter));

            classicCountLabel = new Label(MazeType.CLASSIC.getTranslation());
            classicCountField = new TextField("3");
            classicCountField.setTextFormatter(new TextFormatter<>(integerFilter));

            portalCountLabel = new Label(MazeType.PORTAL.getTranslation());
            portalCountField = new TextField("0");
            portalCountField.setTextFormatter(new TextFormatter<>(integerFilter));

            wallsCountLabel = new Label(MazeType.WALLS.getTranslation());
            wallsCountField = new TextField("0");
            wallsCountField.setTextFormatter(new TextFormatter<>(integerFilter));

            tollCountLabel = new Label(getFormatted("mazeSettings.tollFloorCount"));
            tollCountField = new TextField("0");
            tollCountField.setTextFormatter(new TextFormatter<>(integerFilter));

            patternCountLabel = new Label(MazeType.PATTERN.getTranslation());
            patternCountField = new TextField("0");
            patternCountField.setTextFormatter(new TextFormatter<>(integerFilter));

            orderedCheckCountLabel = new Label(MazeType.CHECKPOINT.getTranslation());
            orderedCheckCountField = new TextField("0");
            orderedCheckCountField.setTextFormatter(new TextFormatter<>(integerFilter));

            oneWayCountField = new TextField("0");
            oneWayCountField.setTextFormatter(new TextFormatter<>(integerFilter));

            wrapAroundCountLabel = new Label(MazeType.WRAP_AROUND.getTranslation());
            wrapAroundCountField = new TextField("0");
            wrapAroundCountField.setTextFormatter(new TextFormatter<>(integerFilter));

            exactStepsCountLabel = new Label(MazeType.EXACT_STEPS.getTranslation());
            exactStepsCountField = new TextField("0");
            exactStepsCountField.setTextFormatter(new TextFormatter<>(integerFilter));
        }

        difficultyLabel = new Label(getFormatted("gameSettings.difficulty"));
        difficultyToggleGroup = new ToggleGroup();
        difficultySegmentedButton = createDifficultySegmentedButton();
        difficultyToggleGroup = difficultySegmentedButton.getToggleGroup();
        selectSegmentedButtonByUserData(difficultySegmentedButton, Difficulty.MEDIUM);

        hintLabel = new Label(getFormatted("gameSettings.hints.label"));
        hintCheckBox = new CheckBox();
        boolean hintsOn = (Boolean) settings.getOrDefault(SudokuSettings.HINTS_ON, true);
        hintCheckBox.setSelected(hintsOn);

        showPathLabel = new Label(getFormatted("gameSettings.showPath"));
        showPathCheckBox = new CheckBox();

        timerLabel = new Label(getFormatted("gameSettings.timer"));
        timerCheckBox = new CheckBox();
        timerCheckBox.setSelected((Boolean) settings.getOrDefault(SudokuSettings.TIMER, false));

        seedLabel = new Label(getFormatted("gameSettings.seed"));
        seedValueLabel = new Label();
        seedValueLabel.setStyle("-fx-font-weight: bold;");

        copySeedButton = new Button(getFormatted("gameSettings.copy"));
        copySeedButton.setOnAction(e -> {
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(seedValueLabel.getText());
            javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);
        });

        sameDimensionCheckBox.setSelected((Boolean) settings.getOrDefault(MazeSettings.SAME_DIMENSION, false));
        showPathCheckBox.setSelected((Boolean) settings.getOrDefault(MazeSettings.SHOW_TRAVEL_PATH, false));
        timerCheckBox.setSelected((Boolean) settings.getOrDefault(SudokuSettings.TIMER, false));
    }

    private void addListeners() {
        sameDimensionCheckBox.selectedProperty().addListener((obs, oldV, newV) -> {
            if (newV) {
                heightSlider.setValue(widthSlider.getValue());
            }
            buildSpecificLayout();
            isChangingConfigDirectly = true;
            settingsChanged();
            isChangingConfigDirectly = false;
        });

        shapeToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                MazeShape selectedShape = (MazeShape) newVal.getUserData();
                if (selectedShape == MazeShape.HEXAGONAL) {
                    customMaskProperty.set(null);
                    if (!seeded) {
                        widthSlider.setDisable(false);
                        heightSlider.setDisable(false);
                        sameDimensionCheckBox.setDisable(false);
                    }
                }
                buildSpecificLayout();
                isChangingConfigDirectly = true;
                settingsChanged();
                isChangingConfigDirectly = false;
            }
        });

        mazeAlgorithmChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                isChangingConfigDirectly = true;
                settingsChanged();
                isChangingConfigDirectly = false;
            }
        });

        difficultyToggleGroup.selectedToggleProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                isChangingConfigDirectly = true;
                settingsChanged();
                isChangingConfigDirectly = false;
            }
        });

        hintCheckBox.selectedProperty().addListener((obs, oldV, newV) -> {
            isChangingConfigDirectly = true;
            settingsChanged();
            isChangingConfigDirectly = false;
        });

        customMaskProperty.addListener((obs, oldMask, newMask) -> {
            isChangingConfigDirectly = true;

            buildSpecificLayout();

            if (newMask != null) {
                settingsChanged();
                btnClearMask.setDisable(false);
            } else {
                settingsChanged();
                btnClearMask.setDisable(true);
            }
            isChangingConfigDirectly = false;
        });

        singleCountField.focusedProperty().addListener((obs, o, n) -> {
            if (!n) {
                isChangingConfigDirectly = true;
                settingsChanged();
                isChangingConfigDirectly = false;
            }
        });

        widthSlider.valueProperty().addListener((obs, oldV, newV) -> {
            int val = newV.intValue();
            widthValueLabel.setText(String.valueOf(val));
            if (sameDimensionCheckBox.isSelected()) {
                heightSlider.setValue(val);
            }

            if (customMaskProperty.get() != null && !seeded) {
                customMaskProperty.set(null);
            }

            isChangingConfigDirectly = true;
            settingsChanged();
            isChangingConfigDirectly = false;
        });

        heightSlider.valueProperty().addListener((obs, oldV, newV) -> {
            int val = newV.intValue();
            heightValueLabel.setText(String.valueOf(val));

            if (customMaskProperty.get() != null && !seeded) {
                customMaskProperty.set(null);
            }

            isChangingConfigDirectly = true;
            settingsChanged();
            isChangingConfigDirectly = false;
        });

        if (mazeType == MazeType.MULTI_LEVEL) {
            multiLevelCheckBox.setOnAction(e -> {
                buildSpecificLayout();
                isChangingConfigDirectly = true;
                settingsChanged();
                isChangingConfigDirectly = false;
            });

            multiLevelType.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                if (newV != null) {
                    buildSpecificLayout();
                    isChangingConfigDirectly = true;
                    settingsChanged();
                    isChangingConfigDirectly = false;
                }
            });

            floorModeToggleGroup.selectedToggleProperty().addListener((obs, oldV, newV) -> {
                if (newV != null) {
                    buildSpecificLayout();
                    isChangingConfigDirectly = true;
                    settingsChanged();
                    isChangingConfigDirectly = false;
                }
            });
            floorCountTextBox.focusedProperty().addListener((obs, oldV, newV) -> {
                if (!newV) {
                    isChangingConfigDirectly = true;
                    settingsChanged();
                    isChangingConfigDirectly = false;
                }
            });
            classicCountField.focusedProperty().addListener((obs, oldV, newV) -> {
                if (!newV) {
                    isChangingConfigDirectly = true;
                    settingsChanged();
                    isChangingConfigDirectly = false;
                }
            });
            portalCountField.focusedProperty().addListener((obs, oldV, newV) -> {
                if (!newV) {
                    isChangingConfigDirectly = true;
                    settingsChanged();
                    isChangingConfigDirectly = false;
                }
            });
            wallsCountField.focusedProperty().addListener((obs, oldV, newV) -> {
                if (!newV) {
                    isChangingConfigDirectly = true;
                    settingsChanged();
                    isChangingConfigDirectly = false;
                }
            });
            orderedCheckCountField.focusedProperty().addListener((obs, oldV, newV) -> {
                if (!newV) {
                    isChangingConfigDirectly = true;
                    settingsChanged();
                    isChangingConfigDirectly = false;
                }
            });
            tollCountField.focusedProperty().addListener((obs, oldV, newV) -> {
                if (!newV) {
                    isChangingConfigDirectly = true;
                    settingsChanged();
                    isChangingConfigDirectly = false;
                }
            });
            patternCountField.focusedProperty().addListener((obs, oldV, newV) -> {
                if (!newV) {
                    isChangingConfigDirectly = true;
                    settingsChanged();
                    isChangingConfigDirectly = false;
                }
            });
            oneWayCountField.focusedProperty().addListener((obs, oldV, newV) -> {
                if (!newV) {
                    isChangingConfigDirectly = true;
                    settingsChanged();
                    isChangingConfigDirectly = false;
                }
            });
            wrapAroundCountField.focusedProperty().addListener((obs, oldV, newV) -> {
                if (!newV) {
                    isChangingConfigDirectly = true;
                    settingsChanged();
                    isChangingConfigDirectly = false;
                }
            });
            exactStepsCountField.focusedProperty().addListener((obs, oldV, newV) -> {
                if (!newV) {
                    isChangingConfigDirectly = true;
                    settingsChanged();
                    isChangingConfigDirectly = false;
                }
            });
        }
    }

    public void openMaskWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(StartOfApp.class.getResource("/cz/logicgo/ui/windows/gameChoice/viewer.fxml"));
            MaskViewerController controller = new MaskViewerController();
            loader.setController(controller);

            Parent content = loader.load();
            content.getStyleClass().add("root");
            Stage stage = new Stage();
            ChosenMask chM = new ChosenMask();

            controller.initialize(user, chM, TypeGame.MAZE);
            controller.setStage(stage);

            stage.setTitle(getFormatted("mazeSettings.maskSelectionTitle"));

            stage.setMinWidth(750);
            stage.setMinHeight(550);
            stage.setMaxWidth(1200);
            stage.setMaxHeight(850);

            stage.setScene(new Scene(content, 750, 550));
            stage.initModality(Modality.APPLICATION_MODAL);
            darkModeIconSet(stage);
            stage.showAndWait();

            if (chM.getMask() != null) {
                var mask = chM.getMask();
                int maskHeight = mask.length;
                int maskWidth = mask[0].length;

                widthSlider.setValue(maskWidth);
                heightSlider.setValue(maskHeight);

                widthSlider.setDisable(true);
                heightSlider.setDisable(true);
                sameDimensionCheckBox.setSelected(maskWidth == maskHeight);
                sameDimensionCheckBox.setDisable(true);

                btnClearMask.setDisable(false);

                this.customMaskProperty.set(mask);

                var algo = mazeAlgorithmChoiceBox.getValue();
                if (!MazeType.MULTI_LEVEL.getSupportedAlgorithms().contains(algo)) {
                    mazeAlgorithmChoiceBox.getSelectionModel().select(MazeAlgorithm.RECURSIVE_BACKTRACKER);
                }

                startBackgroundGeneration();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleOk() {
        if (!isConfigValid()) {
            showValidationErrorDialog(getFormatted("mazeSettings.multiLevelFloorError") != null
                    ? getFormatted("mazeSettings.multiLevelFloorError")
                    : "Počet pater musí být v rozmezí 2 až 200.");
            return;
        }
        super.handleOk();
    }

    @Override
    protected boolean isConfigValid() {
        if (mazeType == MazeType.MULTI_LEVEL) {
            int totalFloors = 0;
            Toggle modeToggle = floorModeToggleGroup.getSelectedToggle();
            boolean isRandomMode = modeToggle == null || (boolean) modeToggle.getUserData();

            if (isRandomMode) {
                try {
                    totalFloors = Integer.parseInt(floorCountTextBox.getText());
                } catch (NumberFormatException _) {
                    return false;
                }
            } else {
                totalFloors += classicCountField.getText().isEmpty() ? 0 : Integer.parseInt(classicCountField.getText());
                totalFloors += portalCountField.getText().isEmpty() ? 0 : Integer.parseInt(portalCountField.getText());
                totalFloors += wallsCountField.getText().isEmpty() ? 0 : Integer.parseInt(wallsCountField.getText());
                totalFloors += patternCountField.getText().isEmpty() ? 0 : Integer.parseInt(patternCountField.getText());
                totalFloors += orderedCheckCountField.getText().isEmpty() ? 0 : Integer.parseInt(orderedCheckCountField.getText());
                totalFloors += wrapAroundCountField.getText().isEmpty() ? 0 : Integer.parseInt(wrapAroundCountField.getText());
                totalFloors += exactStepsCountField.getText().isEmpty() ? 0 : Integer.parseInt(exactStepsCountField.getText());
            }

            return totalFloors >= 2 && totalFloors <= 200;
        }
        return true;
    }

    private void showValidationErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(getFormatted("validation.errorTitle") != null ? getFormatted("validation.errorTitle") : "Chyba nastavení");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    protected Maze createGameInBackground() throws Exception {
        MazeInit config = createConfig();
        return MazeGameFactory.createGame(config);
    }

    @Override
    protected void onGameGenerated(Maze resultGame) {
        if (resultGame == null) {
            resetButtons();
            return;
        }
        Platform.runLater(() -> {
            try {
                MazeInit config = new MazeInit();
                config.setPlayer(user);
                createSettings(config);
                List<GameSetting> settings = gameSettingService.prepareGameSettings(resultGame, config.getSettings());
                if (!config.getSettings().isEmpty()) {
                    userSettingsService.saveSettingsFromBuffer(config);
                }
                resultGame.setSettings(settings);
                mainController.closeCurrentAndReplace(TabType.MAZE, new LoadedGameWrapper(resultGame));
            } catch (IOException ex) {
                ex.printStackTrace();
                resetButtons();
            }
        });
    }

    @Override
    protected GameFavorite createFavoriteFromCurrentSettings() {
        if (!isConfigValid()) return null;
        MazeInit config = createConfig();
        if (config == null) return null;

        MazeFavorite fav = new MazeFavorite();
        fav.setDifficulty(config.getDifficulty());
        fav.setWidth(config.getWidth());
        fav.setHeight(config.getHeight());
        fav.setMazeType(config.getMazeType());
        fav.setMazeShape(config.getMazeShape());
        fav.setMazeAlgorithm(config.getMazeAlgorithm());
        fav.setMask(config.getMask());

        if (config.getMazeType() == MazeType.MULTI_LEVEL) {
            fav.setHasMultipleFloors(true);
            fav.getTypeCount().putAll(config.getTypeCount());
        }

        return fav;
    }

    @Override
    protected void showRealPreview(Maze game) {
        if (game == null || canvas == null) return;

        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double padding = Math.min(canvas.getWidth(), canvas.getHeight()) * 0.05;
        double availW = canvas.getWidth() - 2 * padding;
        double availH = canvas.getHeight() - 2 * padding;

        int cols = game.getWidth();
        int rows = game.getHeight();
        double aw = cols;
        double ah = rows;
        double aspect = 1.0;

        if (game.getMazeShape() == MazeShape.HEXAGONAL) {
            aw = 1.0 + 0.75 * (cols - 1);
            ah = rows + (cols > 1 ? 0.5 : 0.0);
            aspect = Math.sqrt(3) / 2.0;
        }

        double cellW = Math.min(availW / aw, availH / (ah * aspect));
        double cellH = cellW * aspect;

        double actualW = cellW * aw;
        double actualH = cellH * ah;

        int currentFloor = 0;
        int totalFloors = game.getMazeGridFloors().size();
        MazeGrid grid = game.getMazeGridFloors().get(currentFloor).getMazeGrid();

        Canvas tempCanvas = new Canvas(actualW, actualH);
        MazeRenderer.renderGrid(tempCanvas, grid, currentFloor, totalFloors, false, user);

        if (grid.getPath() != null) {
            MazeRenderer.renderPath(tempCanvas, grid.getPath(), grid, false);
        }

        double tx = (canvas.getWidth() - actualW) / 2.0;
        double ty = (canvas.getHeight() - actualH) / 2.0;

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(javafx.scene.paint.Color.TRANSPARENT);
        javafx.scene.image.WritableImage image = tempCanvas.snapshot(params, null);

        gc.drawImage(image, tx, ty);
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
    void updateCanvasPreview() {
        if (canvas == null) return;

        var bgTask = backgroundCreationTask.get();
        if (bgTask != null && bgTask.isDone() && !bgTask.isCancelled()) {
            try {
                Maze currentPreviewGame = bgTask.get();
                if (currentPreviewGame != null) {
                    showRealPreview(currentPreviewGame);
                }
            } catch (Exception e) {
                javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            }
        }
    }

    @Override
    protected void buildSpecificLayout() {
        gridPane.getChildren().clear();
        int row = 0;

        addGridRow(row++, sameDimLabel, sameDimensionCheckBox);

        dimensionHBox.getChildren().clear();
        if (sameDimensionCheckBox.isSelected()) {
            dimLabel.setText(getFormatted("mazeSettings.sameDimension"));
            dimensionHBox.getChildren().addAll(widthSlider, widthValueLabel);
        } else {
            dimLabel.setText(getFormatted("mazeSettings.widthXheight"));
            dimensionHBox.getChildren().addAll(widthSlider, widthValueLabel, xLabel, heightSlider, heightValueLabel);
        }
        addGridRow(row++, dimLabel, dimensionHBox);

        addGridRow(row++, mazeShapeLabel, shapeSegmentedButton);

        MazeType activeTypeForAlgorithms = mazeType;

        if (Objects.requireNonNull(mazeType) == MazeType.MULTI_LEVEL) {
            multiLevelWrapperBox.getChildren().clear();

            GridPane innerGrid = new GridPane();
            innerGrid.setHgap(15);
            innerGrid.setVgap(10);
            int innerRow = 0;

            innerGrid.add(floorModeLabel, 0, innerRow);
            innerGrid.add(floorModeSegmentedButton, 1, innerRow++);

            Toggle modeToggle = floorModeToggleGroup.getSelectedToggle();
            boolean isRandomMode = modeToggle == null || (boolean) modeToggle.getUserData();

            if (isRandomMode) {
                innerRow = buildMultiLevelRandomMode(innerGrid, innerRow);
                if (multiLevelCheckBox.isSelected() && multiLevelType.getValue() != null) {
                    activeTypeForAlgorithms = multiLevelType.getValue();
                }
            } else {
                innerRow = buildMultiLevelCustomMode(innerGrid, innerRow);
            }

            multiLevelWrapperBox.getChildren().add(innerGrid);
            gridPane.add(multiLevelWrapperBox, 0, row++, 2, 1);
        } else {
            if (mazeType != MazeType.CLASSIC && isCustomCounts) {
                addGridRow(row++, singleCountLabel, singleCountField);
            }
        }

        boolean hasMask = customMaskProperty.get() != null;
        if (hasMask) {
            activeTypeForAlgorithms = MazeType.MULTI_LEVEL;
        }

        Toggle shapeToggle = shapeToggleGroup.getSelectedToggle();
        MazeShape selectedShape = shapeToggle != null ? (MazeShape) shapeToggle.getUserData() : MazeShape.RECTANGULAR;

        updateAlgorithmItems(selectedShape, activeTypeForAlgorithms, hasMask, mazeAlgorithmChoiceBox);

        if (mazeAlgorithmChoiceBox.getItems() != null) {
            if (seeded) {
                mazeAlgorithmChoiceBox.setDisable(true);
            }
            if (mazeAlgorithmChoiceBox.getItems().size() > 1) {
                addGridRow(row++, mazeAlgorithmLabel, mazeAlgorithmChoiceBox);
            }
        }

        if (selectedShape != MazeShape.HEXAGONAL) {
            addGridRow(row++, maskLabel, maskButtonsBox);
        }

        addGridRow(row++, difficultyLabel, difficultySegmentedButton);

        addGridRow(row++, hintLabel, hintCheckBox);

        addGridRow(row++, showPathLabel, showPathCheckBox);
        addGridRow(row++, timerLabel, timerCheckBox);

        HBox seedActionsBox = new HBox(10, seedValueLabel, copySeedButton);
        seedActionsBox.setMaxWidth(Double.MAX_VALUE);
        seedActionsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        if (seedLabel != null) {
            GridPane.setValignment(seedLabel, javafx.geometry.VPos.CENTER);
            addGridRow(row++, seedLabel, seedActionsBox);
        }
    }

    private int buildMultiLevelRandomMode(GridPane innerGrid, int innerRow) {
        innerGrid.add(multiLevelSameLabel, 0, innerRow);
        innerGrid.add(multiLevelCheckBox, 1, innerRow++);
        if (multiLevelCheckBox.isSelected()) {
            innerGrid.add(multiLevelTypeLabel, 0, innerRow);
            innerGrid.add(multiLevelType, 1, innerRow++);
        }
        innerGrid.add(floorCountLabel, 0, innerRow);
        innerGrid.add(floorCountTextBox, 1, innerRow++);
        return innerRow;
    }

    private int buildMultiLevelCustomMode(GridPane innerGrid, int innerRow) {
        if (classicCountLabel != null && classicCountField != null) innerGrid.add(classicCountLabel, 0, innerRow);
        if (classicCountField != null) innerGrid.add(classicCountField, 1, innerRow++);

        if (portalCountLabel != null && portalCountField != null) innerGrid.add(portalCountLabel, 0, innerRow);
        if (portalCountField != null) innerGrid.add(portalCountField, 1, innerRow++);

        if (wallsCountLabel == null) wallsCountLabel = new Label(getFormatted("mazeSettings.wallsCount"));
        innerGrid.add(wallsCountLabel, 0, innerRow);
        if (wallsCountField != null) innerGrid.add(wallsCountField, 1, innerRow++);

        if (patternCountLabel != null && patternCountField != null) innerGrid.add(patternCountLabel, 0, innerRow);
        if (patternCountField != null) innerGrid.add(patternCountField, 1, innerRow++);

        if (orderedCheckCountLabel != null && orderedCheckCountField != null)
            innerGrid.add(orderedCheckCountLabel, 0, innerRow);
        if (orderedCheckCountField != null) innerGrid.add(orderedCheckCountField, 1, innerRow++);

        if (wrapAroundCountLabel != null && wrapAroundCountField != null)
            innerGrid.add(wrapAroundCountLabel, 0, innerRow);
        if (wrapAroundCountField != null) innerGrid.add(wrapAroundCountField, 1, innerRow++);

        if (exactStepsCountLabel != null && exactStepsCountField != null)
            innerGrid.add(exactStepsCountLabel, 0, innerRow);
        if (exactStepsCountField != null) innerGrid.add(exactStepsCountField, 1, innerRow++);
        return innerRow;
    }

    private void createSettings(MazeInit config) {
        config.addSetting(MazeSettings.HINTS_ON, hintCheckBox.isSelected());
        config.addSetting(MazeSettings.TIMER, timerCheckBox.isSelected());
        config.addSetting(MazeSettings.SHOW_TRAVEL_PATH, showPathCheckBox.isSelected());
    }

    @Override
    public MazeInit createConfig() {
        try {
            MazeInit config = new MazeInit(user).setMazeType(mazeType);
            createSettings(config);
            config.addSetting(MazeSettings.SHOW_TRAVEL_PATH, showPathCheckBox.isSelected());

            Toggle diffToggle = difficultyToggleGroup.getSelectedToggle();
            Difficulty diff = diffToggle != null ? (Difficulty) diffToggle.getUserData() : Difficulty.MEDIUM;
            config.setDifficulty(diff);

            Toggle shapeToggle = shapeToggleGroup.getSelectedToggle();
            MazeShape shape = shapeToggle != null ? (MazeShape) shapeToggle.getUserData() : MazeShape.RECTANGULAR;
            config.setMazeShape(shape);

            config.setMask(customMaskProperty.get());
            config.setMazeAlgorithm(mazeAlgorithmChoiceBox.getSelectionModel().getSelectedItem());

            if (mazeType == MazeType.MULTI_LEVEL) {
                config.setHasMultipleFloors(true);

                Toggle modeToggle = floorModeToggleGroup.getSelectedToggle();
                boolean mode = modeToggle == null || (boolean) modeToggle.getUserData();

                if (mode) {
                    Integer floorCount = Integer.parseInt(floorCountTextBox.getText());
                    if (multiLevelCheckBox.isSelected()) {
                        MazeType realType = multiLevelType.getValue();
                        config.setTypeCount(realType, floorCount);
                    } else {
                        config.setTypeCount(MazeType.MULTI_LEVEL, floorCount);
                    }
                } else {
                    int classic = classicCountField.getText().isEmpty() ? 0 : Integer.parseInt(classicCountField.getText());
                    int portal = portalCountField.getText().isEmpty() ? 0 : Integer.parseInt(portalCountField.getText());
                    int walls = wallsCountField.getText().isEmpty() ? 0 : Integer.parseInt(wallsCountField.getText());
                    int toll = tollCountField.getText().isEmpty() ? 0 : Integer.parseInt(tollCountField.getText());
                    int pattern = patternCountField.getText().isEmpty() ? 0 : Integer.parseInt(patternCountField.getText());
                    int ordered = orderedCheckCountField.getText().isEmpty() ? 0 : Integer.parseInt(orderedCheckCountField.getText());
                    int oneWay = oneWayCountField.getText().isEmpty() ? 0 : Integer.parseInt(oneWayCountField.getText());
                    int wrap = wrapAroundCountField.getText().isEmpty() ? 0 : Integer.parseInt(wrapAroundCountField.getText());
                    int exactSteps = exactStepsCountField.getText().isEmpty() ? 0 : Integer.parseInt(exactStepsCountField.getText());

                    config.getTypeCount().clear();
                    if (classic > 0) config.setTypeCount(MazeType.CLASSIC, classic);
                    if (portal > 0) config.setTypeCount(MazeType.PORTAL, portal);
                    if (walls > 0) config.setTypeCount(MazeType.WALLS, walls);
                    if (pattern > 0) config.setTypeCount(MazeType.PATTERN, pattern);
                    if (ordered > 0) config.setTypeCount(MazeType.CHECKPOINT, ordered);
                    if (wrap > 0) config.setTypeCount(MazeType.WRAP_AROUND, wrap);
                    if (exactSteps > 0) config.setTypeCount(MazeType.EXACT_STEPS, exactSteps);
                }
            } else {
                config.setHasMultipleFloors(false);
                config.setTypeCount(mazeType, 1);
            }

            int width = (int) widthSlider.getValue();
            config.setWidth(width);
            if (sameDimensionCheckBox.isSelected()) {
                config.setHeight(width);
            } else {
                config.setHeight((int) heightSlider.getValue());
            }

            config.setMask(getEffectiveMask());
            config.setMazeAlgorithm(mazeAlgorithmChoiceBox.getSelectionModel().getSelectedItem());

            Long effectiveSeed = (specialSeed != null) ? specialSeed : customSeed;
            if (effectiveSeed != null) {
                config.setSeed(effectiveSeed);
            }

            return config;
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }
}
