package cz.logicgo.ui.controllers.exportControllers.exportTabs;

import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuRegionLayout;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.engine.algorithms.sudoku.custom.CustomLayoutsLoader;
import cz.logicgo.engine.algorithms.sudoku.validators.MultiGridConfig;
import cz.logicgo.ui.StartOfApp;
import cz.logicgo.ui.controllers.exportControllers.ExportMultipleGamesController;
import cz.logicgo.core.gameClasses.export.gameTypes.GameMode;
import cz.logicgo.core.gameClasses.export.gameTypes.SudokuTypes;
import cz.logicgo.ui.controllers.gameControllers.choosers.ChosenPattern;
import cz.logicgo.ui.controllers.helpers.SudokuControlConfig;
import cz.logicgo.ui.controllers.viewer.SudokuCustomViewerController;
import cz.logicgo.core.misc.formatter.FavoriteFormatter;
import javafx.collections.FXCollections;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.SegmentedButton;

import java.io.IOException;
import java.util.List;

import static cz.logicgo.core.misc.Messages.getFormatted;
import static cz.logicgo.ui.controllers.Openers.darkModeIconSet;
import static cz.logicgo.ui.controllers.exportControllers.ExportMultipleGamesController.*;
import static cz.logicgo.ui.controllers.exportControllers.ExportMultipleGamesController.MAX_GENERATED;
import static cz.logicgo.ui.controllers.helpers.ElementsSetterHelpers.*;


public class SudokuTabController {
    @FXML
    public TextField countGames;
    @FXML
    public GridPane sudokuGridPane;
    public ChoiceBox<SudokuVariant> variantChoiceBox;
    public ChoiceBox<SudokuSize> sudokuSizeChoiceBox;
    public ChoiceBox<SudokuRegionLayout> regionLayoutChoiceBox;

    private SegmentedButton difficultySegmentedButton;
    private ToggleGroup difficultyToggleGroup;

    public Label sudokuVariantLabel;
    public Label sudokuSizeLabel;
    public Label sudokuRegionLayoutLabel;
    public Label sudokuDifficultyLabel;
    public Label sudokuCountLabel;
    @FXML
    public Button addButton;
    private Button openChoice;
    Canvas canvas;
    public ExportMultipleGamesController controller;

    private SudokuPatternLayout patternLayout;
    private User user;

    public void setUser(User user) {
        this.user = user;
    }

    private void setUpLabels() {
        sudokuVariantLabel = new Label(getFormatted("export.sudokuTab.variant"));
        sudokuDifficultyLabel = new Label(getFormatted("gameSettings.difficulty"));
        sudokuCountLabel = new Label(getFormatted("export.tab.countGames"));
        sudokuRegionLayoutLabel = new Label(getFormatted("export.sudokuTab.regionLayout"));
        sudokuSizeLabel = new Label(getFormatted("export.sudokuTab.sudokuSize"));
        if (addButton != null) {
            String btnText = getFormatted("export.tab.addToExport") != null ? getFormatted("export.tab.addToExport") : "Přidat do exportu";
            addButton.setText(btnText);
        }
    }

    public GameMode getCurrentConfig() {
        SudokuSize size = sudokuSizeChoiceBox.getValue();
        SudokuVariant variant = variantChoiceBox.getValue();
        if (variant == null || size == null) return null;
        SudokuRegionLayout selectedRegion = null;
        if (!MultiGridConfig.isMultiDoku(variant) && regionLayoutChoiceBox != null && variant == SudokuVariant.CLASSIC) {
            selectedRegion = regionLayoutChoiceBox.getValue();
        } else if (variant != SudokuVariant.CLASSIC) {
            if (MultiGridConfig.isMultiDoku(variant)) {
                selectedRegion = CustomLayoutsLoader.getBasicLayout(9);
            } else {
                selectedRegion = CustomLayoutsLoader.getBasicLayout(size);
            }
        }
        return new SudokuTypes(Difficulty.EASY, 1, (MultiGridConfig.isMultiDoku(variant)) ? SudokuSize.NINE : size, variant, selectedRegion, patternLayout);
    }

    public void initialize(Canvas canvas, User user, ExportMultipleGamesController controller) {
        this.canvas = canvas;
        this.controller = controller;
        this.user = user;

        setUpLabels();
        setUpCountGamesTextField(countGames, MAX_GENERATED);

        difficultySegmentedButton = createDifficultySegmentedButton();
        difficultyToggleGroup = difficultySegmentedButton.getToggleGroup();
        selectSegmentedButtonByUserData(difficultySegmentedButton, Difficulty.MEDIUM);

        SudokuControlConfig config = new SudokuControlConfig()
                .setCanvas(canvas).setVariantBox(variantChoiceBox)
                .setRegionBox(regionLayoutChoiceBox).setSizeBox(sudokuSizeChoiceBox)
                .setSupportedVariants(SudokuVariant.values());

        setupSudokuControls(config, SudokuVariant.CLASSIC);

        resetLayoutsForSize(SudokuSize.NINE);

        openChoice = new Button();
        openChoice.setOnAction(e -> {
            try {
                openPatternWindow();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        btnClearPattern = new Button(getFormatted("removeButton") != null ? getFormatted("removeButton") : "Odstranit vzor");
        btnClearPattern.setOnAction(e -> {
            SudokuSize size = sudokuSizeChoiceBox != null ? sudokuSizeChoiceBox.getValue() : SudokuSize.NINE;
            if (size == null) size = SudokuSize.NINE;
            this.patternLayout = CustomLayoutsLoader.getDefaultPatternLayoutForSize(size.getGridSize());
            if (controller != null) controller.updatePreview();
        });

        patternButtonsBox = new HBox(10, openChoice, btnClearPattern);

        variantChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            resetLayoutsForSize(sudokuSizeChoiceBox.getValue());
            buildSpecificLayout();
        });

        sudokuSizeChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (oldV != newV) resetLayoutsForSize(newV);
            buildSpecificLayout();
        });

