package cz.logicgo.ui.controllers.gameControllers.choosers;


import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;

public final class ChosenPattern implements IChooser {
    SudokuPatternLayout patternLayout = null;

    public SudokuPatternLayout getPatternLayout() {
        return patternLayout;
    }

    public ChosenPattern setPatternLayout(SudokuPatternLayout patternLayout) {
        this.patternLayout = patternLayout;
        return this;
    }
}
