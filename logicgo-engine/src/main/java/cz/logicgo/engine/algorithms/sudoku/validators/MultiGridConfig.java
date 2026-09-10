package cz.logicgo.engine.algorithms.sudoku.validators;


import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MultiGridConfig {

    public static final Map<SudokuVariant, GridSetup> LAYOUTS = Map.ofEntries(
            Map.entry(SudokuVariant.TWODOKU, new GridSetup(15, new int[][]{{0, 0}, {6, 6}})),
            Map.entry(SudokuVariant.SAMURAI, new GridSetup(21, new int[][]{{0, 0}, {0, 12}, {6, 6}, {12, 0}, {12, 12}})),
            Map.entry(SudokuVariant.CROSS, new GridSetup(21, new int[][]{{0, 6}, {6, 0}, {6, 6}, {6, 12}, {12, 6}})),
            Map.entry(SudokuVariant.TRIPLEDOKU, new GridSetup(21, new int[][]{{0, 0}, {6, 6}, {12, 12}})),
            Map.entry(SudokuVariant.DOUBLEDOKU, new GridSetup(12, new int[][]{{0, 0}, {3, 3}})),
            Map.entry(SudokuVariant.COLUMNDOKU, new GridSetup(15, new int[][]{{0, 0}, {3, 0}, {6, 0}}))
    );

    public static boolean isMultiDoku(SudokuVariant variant) {
        return variant != null && LAYOUTS.containsKey(variant);
    }

    public record GridSetup(int globalSize, int[][] offsets) {

        public int getRealCellCount() {
            Set<Long> uniqueCells = new HashSet<>();
            for (int[] offset : offsets) {
                int rowOff = offset[0];
                int colOff = offset[1];
                for (int r = 0; r < 9; r++) {
                    for (int c = 0; c < 9; c++) {
                        long encodedCoords = ((long) (rowOff + r) << 32) | (colOff + c);
                        uniqueCells.add(encodedCoords);
                    }
                }
            }
            return uniqueCells.size();
        }
    }
}
