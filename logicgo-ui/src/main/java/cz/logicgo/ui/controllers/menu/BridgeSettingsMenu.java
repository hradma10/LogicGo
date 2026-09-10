package cz.logicgo.ui.controllers.menu;

import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.entity.setting.GameSetting;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.factoryInit.bridge.BridgeInit;
import cz.logicgo.core.gameClasses.favorites.BridgeFavorite;
import cz.logicgo.core.gameClasses.favorites.GameFavorite;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.TabType;
import cz.logicgo.core.misc.enums.gameTypes.bridge.BridgeType;
import cz.logicgo.core.misc.enums.settings.BridgeSettings;
import cz.logicgo.core.misc.enums.settings.SettingKey;
import cz.logicgo.core.misc.enums.settings.SudokuSettings;
import cz.logicgo.engine.util.generate.RandomizationFactory;
import cz.logicgo.engine.util.generate.SeedCreator;
import cz.logicgo.persistence.services.GameSettingService;
import cz.logicgo.ui.controllers.screenControllers.MainScreenController;
import cz.logicgo.ui.factories.BridgeGameFactory;
import cz.logicgo.ui.misc.TabState;
import cz.logicgo.ui.misc.tabChoosingClasses.FavoriteGameWrapper;
import cz.logicgo.ui.misc.tabChoosingClasses.LoadedGameWrapper;
import cz.logicgo.ui.misc.tabChoosingClasses.TabChoiceWrapper;
import cz.logicgo.ui.renderers.BridgeRenderer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.controlsfx.control.SegmentedButton;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static cz.logicgo.core.misc.Messages.getFormatted;
import static cz.logicgo.ui.controllers.helpers.ElementsSetterHelpers.*;


public class BridgeSettingsMenu extends AbstractSettingsMenu<Bridge> {
    @FXML
    Canvas canvas;
    BridgeType bridgeType;

    private boolean custom;
    private boolean seeded;

    private SegmentedButton difficultySegmentedButton;
    private ToggleGroup difficultyToggleGroup;

    private CheckBox timerCheckBox;
    private CheckBox sameDimensionCheckBox;

    private Label sameDimLabel;
    private Label dimLabel;
    private Label xLabel;
    private Label difficultyLabel;
    private Label timerLabel;
    private Label seedLabel;
    private Label multipleCountLabel;

    private HBox dimensionHBox;

    private Slider widthSlider;
    private Label widthValueLabel;
    private Slider heightSlider;
    private Label heightValueLabel;

    private SegmentedButton multipleCountSegmentedButton;
    private ToggleGroup multipleCountToggleGroup;

    private Label seedValueLabel;
    private Button copySeedButton;

    private Label hintLabel;
    private CheckBox hintCheckBox;

    private final GameSettingService gameSettingService = new GameSettingService();

    @Override
    protected GameFavorite createFavoriteFromCurrentSettings() {
        if (!isConfigValid()) return null;
        BridgeInit config = createConfig();
        if (config == null) return null;

        BridgeFavorite fav = new BridgeFavorite();
        fav.setDifficulty(config.getDifficulty());
        fav.setWidth(config.getWidth());
        fav.setHeight(config.getHeight());
        fav.setBridgeType(config.getBridgeType());

        return fav;
    }

    @Override
    protected void showRealPreview(Bridge game) {
        if (game == null || canvas == null) return;

        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.save();

        BridgeRenderer.render(canvas, game);
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

        BridgeInit init = (BridgeInit) gameSettings.toGameInit();
        if (init == null) {
            throw new IllegalArgumentException("Unsupported or empty game settings wrapper: " + gameSettings);
        }

        this.bridgeType = init.getBridgeType();
        this.seeded = gameSettings.isFixed();
        this.custom = this.seeded || (gameSettings instanceof FavoriteGameWrapper);

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

            selectSegmentedButtonByUserData(difficultySegmentedButton, init.getDifficulty());

            widthSlider.setValue(init.getWidth());
            heightSlider.setValue(init.getHeight());
            sameDimensionCheckBox.setSelected(init.getWidth() == init.getHeight());

            if (init.getBridgeType() == BridgeType.MULTIPLE && multipleCountSegmentedButton != null) {
                selectSegmentedButtonByUserData(multipleCountSegmentedButton, init.getMultipleCount());
            }

            if (this.seeded) {
                difficultySegmentedButton.setDisable(true);
                widthSlider.setDisable(true);
                heightSlider.setDisable(true);
                sameDimensionCheckBox.setDisable(true);
                if (multipleCountSegmentedButton != null) {
                    multipleCountSegmentedButton.setDisable(true);
                }
            }
        }

