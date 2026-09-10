package cz.logicgo.ui.controllers.menu;

import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.entity.setting.GameSetting;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.factoryInit.shikaku.ShikakuInit;
import cz.logicgo.core.gameClasses.favorites.GameFavorite;
import cz.logicgo.core.gameClasses.favorites.ShikakuFavorite;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.TabType;
import cz.logicgo.core.misc.enums.gameTypes.shikaku.ShikakuType;
import cz.logicgo.core.misc.enums.settings.SettingKey;
import cz.logicgo.core.misc.enums.settings.ShikakuSettings;
import cz.logicgo.core.misc.enums.settings.SudokuSettings;
import cz.logicgo.engine.util.generate.RandomizationFactory;
import cz.logicgo.engine.util.generate.SeedCreator;
import cz.logicgo.persistence.services.GameSettingService;
import cz.logicgo.persistence.services.UserSettingsService;
import cz.logicgo.ui.controllers.screenControllers.MainScreenController;
import cz.logicgo.ui.factories.ShikakuGameFactory;
import cz.logicgo.ui.misc.TabState;
import cz.logicgo.ui.misc.tabChoosingClasses.FavoriteGameWrapper;
import cz.logicgo.ui.misc.tabChoosingClasses.LoadedGameWrapper;
import cz.logicgo.ui.misc.tabChoosingClasses.TabChoiceWrapper;
import cz.logicgo.ui.renderers.ShikakuRenderer;
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
import static cz.logicgo.ui.controllers.helpers.ElementsSetterHelpers.createDifficultySegmentedButton;
import static cz.logicgo.ui.controllers.helpers.ElementsSetterHelpers.selectSegmentedButtonByUserData;


public class ShikakuSettingsMenu extends AbstractSettingsMenu<Shikaku> {

    @FXML
    Canvas canvas;
    ShikakuType shikakuType;

    private boolean custom;
    private boolean seeded;

    private SegmentedButton difficultySegmentedButton;
    private ToggleGroup difficultyToggleGroup;

    private CheckBox timerCheckBox;
    private CheckBox sameDimensionCheckBox;

    private Label sameDimLabel;
    private Label dimLabel;
    private final Label xLabel = new Label("x");
    private Label difficultyLabel;
    private Label timerLabel;
    private Label seedLabel;

    private HBox dimensionHBox;

    private Slider widthSlider;
    private Label widthValueLabel;
    private Slider heightSlider;
    private Label heightValueLabel;

    private Label seedValueLabel;
    private Button copySeedButton;

    private Label hintLabel;
    private CheckBox hintCheckBox;

    private final UserSettingsService userSettingsService = new UserSettingsService();
    private final GameSettingService gameSettingService = new GameSettingService();

    @Override
    protected GameFavorite createFavoriteFromCurrentSettings() {
        if (!isConfigValid()) return null;
        ShikakuInit config = createConfig();
        if (config == null) return null;

        ShikakuFavorite fav = new ShikakuFavorite();
        fav.setDifficulty(config.getDifficulty());
        fav.setWidth(config.getWidth());
        fav.setHeight(config.getHeight());
        fav.setShikakuType(config.getShikakuType());
        return fav;
    }

    @Override
    protected void showRealPreview(Shikaku game) {
        if (game == null || canvas == null) return;

        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.save();

        ShikakuRenderer.render(canvas, game);
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

        ShikakuInit init = (ShikakuInit) gameSettings.toGameInit();
        if (init == null) {
            throw new IllegalArgumentException("Unsupported or empty game settings wrapper: " + gameSettings);
        }

        this.shikakuType = init.getShikakuType();
        this.seeded = gameSettings.isFixed();
        this.custom = this.seeded || (gameSettings instanceof FavoriteGameWrapper);

        setUpGridColumns();

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

            widthSlider.setValue(init.getWidth());
            heightSlider.setValue(init.getHeight());
            sameDimensionCheckBox.setSelected(init.getWidth() == init.getHeight());

            if (this.seeded) {
                difficultySegmentedButton.setDisable(true);
                widthSlider.setDisable(true);
                heightSlider.setDisable(true);
                sameDimensionCheckBox.setDisable(true);
            }
        }

