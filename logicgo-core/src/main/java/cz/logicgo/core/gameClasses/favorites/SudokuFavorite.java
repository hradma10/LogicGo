package cz.logicgo.core.gameClasses.favorites;

import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuRegionLayout;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;

import java.util.Objects;

public final class SudokuFavorite extends GameFavorite {

    private SudokuVariant variant;
    private SudokuSize size;
    private SudokuRegionLayout regionLayout;
    private SudokuPatternLayout patternLayout;

    @Override
    public TypeGame getTypeGame() {
        return TypeGame.SUDOKU;
    }

    public SudokuFavorite() {
        super();
    }

    public SudokuVariant getVariant() {
        return variant;
    }

    public void setVariant(SudokuVariant variant) {
        this.variant = variant;
    }

    public SudokuSize getSize() {
        return size;
    }

    public void setSize(SudokuSize size) {
        this.size = size;
    }

    public SudokuRegionLayout getRegionLayout() {
        return regionLayout;
    }

    public void setRegionLayout(SudokuRegionLayout regionLayout) {
        this.regionLayout = regionLayout;
    }

    public SudokuPatternLayout getPatternLayout() {
        return patternLayout;
    }

    public void setPatternLayout(SudokuPatternLayout patternLayout) {
        this.patternLayout = patternLayout;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SudokuFavorite that)) return false;
        if (!super.equals(o)) return false;
        return getVariant() == that.getVariant() && getSize() == that.getSize() && Objects.equals(getRegionLayout(), that.getRegionLayout()) && Objects.equals(getPatternLayout(), that.getPatternLayout());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getVariant(), getSize(), getRegionLayout(), getPatternLayout());
    }

}