        buildSpecificLayout();
        this.isInitializing = false;
        settingsChanged();
    }

    private void initControls(Map<SettingKey, Object> settings) {
        sameDimLabel = new Label(getFormatted("bridgeSettings.checkSameDim"));

        sameDimensionCheckBox = new CheckBox();
        sameDimensionCheckBox.setSelected((Boolean) settings.getOrDefault(BridgeSettings.SAME_DIMENSION, false));

        dimLabel = new Label();
        dimensionHBox = new HBox(10);

        widthSlider = new Slider(8, 23, 8);
        widthSlider.setBlockIncrement(1);
        widthSlider.setMajorTickUnit(1);
        widthSlider.setMinorTickCount(0);
        widthSlider.setSnapToTicks(true);
        widthValueLabel = new Label("7");

        xLabel = new Label("x");

        heightSlider = new Slider(8, 23, 8);
        heightSlider.setBlockIncrement(1);
        heightSlider.setMajorTickUnit(1);
        heightSlider.setMinorTickCount(0);
        heightSlider.setSnapToTicks(true);
        heightValueLabel = new Label("7");

        multipleCountLabel = new Label(getFormatted("bridgeSettings.multipleCount"));
        multipleCountToggleGroup = new ToggleGroup();
        multipleCountSegmentedButton = createMultipleCountSegmentedButton(multipleCountToggleGroup);
        multipleCountToggleGroup = multipleCountSegmentedButton.getToggleGroup();

        difficultyLabel = new Label(getFormatted("gameSettings.difficulty"));
        difficultyToggleGroup = new ToggleGroup();
        difficultySegmentedButton = createDifficultySegmentedButton();
        difficultyToggleGroup = difficultySegmentedButton.getToggleGroup();
        selectToggleByUserData(difficultyToggleGroup, Difficulty.MEDIUM);

        hintLabel = new Label(getFormatted("gameSettings.hints.label"));
        hintCheckBox = new CheckBox();
        boolean hintsOn = (Boolean) settings.getOrDefault(SudokuSettings.HINTS_ON, true);
        hintCheckBox.setSelected(hintsOn);

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

        boolean sameDim = (Boolean) settings.getOrDefault(BridgeSettings.SAME_DIMENSION, false);
        sameDimensionCheckBox.setSelected(sameDim);

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

        difficultyToggleGroup.selectedToggleProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                isChangingConfigDirectly = true;
                settingsChanged();
                isChangingConfigDirectly = false;
            }
        });

        if (multipleCountToggleGroup != null) {
            multipleCountToggleGroup.selectedToggleProperty().addListener((obs, oldV, newV) -> {
                if (newV != null) {
                    isChangingConfigDirectly = true;
                    settingsChanged();
                    isChangingConfigDirectly = false;
                }
            });
        }

        hintCheckBox.selectedProperty().addListener((obs, oldV, newV) -> {
            isChangingConfigDirectly = true;
            settingsChanged();
            isChangingConfigDirectly = false;
        });

        widthSlider.valueProperty().addListener((obs, oldV, newV) -> {
            int val = newV.intValue();
            widthValueLabel.setText(String.valueOf(val));
            if (sameDimensionCheckBox.isSelected()) {
                heightSlider.setValue(val);
            } else {
                int currentHeight = (int) heightSlider.getValue();
                if (Math.abs(currentHeight - val) > 5) {
                    double targetHeight = val + (currentHeight > val ? 5 : -5);
                    heightSlider.setValue(Math.clamp(targetHeight, heightSlider.getMin(), heightSlider.getMax()));
                }
            }
            isChangingConfigDirectly = true;
            settingsChanged();
            isChangingConfigDirectly = false;
        });

        heightSlider.valueProperty().addListener((obs, oldV, newV) -> {
            int val = newV.intValue();
            heightValueLabel.setText(String.valueOf(val));
            if (!sameDimensionCheckBox.isSelected()) {
                int currentWidth = (int) widthSlider.getValue();
                if (Math.abs(currentWidth - val) > 5) {
                    double targetWidth = val + (currentWidth > val ? 5 : -5);
                    widthSlider.setValue(Math.clamp(targetWidth, widthSlider.getMin(), widthSlider.getMax()));
                }
            }
            isChangingConfigDirectly = true;
            settingsChanged();
            isChangingConfigDirectly = false;
        });
    }

    @Override
    protected boolean isConfigValid() {
        return true;
    }

    @Override
    protected Bridge createGameInBackground() throws Exception {
        BridgeInit config = createConfig();
        if (config == null) throw new IllegalArgumentException("Neplatná konfigurace");
        return BridgeGameFactory.createGame(config);
    }

    @Override
    protected void onGameGenerated(Bridge resultGame) {
        if (resultGame == null) {
            resetButtons();
            return;
        }
        Platform.runLater(() -> {
            try {
                BridgeInit config = new BridgeInit();
                config.setPlayer(user);
                createSettings(config);
                List<GameSetting> settings = gameSettingService.prepareGameSettings(resultGame, config.getSettings());
                if (!config.getSettings().isEmpty()) {
                    userSettingsService.saveSettingsFromBuffer(config);
                }
                resultGame.setSettings(settings);
                mainController.closeCurrentAndReplace(TabType.BRIDGE, new LoadedGameWrapper(resultGame));
            } catch (IOException ex) {
                ex.printStackTrace();
                resetButtons();
            }
        });
    }

    @Override
    protected void buildSpecificLayout() {
        gridPane.getChildren().clear();
        int row = 0;

        addGridRow(row++, new Label(getFormatted("bridgeSettings.checkSameDim")), sameDimensionCheckBox);

        dimensionHBox.getChildren().clear();
        if (sameDimensionCheckBox.isSelected()) {
            dimLabel.setText(getFormatted("bridgeSettings.sameDimension"));
            dimensionHBox.getChildren().addAll(widthSlider, widthValueLabel);
        } else {
            dimLabel.setText(getFormatted("bridgeSettings.widthXheight"));
            dimensionHBox.getChildren().addAll(widthSlider, widthValueLabel, xLabel, heightSlider, heightValueLabel);
        }

        addGridRow(row++, difficultyLabel, difficultySegmentedButton);

        if (bridgeType == BridgeType.MULTIPLE) {
            addGridRow(row++, multipleCountLabel, multipleCountSegmentedButton);
        }

        addGridRow(row++, dimLabel, dimensionHBox);

        addGridRow(row++, hintLabel, hintCheckBox);

        int sizeMin = 8;
        int sizeMax = switch (bridgeType) {
            case CLASSIC -> 19;
            case MULTIPLE -> {
                var selectedToggle = multipleCountToggleGroup.getSelectedToggle();
                yield selectedToggle != null ? switch ((int) selectedToggle.getUserData()) {
                    case 3 -> 15;
                    case 4 -> 11;
                    case 5 -> 10;
                    default -> 10;
                } : 15;
            }
        };

        if (widthSlider != null && heightSlider != null) {
            widthSlider.setMin(sizeMin);
            widthSlider.setMax(sizeMax);
            heightSlider.setMin(sizeMin);
            heightSlider.setMax(sizeMax);

            double wVal = Math.clamp(widthSlider.getValue(), sizeMin, sizeMax);
            double hVal = Math.clamp(heightSlider.getValue(), sizeMin, sizeMax);

            if (sameDimensionCheckBox.isSelected()) {
                widthSlider.setValue(wVal);
                heightSlider.setValue(wVal);

            } else {
                if (Math.abs(hVal - wVal) > 5) {
                    hVal = wVal + (hVal > wVal ? 5 : -5);
                }
                widthSlider.setValue(wVal);
                heightSlider.setValue(hVal);
            }
        }

        addGridRow(row++, timerLabel, timerCheckBox);

        HBox seedActionsBox = new HBox(10, seedValueLabel, copySeedButton);
        seedActionsBox.setMaxWidth(Double.MAX_VALUE);
        seedActionsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        if (seedLabel != null) {
            javafx.scene.layout.GridPane.setValignment(seedLabel, javafx.geometry.VPos.CENTER);
            addGridRow(row++, seedLabel, seedActionsBox);
        }

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
    }

    private void createSettings(BridgeInit config) {
        config.addSetting(BridgeSettings.HINTS_ON, hintCheckBox.isSelected());
    }

    @Override
    public BridgeInit createConfig() {
        try {
            BridgeInit config = new BridgeInit(user).setBridgeType(bridgeType);
            config.addSetting(BridgeSettings.SAME_DIMENSION, sameDimensionCheckBox.isSelected());
            createSettings(config);

            Toggle diffToggle = difficultyToggleGroup.getSelectedToggle();
            Difficulty diff = diffToggle != null ? (Difficulty) diffToggle.getUserData() : Difficulty.MEDIUM;
            config.setDifficulty(diff);

            int width = (int) widthSlider.getValue();
            config.setWidth(width);

            if (sameDimensionCheckBox.isSelected()) {
                config.setHeight(width);
            } else {
                config.setHeight((int) heightSlider.getValue());
            }

            config.setMultipleCount(bridgeType == BridgeType.MULTIPLE ? (int) multipleCountToggleGroup.getSelectedToggle().getUserData() : 2);

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
