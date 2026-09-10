package cz.logicgo.engine.algorithms.sudoku.custom.variant.gen;


import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;

public class OffsetGen {

    public static SudokuPatternLayout getOffsetPattern(int size) {

        String name = createName();

        int height = (int) Math.sqrt(size);
        int width = size / height;

        if (size == 6) {
            height = 2;
            width = 3;
        } else if (size == 8) {
            height = 4;
            width = 2;
        } else if (size == 12) {
            height = 3;
            width = 4;
        } else if (size == 15) {
            height = 5;
            width = 3;
        }

        Integer[][] layout = generateOffsetMatrix(size, height, width);

        String translatedName = String.format("Offset %s x %s", size, size);
        SudokuPatternLayout newPattern = new SudokuPatternLayout(name, layout, false, translatedName);
        newPattern.setSelective(false);

        return newPattern;
    }

    public static String createName() {
        return "sudoku.variant.offset";
    }

    private static Integer[][] generateOffsetMatrix(int size, int blockHeight, int blockWidth) {
        Integer[][] layout = new Integer[size][size];

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int relativeRow = row % blockHeight;
                int relativeCol = col % blockWidth;

                int id = (relativeRow * blockWidth) + relativeCol;
                layout[row][col] = id;
            }
        }
        return layout;
    }
}
