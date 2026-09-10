package cz.logicgo.ui.controllers.exportControllers.exportTabs;

import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeAlgorithm;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.ui.StartOfApp;
import cz.logicgo.ui.controllers.exportControllers.ExportMultipleGamesController;
import cz.logicgo.core.gameClasses.export.gameTypes.GameMode;
import cz.logicgo.core.gameClasses.export.gameTypes.MazeTypes;
import cz.logicgo.ui.controllers.gameControllers.choosers.ChosenMask;
import cz.logicgo.ui.controllers.helpers.MazeControlConfig;
import cz.logicgo.ui.controllers.viewer.MaskViewerController;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.SegmentedButton;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static cz.logicgo.core.misc.Messages.getFormatted;
import static cz.logicgo.ui.controllers.Openers.darkModeIconSet;
import static cz.logicgo.ui.controllers.exportControllers.ExportMultipleGamesController.MAX_GENERATED;
import static cz.logicgo.ui.controllers.helpers.ElementsSetterHelpers.*;


public class MazeTabController {
    @FXML
    public TextField countGames;
    @FXML
    public GridPane mazeGridPane;
    public ChoiceBox<MazeType> mazeTypeChoiceBox;

    private SegmentedButton shapeSegmentedButton;
    private ToggleGroup shapeToggleGroup;

    public ChoiceBox<MazeAlgorithm> mazeAlgorithmChoiceBox;

    private SegmentedButton difficultySegmentedButton;
    private ToggleGroup difficultyToggleGroup;
    private SegmentedButton floorModeSegmentedButton;
    private ToggleGroup floorModeToggleGroup;

    @FXML
    public Slider widthSlider;
    @FXML
    public Label widthValueLabel;
    @FXML
    public Slider heightSlider;
    @FXML
    public Label heightValueLabel;
    @FXML
    public CheckBox sameDimensionCheckBox;

    private Label typeLabel;
    private Label shapeLabel;
    private Label algoLabel;
    private Label difficultyLabel;
    private Label countLabel;
    private Label sizeLabel;
    private final Label xLabel = new Label("x");
    @FXML
    private Button btnMask;

    public Label singleCountLabel;
    public TextField singleCountField;

    private VBox multiLevelWrapperBox;
    public CheckBox multiLevelSameCheckBox;
    public ChoiceBox<MazeType> multiLevelTypeChoiceBox;
    public TextField floorCountTextBox;

    private Label multiLevelTypeLabel;
    private Label floorCountLabel;
    private Label classicCountLabel;
    private Label portalCountLabel;
    private Label wallsCountLabel;
    private Label patternCountLabel;
    private Label orderedCheckCountLabel;
    private Label wrapAroundCountLabel;
    private Label exactStepsCountLabel;

    public TextField classicCountField;
    public TextField portalCountField;
    public TextField wallsCountField;
    public TextField patternCountField;
    public TextField orderedCheckCountField;
    public TextField wrapAroundCountField;
    public TextField exactStepsCountField;
    @FXML
    public Button addButton;
    User user;
    Canvas canvas;
    ExportMultipleGamesController controller;
    private HBox dimensionHBox;

    private final ObjectProperty<boolean[][]> customMaskProperty = new SimpleObjectProperty<>();

    public GameMode getCurrentConfig() {
        MazeType type = mazeTypeChoiceBox.getValue();
        if (type == null) return null;
        MazeShape shape = MazeShape.RECTANGULAR;
        if (shapeToggleGroup != null && shapeToggleGroup.getSelectedToggle() != null) {
            shape = (MazeShape) shapeToggleGroup.getSelectedToggle().getUserData();
        }
        MazeAlgorithm algo = mazeAlgorithmChoiceBox.getValue();
        if (algo == null) algo = MazeAlgorithm.RECURSIVE_BACKTRACKER;
        int width = (int) widthSlider.getValue();
        int height = sameDimensionCheckBox.isSelected() ? width : (int) heightSlider.getValue();

        Map<MazeType, Integer> typeCounts = new HashMap<>();
        if (type == MazeType.MULTI_LEVEL) {
            typeCounts.put(MazeType.CLASSIC, 2);
        } else {
            typeCounts.put(type, 1);
        }
        return new MazeTypes(Difficulty.EASY, 1, type, shape, algo, width, height, customMaskProperty.get(), typeCounts);
    }

