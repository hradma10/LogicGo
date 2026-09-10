package cz.logicgo.core.builders.sudoku;


import cz.logicgo.core.builders.GameCreation;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuRegionLayout;

public abstract sealed class SudokuBuilderBase<T extends SudokuBuilderBase<T>> extends GameCreation<T> permits SudokuCreation {

    private SudokuSize sudokuSize;
    private SudokuVariant sudokuVariant;
    private SudokuRegionLayout regionLayout;
    private SudokuPatternLayout patternLayout;
    private Integer removeCount;

    public SudokuBuilderBase() {
        this.setTypeGame(TypeGame.SUDOKU);
    }

    public SudokuSize getSudokuSize() {
        return sudokuSize;
    }

    public T setSudokuSize(SudokuSize sudokuSize) {
        this.sudokuSize = sudokuSize;
        this.setHeight(sudokuSize.getGridSize());
        this.setWidth(sudokuSize.getGridSize());
        return self();
    }

    public SudokuVariant getSudokuVariant() {
        return sudokuVariant;
    }

    public T setSudokuVariant(SudokuVariant sudokuVariant) {
        this.sudokuVariant = sudokuVariant;
        return self();
    }

    public SudokuRegionLayout getRegionLayout() {
        return regionLayout;
    }

    public T setRegionLayout(SudokuRegionLayout regionLayout) {
        this.regionLayout = regionLayout;
        return self();
    }

    public Integer getRemoveCount() {
        return removeCount;
    }

    public T setRemoveCount(Integer removeCount) {
        this.removeCount = removeCount;
        return self();
    }

    public SudokuPatternLayout getPatternLayout() {
        return patternLayout;
    }

    public T setPatternLayout(SudokuPatternLayout patternLayout) {
        this.patternLayout = patternLayout;
        return self();
    }

}
