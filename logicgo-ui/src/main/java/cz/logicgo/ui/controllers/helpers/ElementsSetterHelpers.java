package cz.logicgo.ui.controllers.helpers;

import cz.logicgo.core.gameClasses.sudoku.misc.SizeRange;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuRegionLayout;
import cz.logicgo.core.misc.TranslationLoader;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.PageLayout;
import cz.logicgo.core.misc.enums.PreviewType;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeAlgorithm;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.core.misc.enums.hints.BridgeHintType;
import cz.logicgo.core.misc.enums.hints.MazeHintType;
import cz.logicgo.core.misc.enums.hints.ShikakuHintType;
import cz.logicgo.core.misc.enums.hints.SudokuHintType;
import cz.logicgo.core.misc.interfaces.Translatable;
import cz.logicgo.engine.algorithms.settings.DifficultyChoosing;
import cz.logicgo.engine.algorithms.sudoku.custom.CustomLayoutsLoader;
import cz.logicgo.engine.algorithms.sudoku.validators.MultiGridConfig;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.controlsfx.control.SegmentedButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cz.logicgo.core.gameClasses.sudoku.SudokuUtils.intToSizes;
import static cz.logicgo.core.misc.Messages.getFormatted;
import static cz.logicgo.ui.controllers.screenControllers.RedrawCanvasFunctions.clearCanvas;
import static cz.logicgo.ui.controllers.screenControllers.RedrawCanvasFunctions.strokeBoard;


public class ElementsSetterHelpers {

    public static SegmentedButton createMazeShapeSegmentedButton(ToggleGroup toggleGroup) {
        SegmentedButton segmentedButton = new SegmentedButton();

        for (MazeShape shape : MazeShape.values()) {
            ToggleButton btn = new ToggleButton(shape.getTranslation());
            btn.setUserData(shape);
            btn.setToggleGroup(toggleGroup);
            segmentedButton.getButtons().add(btn);
        }

        if (!segmentedButton.getButtons().isEmpty()) {
            setUpSegmentedButtonHandling(segmentedButton, segmentedButton.getButtons().getFirst());
        }

        return segmentedButton;
    }

    public static SegmentedButton createDifficultySegmentedButton() {
        SegmentedButton segmentedButton = new SegmentedButton();

        for (Difficulty diff : Difficulty.values()) {
            ToggleButton btn = new ToggleButton(diff.getTranslation());
            btn.setUserData(diff);
            segmentedButton.getButtons().add(btn);
        }

        if (!segmentedButton.getButtons().isEmpty()) {
            setUpSegmentedButtonHandling(segmentedButton, segmentedButton.getButtons().getFirst());
        }

        for (ToggleButton btn : segmentedButton.getButtons()) {
            if (btn.getUserData() == Difficulty.MEDIUM) {
                btn.setSelected(true);
                break;
            }
        }

        return segmentedButton;
    }

    public static SegmentedButton createMultipleCountSegmentedButton(ToggleGroup toggleGroup) {
        SegmentedButton segmentedButton = new SegmentedButton();

        for (int i = 3; i <= 5; i++) {
            ToggleButton btn = new ToggleButton(String.valueOf(i));
            btn.setUserData(i);
            btn.setToggleGroup(toggleGroup);
            segmentedButton.getButtons().add(btn);

            if (i == 3) {
                btn.setSelected(true);
            }
        }

        setUpSegmentedButtonHandling(segmentedButton, segmentedButton.getButtons().getFirst());

        return segmentedButton;
    }

    public static void selectToggleByUserData(ToggleGroup group, Object userData) {
        if (group == null || userData == null) return;
        for (Toggle toggle : group.getToggles()) {
            if (toggle.getUserData() != null && toggle.getUserData().equals(userData)) {
                toggle.setSelected(true);
                break;
            }
        }
    }

    public static void selectSegmentedButtonByUserData(SegmentedButton segmentedButton, Object userData) {
        if (segmentedButton == null || userData == null) return;

        for (ToggleButton btn : segmentedButton.getButtons()) {
            if (userData.equals(btn.getUserData())) {
                btn.setSelected(true);
                break;
            }
        }
    }

