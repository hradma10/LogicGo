package cz.logicgo.engine.algorithms.sudoku.validators.constraints;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.Constraint;

import java.util.Arrays;

public class SkyscraperConstraint implements Constraint {

    private final SudokuCell[][] cells;
    private final int gridSize;

    private final int[] topClues;
    private final int[] bottomClues;
    private final int[] leftClues;
    private final int[] rightClues;

    public SkyscraperConstraint(SudokuCell[][] cells, int[] topClues, int[] bottomClues, int[] leftClues, int[] rightClues) {
        this.cells = cells;
        this.gridSize = cells.length;
        this.topClues = topClues;
        this.bottomClues = bottomClues;
        this.leftClues = leftClues;
        this.rightClues = rightClues;
    }

    @Override
    public boolean isViolated(int row, int col, int num) {
        int currentValue = (num != 0) ? num : cells[row][col].getValue();
        if (currentValue == 0) return false;

        if (leftClues[row] != 0 && isViewViolated(row, col, currentValue, row, 0, 0, 1, leftClues[row])) return true;
        if (rightClues[row] != 0 && isViewViolated(row, col, currentValue, row, gridSize - 1, 0, -1, rightClues[row]))
            return true;
        if (topClues[col] != 0 && isViewViolated(row, col, currentValue, 0, col, 1, 0, topClues[col])) return true;
        return bottomClues[col] != 0 && isViewViolated(row, col, currentValue, gridSize - 1, col, -1, 0, bottomClues[col]);
    }

    private boolean isViewViolated(int row, int col, int checkedValue, int rStart, int cStart, int rStep, int cStep, int clue) {
        int r = rStart;
        int c = cStart;
        boolean isFull = true;
        int visible = 0;
        int maxSeen = 0;
        int firstVal = 0;

        for (int i = 0; i < gridSize; i++) {
            int val = (r == row && c == col) ? checkedValue : cells[r][c].getValue();

            if (i == 0) firstVal = val;

            if (val == 0) {
                isFull = false;
            } else {
                if (val > maxSeen) {
                    visible++;
                    maxSeen = val;
                }
            }

            r += rStep;
            c += cStep;
        }

        if (isFull) {
            return visible != clue;
        }

        if (firstVal == gridSize && clue != 1) return true;
        return firstVal != 0 && firstVal != gridSize && clue == 1;
    }

    public Constraint copy(Sudoku sudoku) {
        return new SkyscraperConstraint(cells,
                Arrays.copyOf(topClues, gridSize),
                Arrays.copyOf(bottomClues, gridSize),
                Arrays.copyOf(leftClues, gridSize),
                Arrays.copyOf(rightClues, gridSize));
    }
}
