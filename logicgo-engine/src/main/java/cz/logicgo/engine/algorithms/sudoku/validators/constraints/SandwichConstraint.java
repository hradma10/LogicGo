package cz.logicgo.engine.algorithms.sudoku.validators.constraints;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.Constraint;

public class SandwichConstraint implements Constraint {

    private final SudokuCell[][] cells;
    private final int gridSize;

    private final int[] topClues;
    private final int[] leftClues;

    public SandwichConstraint(SudokuCell[][] cells, int[] topClues, int[] leftClues) {
        this.cells = cells;
        this.gridSize = cells.length;
        this.topClues = topClues;
        this.leftClues = leftClues;
    }

    @Override
    public boolean isViolated(int row, int col, int num) {
        int currentValue = (num != 0) ? num : cells[row][col].getValue();
        if (currentValue == 0) return false;


        if (leftClues != null && leftClues[row] >= 0) {
            if (isLineViolated(row, col, currentValue, row, 0, 0, 1, leftClues[row])) {
                return true;
            }
        }

        if (topClues != null && topClues[col] >= 0) {
            return isLineViolated(row, col, currentValue, 0, col, 1, 0, topClues[col]);
        }

        return false;
    }

    private boolean isLineViolated(int row, int col, int checkedValue, int rStart, int cStart, int rStep, int cStep, int clue) {
        int pos1 = -1;
        int posMax = -1;
        int[] lineValues = new int[gridSize];

        int r = rStart;
        int c = cStart;

        for (int i = 0; i < gridSize; i++) {
            int val = (r == row && c == col) ? checkedValue : cells[r][c].getValue();
            lineValues[i] = val;

            if (val == 1) {
                pos1 = i;
            } else if (val == gridSize) {
                posMax = i;
            }

            r += rStep;
            c += cStep;
        }

        if (pos1 != -1 && posMax != -1) {
            int start = Math.min(pos1, posMax) + 1;
            int end = Math.max(pos1, posMax);

            int currentSum = 0;
            int emptyCount = 0;

            for (int i = start; i < end; i++) {
                if (lineValues[i] == 0) {
                    emptyCount++;
                } else {
                    currentSum += lineValues[i];
                }
            }

            if (currentSum > clue) {
                return true;
            }

            return emptyCount == 0 && currentSum != clue;
        }

        return false;
    }

    public Constraint copy(Sudoku sudoku) {
        return new SandwichConstraint(sudoku.getBoard(), topClues, leftClues);
    }
}