        buildSpecificLayout();
        this.isInitializing = false;
        settingsChanged();
    }

    private void initControls(Map<SettingKey, Object> settings) {
        sameDimLabel = new Label(getFormatted("bridgeSettings.checkSameDim"));

        sameDimensionCheckBox = new CheckBox();
        sameDimensionCheckBox.setSelected((Boolean) settings.getOrDefault(ShikakuSettings.SAME_DIMENSION, false));

        dimLabel = new Label();
        dimensionHBox = new HBox(10);

        widthSlider = new Slider(1, 25, 7);
        widthSlider.setBlockIncrement(1);
        widthSlider.setMajorTickUnit(1);
        widthSlider.setMinorTickCount(0);
        widthSlider.setSnapToTicks(true);
        widthValueLabel = new Label("7");

        heightSlider = new Slider(1, 25, 7);
        heightSlider.setBlockIncrement(1);
        heightSlider.setMajorTickUnit(1);
        heightSlider.setMinorTickCount(0);
        heightSlider.setSnapToTicks(true);
        heightValueLabel = new Label("7");

        difficultyLabel = new Label(getFormatted("gameSettings.difficulty"));
        difficultyToggleGroup = new ToggleGroup();
        difficultySegmentedButton = createDifficultySegmentedButton();
        difficultyToggleGroup = difficultySegmentedButton.getToggleGroup();
        selectSegmentedButtonByUserData(difficultySegmentedButton, Difficulty.MEDIUM);

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

        boolean sameDim = (Boolean) settings.getOrDefault(ShikakuSettings.SAME_DIMENSION, false);
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
                if (Math.abs(heightSlider.getValue() - val) > 5) {
                    heightSlider.setValue(val + (heightSlider.getValue() > val ? 5 : -5));
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
                if (Math.abs(widthSlider.getValue() - val) > 5) {
                    widthSlider.setValue(val + (widthSlider.getValue() > val ? 5 : -5));
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
    protected Shikaku createGameInBackground() throws Exception {
        ShikakuInit config = createConfig();
        if (config == null) throw new IllegalArgumentException("Neplatná konfigurace Shikaku");
        return ShikakuGameFactory.createGame(config);
    }

    @Override
    protected void onGameGenerated(Shikaku resultGame) {
        if (resultGame == null) {
            resetButtons();
            return;
        }
        Platform.runLater(() -> {
            try {
                ShikakuInit config = new ShikakuInit();
                config.setPlayer(user);
                createSettings(config);
                List<GameSetting> settings = gameSettingService.prepareGameSettings(resultGame, config.getSettings());
                if (!config.getSettings().isEmpty()) {
                    userSettingsService.saveSettingsFromBuffer(config);
                }
                resultGame.setSettings(settings);
                mainController.closeCurrentAndReplace(TabType.SHIKAKU, new LoadedGameWrapper(resultGame));
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
        addGridRow(row++, dimLabel, dimensionHBox);

        addGridRow(row++, hintLabel, hintCheckBox);

        int sizeMin = 8;
        int sizeMax = switch (shikakuType) {
            case CLASSIC -> 25;
            case OFF_BY_ONE -> 15;
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

    private void createSettings(ShikakuInit config) {
        config.addSetting(ShikakuSettings.HINTS_ON, hintCheckBox.isSelected());
    }

    public ShikakuInit createConfig() {
        try {
            ShikakuInit config = new ShikakuInit(user).setShikakuType(shikakuType);
            config.addSetting(ShikakuSettings.SAME_DIMENSION, sameDimensionCheckBox.isSelected());
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