    public static void updateAlgorithmItems(MazeShape shape, MazeType type, boolean hasCustomMask, ChoiceBox<MazeAlgorithm> algorithmChoiceBox) {
        if (shape == null || algorithmChoiceBox == null) return;

        List<MazeAlgorithm> supportedAlgos = new ArrayList<>(shape.getSupportedAlgorithms());

        List<MazeAlgorithm> supportedTypeAlgos = type != null ? type.getSupportedAlgorithms() : List.of(MazeAlgorithm.values());
        supportedAlgos.retainAll(supportedTypeAlgos);

        if (hasCustomMask) {
            supportedAlgos.retainAll(MazeType.WALLS.getSupportedAlgorithms());
        }

        ObservableList<MazeAlgorithm> algorithmObservableList = FXCollections.observableArrayList(supportedAlgos);

        MazeAlgorithm currentSelected = algorithmChoiceBox.getValue();

        algorithmChoiceBox.setItems(algorithmObservableList);

        if (!algorithmObservableList.isEmpty()) {
            if (currentSelected != null && algorithmObservableList.contains(currentSelected)) {
                algorithmChoiceBox.getSelectionModel().select(currentSelected);
            } else if (algorithmObservableList.contains(MazeAlgorithm.RECURSIVE_BACKTRACKER)) {
                algorithmChoiceBox.getSelectionModel().select(MazeAlgorithm.RECURSIVE_BACKTRACKER);
            } else {
                algorithmChoiceBox.getSelectionModel().selectFirst();
            }
        }

        algorithmChoiceBox.setDisable(algorithmObservableList.size() <= 1);

        algorithmChoiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(MazeAlgorithm algorithm) {
                return (algorithm != null) ? algorithm.getTranslation() : getFormatted("choiceNoValue");
            }

            @Override
            public MazeAlgorithm fromString(String val) {
                return TranslationLoader.getInstanceFromTranslation(val);
            }
        });
    }

    public static void setUpCountGamesTextField(TextField countGamesTextField, int maxGenerated) {
        countGamesTextField.setText("1");

        countGamesTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isCountValid(newValue, 0, maxGenerated)) {
                if (!countGamesTextField.getStyleClass().contains("error")) {
                    countGamesTextField.getStyleClass().add("error");
                }
            } else {
                countGamesTextField.getStyleClass().removeAll(Collections.singleton("error"));
            }
        });
    }

    public static boolean isCountValid(String number, int min, int max) {
        try {
            int num = Integer.parseInt(number);
            return num > min && num < max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static void setupSudokuControls(SudokuControlConfig config, SudokuVariant main) {
        var variantBox = config.getVariantBox();
        var regionBox = config.getRegionBox();
        var sizeBox = config.getSizeBox();
        var canvas = config.getCanvas();

        if (regionBox != null && variantBox != null && sizeBox != null) {
            setItemsRegionTypeChoiceBox(canvas, variantBox, sizeBox, regionBox);
        }

        if (sizeBox != null) {
            setItemsSudokuSizeChoiceBox(variantBox, sizeBox, regionBox);
        }

        if (variantBox != null) {
            setItemsSudokuVariant(variantBox, sizeBox, config.getSupportedVariants());
            variantBox.getSelectionModel().select(main);
        }
    }

    public static void setupMazeControls(MazeControlConfig config) {
        var typeChoiceBox = config.getTypeChoiceBox();
        var shapeChoiceBox = config.getShapeChoiceBox();
        var algoChoiceBox = config.getAlgorithmChoiceBox();
        var canvas = config.getCanvas();

        if (algoChoiceBox != null) {
            setItemsMazeAlgorithmChoiceBox(canvas, typeChoiceBox, shapeChoiceBox, algoChoiceBox);
        }

        if (shapeChoiceBox != null) {
            setItemsMazeShapeChoiceBox(typeChoiceBox, shapeChoiceBox, algoChoiceBox);
        }

        if (typeChoiceBox != null) {
            setItemsMazeType(typeChoiceBox, shapeChoiceBox);
            typeChoiceBox.getSelectionModel().select(MazeType.CLASSIC);
        }
    }

    public static void setItemsMazeType(ChoiceBox<MazeType> typeBox,
                                        ChoiceBox<MazeShape> shapeBox, MazeType... onlyTypes) {
        if (typeBox == null) return;

        if (onlyTypes == null || onlyTypes.length == 0 || onlyTypes[0] == null) {
            typeBox.setItems(FXCollections.observableArrayList(MazeType.values()));
        } else {
            typeBox.setItems(FXCollections.observableArrayList(onlyTypes));
        }

        typeBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(MazeType variant) {
                return (variant != null) ? variant.getTranslation() : getFormatted("choiceNoValue");
            }

            @Override
            public MazeType fromString(String string) {
                return TranslationLoader.getInstanceFromTranslation(string);
            }
        });

        typeBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && shapeBox != null) {
                updateShapeItems(newVal, shapeBox);
            }
        });
    }

    private static void updateShapeItems(MazeType type, ChoiceBox<MazeShape> shapeChoiceBox) {
        if (shapeChoiceBox == null) return;
        var supportedSizes = type.getSupportedShapes();

        shapeChoiceBox.setItems(FXCollections.observableArrayList(supportedSizes));

        if (supportedSizes.contains(MazeShape.RECTANGULAR)) {
            shapeChoiceBox.getSelectionModel().select(MazeShape.RECTANGULAR);
        } else if (!supportedSizes.isEmpty()) {
            shapeChoiceBox.getSelectionModel().selectFirst();
        }
        shapeChoiceBox.setDisable(shapeChoiceBox.getItems().size() <= 1);
    }

    public static void setItemsMazeShapeChoiceBox(ChoiceBox<MazeType> typeChoiceBox,
                                                  ChoiceBox<MazeShape> shapeChoiceBox,
                                                  ChoiceBox<MazeAlgorithm> algorithmChoiceBox) {
        setItemsMazeShapeChoiceBox(typeChoiceBox, shapeChoiceBox, algorithmChoiceBox, false);
    }

    public static void setItemsMazeShapeChoiceBox(ChoiceBox<MazeType> typeChoiceBox,
                                                  ChoiceBox<MazeShape> shapeChoiceBox,
                                                  ChoiceBox<MazeAlgorithm> algorithmChoiceBox,
                                                  boolean hasCustomMask) {
        if (shapeChoiceBox == null) return;

        shapeChoiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(MazeShape shape) {
                return (shape != null) ? shape.getTranslation() : getFormatted("choiceNoValue");
            }

            @Override
            public MazeShape fromString(String string) {
                return TranslationLoader.getInstanceFromTranslation(string);
            }
        });

        shapeChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newShape) -> {
            if (newShape != null && algorithmChoiceBox != null) {
                MazeType type = typeChoiceBox != null ? typeChoiceBox.getValue() : MazeType.CLASSIC;
                updateAlgorithmItems(newShape, type, hasCustomMask, algorithmChoiceBox);
            }
        });
    }

    private static void updateMultiLevelItems(ChoiceBox<MazeType> mazeTypeChoiceBox) {
        if (mazeTypeChoiceBox == null) return;
        ArrayList<MazeType> types = new ArrayList<>(List.of(MazeType.values()));
        types.remove(MazeType.MULTI_LEVEL);
        ObservableList<MazeType> list = FXCollections.observableArrayList(types);
        mazeTypeChoiceBox.setItems(list);
        if (!list.isEmpty()) {
            mazeTypeChoiceBox.getSelectionModel().selectFirst();
        }
        mazeTypeChoiceBox.setDisable(list.size() <= 1);
    }

    public static void setItemsMazeAlgorithmChoiceBox(Canvas canvas,
                                                      ChoiceBox<MazeType> typeChoiceBox,
                                                      ChoiceBox<MazeShape> shapeChoiceBox,
                                                      ChoiceBox<MazeAlgorithm> algorithmChoiceBox) {
        if (algorithmChoiceBox == null) return;

        algorithmChoiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(MazeAlgorithm algorithm) {
                return (algorithm != null) ? algorithm.getTranslation() : getFormatted("choiceNoValue");
            }

            @Override
            public MazeAlgorithm fromString(String val) {
                return TranslationLoader.getInstanceFromTranslation(val);
            }
        });
    }

    public static void setItemsSudokuVariant(ChoiceBox<SudokuVariant> variantBox,
                                             ChoiceBox<SudokuSize> sizeBox, SudokuVariant... onlyVariants) {
        if (variantBox == null) return;

        if (onlyVariants == null || onlyVariants.length == 0 || onlyVariants[0] == null) {
            variantBox.setItems(FXCollections.observableArrayList(SudokuVariant.values()));
        } else {
            variantBox.setItems(FXCollections.observableArrayList(onlyVariants));
        }

        variantBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(SudokuVariant variant) {
                return (variant != null) ? variant.getTranslation() : getFormatted("choiceNoValue");
            }

            @Override
            public SudokuVariant fromString(String string) {
                return TranslationLoader.getInstanceFromTranslation(string);
            }
        });

        variantBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && sizeBox != null) {
                updateSizeItems(newVal, sizeBox);
            }
        });
    }

    public static void setItemsSudokuSizeChoiceBox(ChoiceBox<SudokuVariant> variantBox,
                                                   ChoiceBox<SudokuSize> sizeBox,
                                                   ChoiceBox<SudokuRegionLayout> regionBox) {
        if (sizeBox == null) return;
        sizeBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(SudokuSize size) {
                return (size != null) ? size.getDescription() : getFormatted("choiceNoValue");
            }

            @Override
            public SudokuSize fromString(String string) {
                return SudokuSize.getInstanceByDescription(string);
            }
        });

        sizeBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newSize) -> {
            if (newSize != null && regionBox != null && variantBox != null) {
                updateRegionItems(variantBox.getValue(), newSize, regionBox);
            }
        });
    }

    public static void setItemsRegionTypeChoiceBox(Canvas canvas,
                                                   ChoiceBox<SudokuVariant> variantBox,
                                                   ChoiceBox<SudokuSize> sizeBox,
                                                   ChoiceBox<SudokuRegionLayout> regionBox) {
        if (regionBox == null) return;

        regionBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(SudokuRegionLayout layout) {
                return (layout != null) ? layout.getTranslatedName() : getFormatted("choiceNoValue");
            }

            @Override
            public SudokuRegionLayout fromString(String string) {
                String revTransName = CustomLayoutsLoader.getLayoutsTranslated().get(string);
                return CustomLayoutsLoader.getLayoutsByName(revTransName);
            }
        });

        regionBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newLayout) -> {
            if (newLayout != null && sizeBox != null && sizeBox.getValue() != null && variantBox != null && variantBox.getValue() != null) {
                SudokuSize size = SudokuSize.getTypeByGridSize(newLayout.getRegions().length);
                onCanvasTypeChangeSudokuTab(canvas, size, regionBox, variantBox.getValue());
            }
        });
    }

    public static void setItemsMazeTypeChoiceBox(Canvas canvas, ChoiceBox<MazeType> mazeTypeChoiceBox) {
        if (mazeTypeChoiceBox == null) return;
        updateMultiLevelItems(mazeTypeChoiceBox);
        mazeTypeChoiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(MazeType type) {
                return (type != null) ? type.getTranslation() : getFormatted("choiceNoValue");
            }

            @Override
            public MazeType fromString(String string) {
                return TranslationLoader.getInstanceFromTranslation(string);
            }
        });
    }

    private static void updateSizeItems(SudokuVariant variant, ChoiceBox<SudokuSize> sizeBox) {
        if (sizeBox == null) return;
        SizeRange sizes = DifficultyChoosing.getValidSizeRange(variant);

        List<SudokuSize> sizeInstances = intToSizes(sizes);
        if (variant != SudokuVariant.CLASSIC) {
            sizeInstances.remove(SudokuSize.SEVEN);
        }
        sizeBox.setItems(FXCollections.observableArrayList(sizeInstances));

        if (sizeInstances.contains(SudokuSize.NINE)) {
            sizeBox.getSelectionModel().select(SudokuSize.NINE);
        } else if (!sizeInstances.isEmpty()) {
            sizeBox.getSelectionModel().selectFirst();
        }
        sizeBox.setDisable(sizeBox.getItems().size() <= 1);
    }

    private static void updateRegionItems(SudokuVariant variant, SudokuSize size, ChoiceBox<SudokuRegionLayout> regionBox) {
        if (regionBox == null || variant == null || size == null) return;
        ObservableList<SudokuRegionLayout> regionLayouts;

        switch (variant) {
            case IRREGULAR, PATTERNED -> regionLayouts = FXCollections.observableArrayList(List.of());
            case CLASSIC ->
                    regionLayouts = FXCollections.observableArrayList(CustomLayoutsLoader.getLayoutsForSize(size));
            default ->
                    regionLayouts = FXCollections.observableArrayList(List.of(CustomLayoutsLoader.getBasicLayout(size)));
        }

        regionBox.setItems(regionLayouts);

        if (!regionLayouts.isEmpty()) {
            regionBox.getSelectionModel().selectFirst();
        }
        regionBox.setDisable(regionLayouts.size() <= 1);
    }

    public static void onCanvasTypeChangeSudokuTab(Canvas canvas, SudokuSize sudokuSize, ChoiceBox<SudokuRegionLayout> regionTypeChoiceBox, SudokuVariant sudokuVariant) {
        if (canvas == null) return;
        double width = canvas.getWidth() / sudokuSize.getGridSize();
        double height = canvas.getHeight() / sudokuSize.getGridSize();

        SudokuRegionLayout value = regionTypeChoiceBox.getSelectionModel().getSelectedItem();
        clearCanvas(canvas);
        if (value != null && value.getRegions() != null) {
            strokeBoard(
                    value.getRegions(), width, height,
                    canvas.getGraphicsContext2D(),
                    sudokuVariant, CustomLayoutsLoader.getEmptyPatternLayout(sudokuSize),
                    true
            );
        }
    }

    public static void setItemsLayoutChoiceBox(ChoiceBox<PageLayout> choiceLayout) {
        ObservableList<PageLayout> layouts = FXCollections.observableArrayList(PageLayout.values());
        choiceLayout.setItems(layouts);
        choiceLayout.getSelectionModel().select(PageLayout.AUTOMATIC);

        choiceLayout.setConverter(new StringConverter<>() {
            @Override
            public String toString(PageLayout layout) {
                return (layout != null) ? layout.getTranslation() : getFormatted("choiceNoValue");
            }

            @Override
            public PageLayout fromString(String string) {
                return TranslationLoader.getInstanceFromTranslation(string);
            }
        });
    }

    public static void setItemsPreviewTypeChoiceBox(ChoiceBox<PreviewType> choiceExport) {
        ObservableList<PreviewType> previewTypes = FXCollections.observableArrayList(List.of(PreviewType.UNSOLVED, PreviewType.BOTH));
        choiceExport.setItems(previewTypes);
        choiceExport.getSelectionModel().select(PreviewType.BOTH);

        choiceExport.setConverter(new StringConverter<>() {
            @Override
            public String toString(PreviewType difficulty) {
                return (difficulty != null) ? difficulty.getTranslation() : getFormatted("choiceNoValue");
            }

            @Override
            public PreviewType fromString(String string) {
                return TranslationLoader.getInstanceFromTranslation(string);
            }
        });
    }

    public static void setUpHintTypeComboBox(ComboBox<Object> hintTypeComboBox, TypeGame gameType) {
        setUpHintTypeComboBox(hintTypeComboBox, gameType, null);
    }

    public static void setUpHintTypeComboBox(ComboBox<Object> hintTypeComboBox, TypeGame gameType, Object other) {
        ObservableList<Object> hintTypeList = FXCollections.observableArrayList();

        switch (gameType) {
            case SUDOKU -> {
                var list = new ArrayList<>(List.of(SudokuHintType.values()));
                if (other instanceof SudokuVariant variant && !MultiGridConfig.isMultiDoku(variant)) {
                    list.remove(SudokuHintType.CHECK_ONE_BOARD);
                }
                hintTypeList.addAll(list);
                hintTypeComboBox.getSelectionModel().select(SudokuHintType.CHECK_CELL);
            }
            case BRIDGE -> {
                hintTypeList.addAll(BridgeHintType.values());
                hintTypeComboBox.getSelectionModel().select(BridgeHintType.CHECK_ISLAND);
            }
            case MAZE -> {
                hintTypeList.addAll(MazeHintType.values());
                hintTypeComboBox.getSelectionModel().select(MazeHintType.SHOW_LITTLE_OF_PATH);
            }
            case SHIKAKU -> {
                hintTypeList.addAll(ShikakuHintType.values());
                hintTypeComboBox.getSelectionModel().select(ShikakuHintType.CHECK_VALIDITY);
            }
        }

        hintTypeComboBox.setItems(hintTypeList);

        hintTypeComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setDisable(false);
                } else if (item instanceof GroupLabel(String name)) {
                    setText(name);
                    setStyle("-fx-font-weight: bold; -fx-background-color: #7b98b5; -fx-text-fill: white;");
                    setDisable(true);
                } else if (item instanceof Translatable translatable) {
                    setText("  " + translatable.getTranslation());
                    setStyle("");
                    setDisable(false);
                }
            }
        });

        hintTypeComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item instanceof GroupLabel) {
                    setText(null);
                } else if (item instanceof Translatable translatable) {
                    setText(translatable.getTranslation());
                }
            }
        });

        hintTypeComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Object obj) {
                if (obj instanceof Translatable translatable) {
                    return translatable.getTranslation();
                } else if (obj instanceof GroupLabel(String name)) {
                    return name;
                }
                return "";
            }

            @Override
            public Object fromString(String string) {
                return null;
            }
        });
    }

    public static void setUpSegmentedButtonHandling(SegmentedButton segmentedButton, ToggleButton defaultButton) {
        if (segmentedButton == null || defaultButton == null) return;
        defaultButton.setSelected(true);
        segmentedButton.getToggleGroup().selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (oldVal == newVal || newVal == null) {
                if (oldVal != null) {
                    oldVal.setSelected(true);
                }
            }
        });
    }
}