    private void setUpLabels() {
        typeLabel = new Label(getFormatted("export.tab.type") != null ? getFormatted("export.tab.type") : "Typ:");
        shapeLabel = new Label(getFormatted("mazeSettings.mazeShape") != null ? getFormatted("mazeSettings.mazeShape") : "Tvar:");
        algoLabel = new Label(getFormatted("mazeSettings.algorithm") != null ? getFormatted("mazeSettings.algorithm") : "Algoritmus:");
        difficultyLabel = new Label(getFormatted("gameSettings.difficulty"));
        countLabel = new Label(getFormatted("export.tab.countGames"));
        sizeLabel = new Label();
        sizeLabel.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        if (addButton != null) {
            String btnText = getFormatted("export.tab.addToExport") != null ? getFormatted("export.tab.addToExport") : "Přidat do exportu";
            addButton.setText(btnText);
        }
        String elementText = switch (mazeTypeChoiceBox.getValue() != null ? mazeTypeChoiceBox.getValue() : MazeType.CLASSIC) {
            case WALLS, PORTAL -> getFormatted("mazeSettings.regionCount");
            case PATTERN -> getFormatted("mazeSettings.patternCount");
            case CHECKPOINT -> getFormatted("mazeSettings.checkpointCount");
            case WRAP_AROUND -> getFormatted("mazeSettings.wrapCount");
            default -> "";
        };
        singleCountLabel.setText(elementText);
        btnClearMask = new Button(getFormatted("mazeSettings.cancelMask") != null ? getFormatted("mazeSettings.cancelMask") : "Zrušit masku");
        btnClearMask.setDisable(true);
        multiLevelSameCheckBox.setText(getFormatted("mazeSettings.multiLevelSame"));
        multiLevelTypeLabel = new Label(getFormatted("mazeSettings.multiLevelType"));
        floorCountLabel = new Label(getFormatted("mazeSettings.floorCount"));
        classicCountLabel = new Label(MazeType.CLASSIC.getTranslation());
        portalCountLabel = new Label(MazeType.PORTAL.getTranslation());
        wallsCountLabel = new Label(MazeType.WALLS.getTranslation());
        patternCountLabel = new Label(MazeType.PATTERN.getTranslation());
        orderedCheckCountLabel = new Label(MazeType.CHECKPOINT.getTranslation());
        wrapAroundCountLabel = new Label(MazeType.WRAP_AROUND.getTranslation());
        exactStepsCountLabel = new Label(MazeType.EXACT_STEPS.getTranslation());
    }

    public void initialize(Canvas canvas, User user, ExportMultipleGamesController controller) {
        this.user = user;
        this.canvas = canvas;
        this.controller = controller;

        setUpLabels();
        setUpCountGamesTextField(countGames, MAX_GENERATED);

        sameDimensionCheckBox.setText("");
        sameDimensionCheckBox.setSelected(true);

        widthValueLabel.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        heightValueLabel.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        shapeToggleGroup = new ToggleGroup();
        shapeSegmentedButton = createMazeShapeSegmentedButton(shapeToggleGroup);
        shapeToggleGroup = shapeSegmentedButton.getToggleGroup();
        selectToggleByUserData(shapeToggleGroup, MazeShape.RECTANGULAR);

        difficultyToggleGroup = new ToggleGroup();
        difficultySegmentedButton = createDifficultySegmentedButton();
        difficultyToggleGroup = difficultySegmentedButton.getToggleGroup();
        selectSegmentedButtonByUserData(difficultySegmentedButton, Difficulty.MEDIUM);

        btnClearMask.setOnAction(_ -> {
            this.customMaskProperty.set(null);
            btnClearMask.setDisable(true);
            widthSlider.setDisable(false);
            heightSlider.setDisable(false);
            sameDimensionCheckBox.setDisable(false);
            buildSpecificLayout();
        });

        ToggleButton btnRandom = new ToggleButton(getFormatted("mazeSettings.randomMode"));
        btnRandom.setUserData(true);
        ToggleButton btnCustom = new ToggleButton(getFormatted("mazeSettings.customCountsMode"));
        btnCustom.setUserData(false);
        floorModeSegmentedButton = new SegmentedButton(btnRandom, btnCustom);
        floorModeToggleGroup = floorModeSegmentedButton.getToggleGroup();
        setUpSegmentedButtonHandling(floorModeSegmentedButton, btnRandom);

        var config = new MazeControlConfig()
                .setCanvas(canvas).setAlgorithmChoiceBox(mazeAlgorithmChoiceBox)
                .setShapeChoiceBox(null).setTypeChoiceBox(mazeTypeChoiceBox);
        setupMazeControls(config);

        setItemsMazeTypeChoiceBox(canvas, multiLevelTypeChoiceBox);
        maskButtonsBox = new HBox(10, btnMask, btnClearMask);
        dimensionHBox = new HBox(10);
        multiLevelWrapperBox = new VBox(15);
        multiLevelWrapperBox.setStyle("-fx-border-color: #3f4246; -fx-border-width: 1px; -fx-padding: 15; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-background-color: -color-bg-input;");

        setupListeners();
        buildSpecificLayout();
    }

