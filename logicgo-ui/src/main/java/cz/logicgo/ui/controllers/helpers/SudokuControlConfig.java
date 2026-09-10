package cz.logicgo.ui.controllers.helpers;

import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuRegionLayout;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ChoiceBox;

public class SudokuControlConfig {
    private Canvas canvas;
    private ChoiceBox<SudokuVariant> variantBox;
    private ChoiceBox<SudokuSize> sizeBox;
    private ChoiceBox<SudokuRegionLayout> regionBox;
    private SudokuVariant[] supportedVariants;

    public Canvas getCanvas() {
        return canvas;
    }

    public SudokuControlConfig setCanvas(Canvas canvas) {
        this.canvas = canvas;
        return this;
    }

    public ChoiceBox<SudokuVariant> getVariantBox() {
        return variantBox;
    }

    public SudokuControlConfig setVariantBox(ChoiceBox<SudokuVariant> variantBox) {
        this.variantBox = variantBox;
        return this;
    }

    public ChoiceBox<SudokuSize> getSizeBox() {
        return sizeBox;
    }

    public SudokuControlConfig setSizeBox(ChoiceBox<SudokuSize> sizeBox) {
        this.sizeBox = sizeBox;
        return this;
    }

    public ChoiceBox<SudokuRegionLayout> getRegionBox() {
        return regionBox;
    }

    public SudokuControlConfig setRegionBox(ChoiceBox<SudokuRegionLayout> regionBox) {
        this.regionBox = regionBox;
        return this;
    }

    public SudokuVariant[] getSupportedVariants() {
        return supportedVariants;
    }

    public SudokuControlConfig setSupportedVariants(SudokuVariant... supportedVariants) {
        this.supportedVariants = supportedVariants;
        return this;
    }
}
