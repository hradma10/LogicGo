package cz.logicgo.engine.algorithms.sudoku.validators.constraints;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.Constraint;

public class XSumsConstraint implements Constraint {

    private final SudokuCell[][] cells;
    private final int[] top, bottom, left, right;
    private final int size;

    public XSumsConstraint(SudokuCell[][] cells, int[] top, int[] bottom, int[] left, int[] right) {
        this.cells = cells;
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        this.size = cells.length;
    }

    @Override
    public boolean isViolated(int row, int col, int num) {
        int originalValue = cells[row][col].getValue();
        cells[row][col].setValue(num);

        boolean isViolated = checkLine(row, left[row], true, true) ||
                checkLine(row, right[row], true, false) ||
                checkLine(col, top[col], false, true) ||
                checkLine(col, bottom[col], false, false);

        cells[row][col].setValue(originalValue);
        return isViolated;
    }

    private boolean checkLine(int index, int targetSum, boolean isRow, boolean isStart) {
        if (targetSum == 0) return false;

        SudokuCell firstCell = isRow
                ? (isStart ? cells[index][0] : cells[index][size - 1])
                : (isStart ? cells[0][index] : cells[size - 1][index]);

        int x = firstCell.getValue();
        if (x == 0) return false;

        int currentSum = 0;
        boolean hasEmpty = false;

        for (int i = 0; i < x; i++) {
            int r = isRow ? index : (isStart ? i : size - 1 - i);
            int c = isRow ? (isStart ? i : size - 1 - i) : index;

            int val = cells[r][c].getValue();
            if (val == 0) {
                hasEmpty = true;
            } else {
                currentSum += val;
            }
        }

        if (hasEmpty) {
            return currentSum >= targetSum;
        } else {
            return currentSum != targetSum;
        }
    }

    @Override
    public Constraint copy(Sudoku sudoku) {
        var mods = sudoku.getModifiers().getXSums();
        return new XSumsConstraint(sudoku.getBoard(), mods.top(), mods.bottom(), mods.left(), mods.right());
    }
}