        if (regionLayoutChoiceBox != null) {
            regionLayoutChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                if (controller != null) controller.updatePreview();
            });
        }

        buildSpecificLayout();
    }

    private void resetLayoutsForSize(SudokuSize size) {
        if (size != null) {
            if (variantChoiceBox != null && variantChoiceBox.getValue() == SudokuVariant.PATTERNED) {
                var availablePatterns = CustomLayoutsLoader.getPatternsForSize(size.getGridSize());
                if (availablePatterns != null && !availablePatterns.isEmpty()) {
                    this.patternLayout = availablePatterns.getFirst();
                } else {
                    this.patternLayout = CustomLayoutsLoader.getDefaultPatternLayoutForSize(size.getGridSize());
                }
            } else {
                this.patternLayout = CustomLayoutsLoader.getEmptyPatternLayout(size.getGridSize());
            }
        }
    }

    private Button btnClearPattern;
    private HBox patternButtonsBox;

    private void buildSpecificLayout() {
        sudokuGridPane.getChildren().clear();
        int row = 0;

        addGridRow(row++, sudokuVariantLabel, variantChoiceBox);

        SudokuVariant variant = variantChoiceBox.getValue();
        SudokuSize size = sudokuSizeChoiceBox.getValue();

        if (!MultiGridConfig.isMultiDoku(variant)) {
            if (sudokuSizeChoiceBox.getItems() != null && sudokuSizeChoiceBox.getItems().size() > 1) {
                addGridRow(row++, sudokuSizeLabel, sudokuSizeChoiceBox);
            }
        }
        if (variant == SudokuVariant.PATTERNED) {
            openChoice.setText(getFormatted("sudokuSettings.open_pattern"));
            addGridRow(row++, new Label(getFormatted("sudokuSettings.choose_pattern")), patternButtonsBox);
        } else if (!MultiGridConfig.isMultiDoku(variant)) {
            if (size != null && variant != null) {
                List<SudokuRegionLayout> regionLayouts;

                switch (variant) {
                    case IRREGULAR -> regionLayouts = FXCollections.observableArrayList(List.of());
                    case CLASSIC ->
                            regionLayouts = FXCollections.observableArrayList(CustomLayoutsLoader.getLayoutsForSize(size));
                    default ->
                            regionLayouts = FXCollections.observableArrayList(List.of(CustomLayoutsLoader.getBasicLayout(size)));
                }
                if (regionLayouts.size() > 1) {
                    addGridRow(row++, sudokuRegionLayoutLabel, regionLayoutChoiceBox);
                }
            }
        }

        addGridRow(row++, sudokuDifficultyLabel, difficultySegmentedButton);
        addGridRow(row++, sudokuCountLabel, countGames);

        if (addButton != null) {
            sudokuGridPane.add(addButton, 1, row);
            GridPane.setMargin(addButton, new Insets(10, 0, 0, 0));
        }

        if (controller != null) controller.updatePreview();
    }

    private void addGridRow(int row, Node col1, Node col2) {
        if (col1 != null) {
            sudokuGridPane.add(col1, 0, row);
            GridPane.setHalignment(col1, HPos.RIGHT);
        }
        if (col2 != null) {
            sudokuGridPane.add(col2, 1, row);
            GridPane.setHalignment(col2, HPos.LEFT);
        }
    }

    @FXML
    public void openPatternWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader(StartOfApp.class.getResource("/cz/logicgo/ui/windows/gameChoice/viewer.fxml"));
        SudokuCustomViewerController controller = new SudokuCustomViewerController();
        loader.setController(controller);

        Parent content = loader.load();
        content.getStyleClass().add("root");
        Stage stage = new Stage();

        var size = sudokuSizeChoiceBox.getValue() != null ? sudokuSizeChoiceBox.getValue().getGridSize() : 9;
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
            this.patternLayout = chP.getPatternLayout();
            if (this.controller != null) this.controller.updatePreview();
        }
    }

    @FXML
    private void addToList() {
        SudokuTypes sudokuData = (SudokuTypes) getCurrentConfig();
        if (sudokuData == null) return;

        int count = Integer.parseInt(countGames.getText());

        Toggle diffToggle = difficultyToggleGroup.getSelectedToggle();
        Difficulty difficulty = diffToggle != null ? (Difficulty) diffToggle.getUserData() : Difficulty.MEDIUM;

        SudokuTypes finalData = new SudokuTypes(
                difficulty, count, sudokuData.sudokuSize(), sudokuData.sudokuVariant(), sudokuData.regionLayout(), sudokuData.patternLayout()
        );

        var dummySudoku = new Sudoku();
        dummySudoku.setVariant(finalData.sudokuVariant());
        dummySudoku.setType(finalData.sudokuSize());
        dummySudoku.setDifficulty(difficulty);

        ExportItem item = new ExportItem(
                FavoriteFormatter.parseGameDetails(dummySudoku),
                finalData
        );
        controller.addToList(item);
    }
}