    private void setupListeners() {
        mazeTypeChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            setUpLabels();
            buildSpecificLayout();
        });
        shapeToggleGroup.selectedToggleProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                MazeShape selectedShape = (MazeShape) newV.getUserData();
                if (selectedShape == MazeShape.HEXAGONAL) {
                    customMaskProperty.set(null);
                    widthSlider.setDisable(false);
                    heightSlider.setDisable(false);
                    sameDimensionCheckBox.setDisable(false);
                }
                buildSpecificLayout();
            }
        });

        floorModeToggleGroup.selectedToggleProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) buildSpecificLayout();
        });

        multiLevelSameCheckBox.selectedProperty().addListener((obs, oldV, newV) -> {
            buildSpecificLayout();
        });

        customMaskProperty.addListener((obs, oldMask, newMask) -> {
            btnClearMask.setDisable(newMask == null);
            buildSpecificLayout();
            if (controller != null) controller.updatePreview();
        });

        multiLevelTypeChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) buildSpecificLayout();
        });

        widthSlider.valueProperty().addListener((obs, oldV, newV) -> {
            int val = newV.intValue();
            widthValueLabel.setText(String.valueOf(val));
            if (sameDimensionCheckBox.isSelected()) {
                heightSlider.setValue(val);
            }
            if (controller != null) controller.updatePreview();
        });

        heightSlider.valueProperty().addListener((obs, oldV, newV) -> {
            heightValueLabel.setText(String.valueOf(newV.intValue()));
            if (controller != null) controller.updatePreview();
        });

        sameDimensionCheckBox.selectedProperty().addListener((obs, oldV, newV) -> {
            if (newV) heightSlider.setValue(widthSlider.getValue());
            buildSpecificLayout();
        });

        mazeAlgorithmChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (controller != null) controller.updatePreview();
        });
    }

    private Button btnClearMask;
    private HBox maskButtonsBox;

    private void buildSpecificLayout() {
        mazeGridPane.getChildren().clear();
        int row = 0;

        addGridRow(row++, new Label(getFormatted("bridgeSettings.checkSameDim")), sameDimensionCheckBox);

        dimensionHBox.getChildren().clear();
        if (sameDimensionCheckBox.isSelected()) {
            sizeLabel.setText(getFormatted("mazeSettings.sameDimension"));
            dimensionHBox.getChildren().addAll(widthSlider, widthValueLabel);
        } else {
            sizeLabel.setText(getFormatted("mazeSettings.widthXheight"));
            dimensionHBox.getChildren().addAll(widthSlider, widthValueLabel, xLabel, heightSlider, heightValueLabel);
        }
        addGridRow(row++, sizeLabel, dimensionHBox);

        addGridRow(row++, typeLabel, mazeTypeChoiceBox);
        addGridRow(row++, shapeLabel, shapeSegmentedButton);
        addGridRow(row++, algoLabel, mazeAlgorithmChoiceBox);

        MazeType mazeType = mazeTypeChoiceBox.getValue();
        MazeType activeTypeForAlgorithms = mazeType;

        if (Objects.requireNonNull(mazeType) == MazeType.MULTI_LEVEL) {
            countGames.setText("1");
            countGames.setDisable(true);

            multiLevelWrapperBox.getChildren().clear();
            GridPane innerGrid = new GridPane();
            innerGrid.setHgap(15);
            innerGrid.setVgap(10);
            int innerRow = 0;

            innerGrid.add(new Label(getFormatted("mazeSettings.floorMode")), 0, innerRow);
            innerGrid.add(floorModeSegmentedButton, 1, innerRow++);

            Toggle modeToggle = floorModeToggleGroup.getSelectedToggle();
            boolean isRandomMode = modeToggle == null || (boolean) modeToggle.getUserData();

            if (isRandomMode) {
                innerGrid.add(multiLevelSameCheckBox, 1, innerRow++);
                if (multiLevelSameCheckBox.isSelected()) {
                    innerGrid.add(multiLevelTypeLabel, 0, innerRow);
                    innerGrid.add(multiLevelTypeChoiceBox, 1, innerRow++);
                    if (multiLevelTypeChoiceBox.getValue() != null) {
                        activeTypeForAlgorithms = multiLevelTypeChoiceBox.getValue();
                    }
                }
                innerGrid.add(floorCountLabel, 0, innerRow);
                innerGrid.add(floorCountTextBox, 1, innerRow++);
            } else {
                innerRow = buildMultiLevelCustomMode(innerGrid, innerRow);
            }

            multiLevelWrapperBox.getChildren().add(innerGrid);
            mazeGridPane.add(multiLevelWrapperBox, 0, row++, 2, 1);
        }

        boolean hasMask = customMaskProperty.get() != null;
        if (hasMask) {
            activeTypeForAlgorithms = MazeType.MULTI_LEVEL;
        }

        Toggle shapeToggle = shapeToggleGroup.getSelectedToggle();
        MazeShape selectedShape = shapeToggle != null ? (MazeShape) shapeToggle.getUserData() : MazeShape.RECTANGULAR;

        updateAlgorithmItems(selectedShape, activeTypeForAlgorithms, hasMask, mazeAlgorithmChoiceBox);

        if (selectedShape != MazeShape.HEXAGONAL) {
            addGridRow(row++, new Label(getFormatted("mazeSettings.mazeMask")), maskButtonsBox);
        }

        addGridRow(row++, difficultyLabel, difficultySegmentedButton);
        addGridRow(row++, countLabel, countGames);

        if (addButton != null) {
            mazeGridPane.add(addButton, 1, row);
            GridPane.setMargin(addButton, new Insets(10, 0, 0, 0));
        }

        if (controller != null) controller.updatePreview();
    }

    private int buildMultiLevelCustomMode(GridPane innerGrid, int innerRow) {
        if (classicCountLabel != null && classicCountField != null) innerGrid.add(classicCountLabel, 0, innerRow);
        if (classicCountField != null) innerGrid.add(classicCountField, 1, innerRow++);
        if (portalCountLabel != null && portalCountField != null) innerGrid.add(portalCountLabel, 0, innerRow);
        if (portalCountField != null) innerGrid.add(portalCountField, 1, innerRow++);
        if (wallsCountLabel != null && wallsCountField != null) innerGrid.add(wallsCountLabel, 0, innerRow);
        if (wallsCountField != null) innerGrid.add(wallsCountField, 1, innerRow++);
        if (patternCountLabel != null && patternCountField != null) innerGrid.add(patternCountLabel, 0, innerRow);
        if (patternCountField != null) innerGrid.add(patternCountField, 1, innerRow++);
        if (orderedCheckCountLabel != null && orderedCheckCountField != null) innerGrid.add(orderedCheckCountLabel, 0, innerRow);
        if (orderedCheckCountField != null) innerGrid.add(orderedCheckCountField, 1, innerRow++);
        if (wrapAroundCountLabel != null && wrapAroundCountField != null) innerGrid.add(wrapAroundCountLabel, 0, innerRow);
        if (wrapAroundCountField != null) innerGrid.add(wrapAroundCountField, 1, innerRow++);
        if (exactStepsCountLabel != null && exactStepsCountField != null) innerGrid.add(exactStepsCountLabel, 0, innerRow);
        if (exactStepsCountField != null) innerGrid.add(exactStepsCountField, 1, innerRow++);
        return innerRow;
    }

    private void addGridRow(int row, Node col1, Node col2) {
        if (col1 != null) {
            mazeGridPane.add(col1, 0, row);
            GridPane.setHalignment(col1, HPos.RIGHT);
        }
        if (col2 != null) {
            mazeGridPane.add(col2, 1, row);
            GridPane.setHalignment(col2, HPos.LEFT);
        }
    }

    @FXML
    public void openMaskWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader(StartOfApp.class.getResource("/cz/logicgo/ui/windows/gameChoice/viewer.fxml"));
        MaskViewerController controllerM = new MaskViewerController();
        loader.setController(controllerM);

        Parent content = loader.load();
        content.getStyleClass().add("root");
        Stage stage = new Stage();
        ChosenMask chM = new ChosenMask();

        controllerM.initialize(user, chM, TypeGame.MAZE);
        controllerM.setStage(stage);

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
            customMaskProperty.set(chM.getMask());
            int maskHeight = chM.getMask().length;
            int maskWidth = chM.getMask()[0].length;

            widthSlider.setValue(maskWidth);
            heightSlider.setValue(maskHeight);
            widthSlider.setDisable(true);
            heightSlider.setDisable(true);
            sameDimensionCheckBox.setSelected(maskWidth == maskHeight);
            sameDimensionCheckBox.setDisable(true);

            btnClearMask.setDisable(false);
            buildSpecificLayout();
        }
    }

    @FXML
    private void addToList() {
        MazeType mazeType = mazeTypeChoiceBox.getValue();
        int totalFloors = 0;

        Toggle modeToggle = floorModeToggleGroup.getSelectedToggle();
        boolean isRandomMode = modeToggle == null || (boolean) modeToggle.getUserData();

        Map<MazeType, Integer> typeCounts = new HashMap<>();

        if (mazeType == MazeType.MULTI_LEVEL) {
            if (isRandomMode) {
                totalFloors = parseOrDefault(floorCountTextBox, 3);
            } else {
                totalFloors += parseOrDefault(classicCountField, 0);
                totalFloors += parseOrDefault(portalCountField, 0);
                totalFloors += parseOrDefault(wallsCountField, 0);
                totalFloors += parseOrDefault(patternCountField, 0);
                totalFloors += parseOrDefault(orderedCheckCountField, 0);
                totalFloors += parseOrDefault(wrapAroundCountField, 0);
                totalFloors += parseOrDefault(exactStepsCountField, 0);
            }

            if (totalFloors < 2) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(getFormatted("validation.errorTitle") != null ? getFormatted("validation.errorTitle") : "Chyba nastavení");
                alert.setHeaderText(null);
                alert.setContentText(getFormatted("mazeSettings.multiLevelFloorError") != null ? getFormatted("mazeSettings.multiLevelFloorError") : "Víceúrovňové bludiště musí mít minimálně 2 patra.");
                alert.showAndWait();
                return;
            }

            if (isRandomMode) {
                if (multiLevelSameCheckBox.isSelected()) {
                    MazeType specType = multiLevelTypeChoiceBox.getValue();
                    typeCounts.put(specType != null ? specType : MazeType.MULTI_LEVEL, totalFloors);
                } else {
                    typeCounts.put(MazeType.MULTI_LEVEL, totalFloors);
                }
            } else {
                typeCounts.put(MazeType.CLASSIC, parseOrDefault(classicCountField, 0));
                typeCounts.put(MazeType.PORTAL, parseOrDefault(portalCountField, 0));
                typeCounts.put(MazeType.WALLS, parseOrDefault(wallsCountField, 0));
                typeCounts.put(MazeType.PATTERN, parseOrDefault(patternCountField, 0));
                typeCounts.put(MazeType.CHECKPOINT, parseOrDefault(orderedCheckCountField, 0));
                typeCounts.put(MazeType.WRAP_AROUND, parseOrDefault(wrapAroundCountField, 0));
                typeCounts.put(MazeType.EXACT_STEPS, parseOrDefault(exactStepsCountField, 0));
            }
        } else if (mazeType != MazeType.CLASSIC) {
            typeCounts.put(mazeType, parseOrDefault(singleCountField, 1));
        }

        boolean[][] mask = customMaskProperty.get();
        Toggle shapeToggle = shapeToggleGroup.getSelectedToggle();
        MazeShape mazeShape = shapeToggle != null ? (MazeShape) shapeToggle.getUserData() : MazeShape.RECTANGULAR;
        MazeAlgorithm mazeAlgorithm = mazeAlgorithmChoiceBox.getValue();
        int width = (int) widthSlider.getValue();
        int height = sameDimensionCheckBox.isSelected() ? width : (int) heightSlider.getValue();
        int count = (mazeType == MazeType.MULTI_LEVEL) ? totalFloors : Integer.parseInt(countGames.getText());

        Toggle diffToggle = difficultyToggleGroup.getSelectedToggle();
        Difficulty difficulty = diffToggle != null ? (Difficulty) diffToggle.getUserData() : Difficulty.MEDIUM;

        MazeTypes mazeData = new MazeTypes(difficulty, count, mazeType, mazeShape, mazeAlgorithm, width, height, mask, typeCounts);

        String shapeStr = mazeShape != null ? mazeShape.getTranslation() : "";
        String typeStr = mazeType != null ? mazeType.getTranslation() : "";
        String mazeDesc = (mazeType == MazeType.MULTI_LEVEL)
                ? getFormatted("export.detail.maze.multi", typeStr, shapeStr, width, height, totalFloors, difficulty.getTranslation())
                : getFormatted("export.detail.maze.single", typeStr, shapeStr, width, height, difficulty.getTranslation());

        controller.addToList(new ExportMultipleGamesController.ExportItem(mazeDesc, mazeData));
    }

    private int parseOrDefault(TextField field, int defaultValue) {
        if (field == null || field.getText() == null || field.getText().isEmpty()) return defaultValue;
        try { return Integer.parseInt(field.getText()); }
        catch (NumberFormatException e) { return defaultValue; }
    }
}
