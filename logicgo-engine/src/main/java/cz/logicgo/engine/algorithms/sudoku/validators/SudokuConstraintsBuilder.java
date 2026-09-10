package cz.logicgo.engine.algorithms.sudoku.validators;


import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuRegionLayout;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;

public class SudokuConstraintsBuilder {

    SudokuVariant variant;
    SudokuSize sudokuSize;
    SudokuRegionLayout regionLayout;
    SudokuPatternLayout patternLayout;

    public SudokuConstraintsBuilder() {
    }

    public SudokuVariant getVariant() {
        return variant;
    }

    public SudokuConstraintsBuilder setVariant(SudokuVariant variant) {
        this.variant = variant;
        return this;
    }

    public SudokuSize getSudokuSize() {
        return sudokuSize;
    }

    public SudokuConstraintsBuilder setSudokuSize(SudokuSize sudokuSize) {
        this.sudokuSize = sudokuSize;
        return this;
    }

    public SudokuRegionLayout getRegionLayout() {
        return regionLayout;
    }

    public SudokuConstraintsBuilder setRegionLayout(SudokuRegionLayout regionLayout) {
        this.regionLayout = regionLayout;
        return this;
    }

    public SudokuPatternLayout getPatternLayout() {
        return patternLayout;
    }

    public SudokuConstraintsBuilder setPatternLayout(SudokuPatternLayout patternLayout) {
        this.patternLayout = patternLayout;
        return this;
    }
}
