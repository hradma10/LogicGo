package cz.logicgo.ui.controllers.exportControllers.exportTabs;

import cz.logicgo.core.misc.TranslationLoader;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.gameTypes.bridge.BridgeType;
import cz.logicgo.ui.controllers.exportControllers.ExportMultipleGamesController;
import cz.logicgo.core.gameClasses.export.gameTypes.BridgeTypes;
import cz.logicgo.core.gameClasses.export.gameTypes.GameMode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import org.controlsfx.control.SegmentedButton;

import static cz.logicgo.core.misc.Messages.getFormatted;
import static cz.logicgo.ui.controllers.exportControllers.ExportMultipleGamesController.MAX_GENERATED;
import static cz.logicgo.ui.controllers.helpers.ElementsSetterHelpers.*;


public class BridgeTabController {
    @FXML
    public TextField countGames;
    @FXML
    public GridPane bridgeGridPane;
    public ChoiceBox<BridgeType> bridgeTypeChoiceBox;

    private SegmentedButton difficultySegmentedButton;
    private ToggleGroup difficultyToggleGroup;
    private SegmentedButton multipleCountSegmentedButton;
    private ToggleGroup multipleCountToggleGroup;

    public Label bridgeTypeLabel;
    public Label bridgeDifficultyLabel;
    public Label bridgeCountGamesLabel;
    public Label multipleCountLabel;
    public Label sizeLabel;
    private Label xLabel;
    Canvas canvas;

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
    @FXML
    public Button addButton;
    private HBox dimensionHBox;
    public ExportMultipleGamesController controller;

    public GameMode getCurrentConfig() {
        BridgeType type = bridgeTypeChoiceBox.getValue();
        if (type == null) return null;
        int width = (int) widthSlider.getValue();
        int height = sameDimensionCheckBox.isSelected() ? width : (int) heightSlider.getValue();
        int mult = 2;
        if (type == BridgeType.MULTIPLE && multipleCountToggleGroup != null && multipleCountToggleGroup.getSelectedToggle() != null) {
            mult = (int) multipleCountToggleGroup.getSelectedToggle().getUserData();
        }
        return new BridgeTypes(Difficulty.EASY, 1, type, width, height, mult);
    }

    private void setUpLabels() {
        bridgeDifficultyLabel = new Label(getFormatted("gameSettings.difficulty"));
        bridgeCountGamesLabel = new Label(getFormatted("export.tab.countGames"));
        bridgeTypeLabel = new Label(getFormatted("export.tab.type") != null ? getFormatted("export.tab.type") : "Typ:");
        sizeLabel = new Label();
        sizeLabel.setMinSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
        xLabel = new Label("x");
        multipleCountLabel = new Label(getFormatted("bridgeSettings.multipleCount"));
        if (addButton != null) {
            String btnText = getFormatted("export.tab.addToExport") != null ? getFormatted("export.tab.addToExport") : "Přidat do exportu";
            addButton.setText(btnText);
        }
    }

