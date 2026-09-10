package cz.logicgo.core.gameClasses.export.gameTypes;


import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuRegionLayout;

public record SudokuTypes(
        Difficulty difficulty,
        int count,
        SudokuSize sudokuSize,
        SudokuVariant sudokuVariant,
        SudokuRegionLayout regionLayout,
        SudokuPatternLayout patternLayout
) implements GameMode {
    @Override
    public Difficulty getDifficulty() {
        return difficulty;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public TypeGame getTypeGame() {
        return TypeGame.SUDOKU;
    }
}
