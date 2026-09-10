package cz.logicgo.engine.algorithms.sudoku.solvers;

import java.util.Random;

public class ZobristTable {
    private final long[][][] table;

    public ZobristTable(int gridSize, boolean isMultidoku) {
        int maxNumbers = isMultidoku ? 10 : gridSize + 1;
        this.table = new long[gridSize][gridSize][maxNumbers];

        Random rnd = new Random(67);

        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                for (int v = 1; v < maxNumbers; v++) {
                    table[r][c][v] = rnd.nextLong();
                }
            }
        }
    }

    public long getHashValue(int row, int col, int value) {
        if (row < 0 || row >= table.length || col < 0 || col >= table[0].length || value < 1 || value >= table[0][0].length) {
            return 0L;
        }

        return table[row][col][value];
    }
}