    public void initialize(Canvas canvas, ExportMultipleGamesController controller) {
        this.canvas = canvas;
        this.controller = controller;
        setUpLabels();
        setUpCountGamesTextField(countGames, MAX_GENERATED);

        sameDimensionCheckBox.setText("");
        sameDimensionCheckBox.setSelected(true);

        difficultySegmentedButton = createDifficultySegmentedButton();
        difficultyToggleGroup = difficultySegmentedButton.getToggleGroup();
        selectSegmentedButtonByUserData(difficultySegmentedButton, Difficulty.MEDIUM);

        multipleCountToggleGroup = new ToggleGroup();
        multipleCountSegmentedButton = createMultipleCountSegmentedButton(multipleCountToggleGroup);
        multipleCountToggleGroup = multipleCountSegmentedButton.getToggleGroup();
        selectSegmentedButtonByUserData(multipleCountSegmentedButton, 3);

        ObservableList<BridgeType> variantList = FXCollections.observableArrayList(BridgeType.values());
        bridgeTypeChoiceBox.setItems(variantList);
        bridgeTypeChoiceBox.getSelectionModel().select(BridgeType.CLASSIC);
        bridgeTypeChoiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(BridgeType bridgeType) {
                return bridgeType != null ? bridgeType.getTranslation() : getFormatted("choiceNoValue");
            }
            @Override
            public BridgeType fromString(String string) {
                return TranslationLoader.getInstanceFromTranslation(string);
            }
        });

        dimensionHBox = new HBox(10);
        widthValueLabel.setMinSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
        heightValueLabel.setMinSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);

        sameDimensionCheckBox.selectedProperty().addListener((obs, oldV, newV) -> {
            if (newV) heightSlider.setValue(widthSlider.getValue());
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
            if (controller != null) controller.updatePreview();
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
            if (controller != null) controller.updatePreview();
        });

        sameDimensionCheckBox.selectedProperty().addListener((obs, oldV, newV) -> buildSpecificLayout());
        bridgeTypeChoiceBox.valueProperty().addListener((obs, oldV, newV) -> buildSpecificLayout());
        multipleCountToggleGroup.selectedToggleProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) buildSpecificLayout();
        });

        buildSpecificLayout();
    }

    private void buildSpecificLayout() {
        bridgeGridPane.getChildren().clear();
        int row = 0;

        addGridRow(row++, bridgeTypeLabel, bridgeTypeChoiceBox);
        addGridRow(row++, new Label(getFormatted("bridgeSettings.checkSameDim")), sameDimensionCheckBox);

        dimensionHBox.getChildren().clear();
        if (sameDimensionCheckBox.isSelected()) {
            sizeLabel.setText(getFormatted("bridgeSettings.sameDimension"));
            dimensionHBox.getChildren().addAll(widthSlider, widthValueLabel);
        } else {
            sizeLabel.setText(getFormatted("bridgeSettings.widthXheight"));
            dimensionHBox.getChildren().addAll(widthSlider, widthValueLabel, xLabel, heightSlider, heightValueLabel);
        }

        BridgeType bridgeType = bridgeTypeChoiceBox.getValue();
        if (bridgeType == BridgeType.MULTIPLE) {
            addGridRow(row++, multipleCountLabel, multipleCountSegmentedButton);
        }

        addGridRow(row++, sizeLabel, dimensionHBox);
        addGridRow(row++, bridgeDifficultyLabel, difficultySegmentedButton);
        addGridRow(row++, bridgeCountGamesLabel, countGames);

        int sizeMin = 8;
        int sizeMax = switch (bridgeType) {
            case CLASSIC -> 19;
            case MULTIPLE -> {
                var selectedToggle = multipleCountToggleGroup.getSelectedToggle();
                yield selectedToggle != null ? switch ((int) selectedToggle.getUserData()) {
                    case 3 -> 15;
                    case 4 -> 11;
                    default -> 10;
                } : 15;
            }
        };

        widthSlider.setMin(sizeMin);
        widthSlider.setMax(sizeMax);
        heightSlider.setMin(sizeMin);
        heightSlider.setMax(sizeMax);

        if (addButton != null) {
            bridgeGridPane.add(addButton, 1, row);
        }

        if (controller != null) controller.updatePreview();
    }

    private void addGridRow(int row, Node col1, Node col2) {
        if (col1 != null) {
            bridgeGridPane.add(col1, 0, row);
            GridPane.setHalignment(col1, HPos.RIGHT);
        }
        if (col2 != null) {
            bridgeGridPane.add(col2, 1, row);
            GridPane.setHalignment(col2, HPos.LEFT);
        }
    }

    @FXML
    private void addToList() {
        var type = bridgeTypeChoiceBox.getValue();
        int width = (int) widthSlider.getValue();
        int height = sameDimensionCheckBox.isSelected() ? width : (int) heightSlider.getValue();
        int count = Integer.parseInt(countGames.getText());

        Toggle diffToggle = difficultyToggleGroup.getSelectedToggle();
        Difficulty difficulty = diffToggle != null ? (Difficulty) diffToggle.getUserData() : Difficulty.MEDIUM;

        int multipleCount = 2;
        if (type == BridgeType.MULTIPLE) {
            Toggle multToggle = multipleCountToggleGroup.getSelectedToggle();
            multipleCount = multToggle != null ? (int) multToggle.getUserData() : 3;
        }

        BridgeTypes bridgeData = new BridgeTypes(difficulty, count, type, width, height, multipleCount);

        controller.addToList(new ExportMultipleGamesController.ExportItem(
                getFormatted("export.detail.bridge", type.getTranslation(), width, height, difficulty.getTranslation()),
                bridgeData
        ));
    }
}
