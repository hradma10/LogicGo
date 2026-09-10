package cz.logicgo.ui.controllers.exportControllers.exportTabs;

import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.misc.TranslationLoader;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.gameTypes.shikaku.ShikakuType;
import cz.logicgo.ui.controllers.exportControllers.ExportMultipleGamesController;
import cz.logicgo.core.gameClasses.export.gameTypes.GameMode;
import cz.logicgo.core.gameClasses.export.gameTypes.ShikakuTypes;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.StringConverter;
import org.controlsfx.control.SegmentedButton;

import static cz.logicgo.core.misc.Messages.getFormatted;
import static cz.logicgo.ui.controllers.exportControllers.ExportMultipleGamesController.MAX_GENERATED;
import static cz.logicgo.ui.controllers.helpers.ElementsSetterHelpers.*;


public class ShikakuTabController {
    @FXML
    public TextField countGames;
    @FXML
    public GridPane shikakuGridPane;
    public ChoiceBox<ShikakuType> shikakuTypeChoiceBox;

    private SegmentedButton difficultySegmentedButton;
    private ToggleGroup difficultyToggleGroup;

    public Label shikakuTypeLabel;
    public Label shikakuDifficultyLabel;
    public Label shikakuCountGamesLabel;
    private Label sizeLabel;
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
    User user;

    public GameMode getCurrentConfig() {
        ShikakuType type = shikakuTypeChoiceBox.getValue();
        if (type == null) return null;
        int width = (int) widthSlider.getValue();
        int height = sameDimensionCheckBox.isSelected() ? width : (int) heightSlider.getValue();
        return new ShikakuTypes(Difficulty.EASY, 1, type, width, height);
    }

    private void setUpLabels() {
        shikakuTypeLabel = new Label(getFormatted("export.tab.type") != null ? getFormatted("export.tab.type") : "Typ:");
        shikakuDifficultyLabel = new Label(getFormatted("gameSettings.difficulty"));
        shikakuCountGamesLabel = new Label(getFormatted("export.tab.countGames"));
        sizeLabel = new Label();
        sizeLabel.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        xLabel = new Label("x");
        if (addButton != null) {
            String btnText = getFormatted("export.tab.addToExport") != null ? getFormatted("export.tab.addToExport") : "Přidat do exportu";
            addButton.setText(btnText);
        }
    }

    public void initialize(Canvas canvas, ExportMultipleGamesController controller, User user) {
        this.canvas = canvas;
        this.controller = controller;
        this.user = user;
        setUpLabels();
        setUpCountGamesTextField(countGames, MAX_GENERATED);
        sameDimensionCheckBox.setText("");
        sameDimensionCheckBox.setSelected(true);

        difficultyToggleGroup = new ToggleGroup();
        difficultySegmentedButton = createDifficultySegmentedButton();
        difficultyToggleGroup = difficultySegmentedButton.getToggleGroup();
        selectSegmentedButtonByUserData(difficultySegmentedButton, Difficulty.MEDIUM);

        ObservableList<ShikakuType> variantList = FXCollections.observableArrayList(ShikakuType.values());
        shikakuTypeChoiceBox.setItems(variantList);
        shikakuTypeChoiceBox.getSelectionModel().select(ShikakuType.CLASSIC);
        shikakuTypeChoiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(ShikakuType shikakuType) {
                return shikakuType != null ? shikakuType.getTranslation() : getFormatted("choiceNoValue");
            }
            @Override
            public ShikakuType fromString(String string) {
                return TranslationLoader.getInstanceFromTranslation(string);
            }
        });

        dimensionHBox = new HBox(10);

        widthValueLabel.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        heightValueLabel.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        sameDimensionCheckBox.selectedProperty().addListener((obs, oldV, newV) -> {
            if (newV) heightSlider.setValue(widthSlider.getValue());
            buildSpecificLayout();
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

        shikakuTypeChoiceBox.valueProperty().addListener((obs, oldV, newV) -> buildSpecificLayout());

        buildSpecificLayout();
    }

    private void buildSpecificLayout() {
        shikakuGridPane.getChildren().clear();
        int row = 0;

        addGridRow(row++, shikakuTypeLabel, shikakuTypeChoiceBox);
        addGridRow(row++, new Label(getFormatted("bridgeSettings.checkSameDim")), sameDimensionCheckBox);

        dimensionHBox.getChildren().clear();
        if (sameDimensionCheckBox.isSelected()) {
            sizeLabel.setText(getFormatted("bridgeSettings.sameDimension"));
            dimensionHBox.getChildren().addAll(widthSlider, widthValueLabel);
        } else {
            sizeLabel.setText(getFormatted("bridgeSettings.widthXheight"));
            dimensionHBox.getChildren().addAll(widthSlider, widthValueLabel, xLabel, heightSlider, heightValueLabel);
        }

        addGridRow(row++, sizeLabel, dimensionHBox);
        addGridRow(row++, shikakuDifficultyLabel, difficultySegmentedButton);
        addGridRow(row++, shikakuCountGamesLabel, countGames);

        int sizeMin = 8;
        ShikakuType shikakuType = shikakuTypeChoiceBox.getValue();
        int sizeMax = switch (shikakuType != null ? shikakuType : ShikakuType.CLASSIC) {
            case CLASSIC -> 25;
            case OFF_BY_ONE -> 15;
            default -> 15;
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
                heightSlider.setValue(Math.clamp(hVal, sizeMin, sizeMax));
            }
        }

        if (addButton != null) {
            shikakuGridPane.add(addButton, 1, row);
            GridPane.setMargin(addButton, new Insets(10, 0, 0, 0));
        }

        if (controller != null) controller.updatePreview();
    }

    private void addGridRow(int row, Node col1, Node col2) {
        if (col1 != null) {
            shikakuGridPane.add(col1, 0, row);
            GridPane.setHalignment(col1, HPos.RIGHT);
        }
        if (col2 != null) {
            shikakuGridPane.add(col2, 1, row);
            GridPane.setHalignment(col2, HPos.LEFT);
        }
    }

    @FXML
    private void addToList() {
        var type = shikakuTypeChoiceBox.getValue();
        int width = (int) widthSlider.getValue();
        int height = sameDimensionCheckBox.isSelected() ? width : (int) heightSlider.getValue();
        int count = Integer.parseInt(countGames.getText());

        Toggle diffToggle = difficultyToggleGroup.getSelectedToggle();
        Difficulty difficulty = diffToggle != null ? (Difficulty) diffToggle.getUserData() : Difficulty.MEDIUM;

        ShikakuTypes shikakuTypes = new ShikakuTypes(difficulty, count, type, width, height);

        controller.addToList(new ExportMultipleGamesController.ExportItem(
                getFormatted("export.detail.shikaku", type.getTranslation(), width, height, difficulty.getTranslation()),
                shikakuTypes
        ));
    }
}
