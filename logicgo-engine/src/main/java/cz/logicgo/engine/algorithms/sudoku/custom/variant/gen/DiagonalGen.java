package cz.logicgo.engine.algorithms.sudoku.custom.variant.gen;


import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;

import java.util.Arrays;

public class DiagonalGen {

    public static SudokuPatternLayout generateMainDiagonalLayout(int size) {
        String name = createName(true);

        Integer[][] layout = new Integer[size][size];
        for (int i = 0; i < size; i++) {
            Arrays.fill(layout[i], 0);
        }

        for (int i = 0; i < size; i++) {
            layout[i][i] = 1;
        }

        String translatedName = createTranslatedName(size, true);
        return new SudokuPatternLayout(name, layout, true, translatedName);
    }

    public static SudokuPatternLayout generateAntiDiagonalLayout(int size) {
        String name = createName(false);

        Integer[][] layout = new Integer[size][size];
        for (int i = 0; i < size; i++) {
            Arrays.fill(layout[i], 0);
        }

        for (int i = 0; i < size; i++) {
            layout[i][size - 1 - i] = 1;
        }

        String translatedName = createTranslatedName(size, false);
        return new SudokuPatternLayout(name, layout, true, translatedName);
    }

    public static String createName(boolean main) {
        String type = main ? "main" : "anti";
        return String.format("sudokuGame.variant.diagonal.%s", type);
    }

    public static String createTranslatedName(int size, boolean main) {
        String type = main ? "main" : "anti";
        return String.format("Diagonal-%s %s x %s", type, size, size);
    }
}
