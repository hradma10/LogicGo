package cz.logicgo.engine.algorithms.sudoku.validators.constraints;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.Constraint;

public class AntiConsecutiveConstraint implements Constraint {
    final static int[][] deltas = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private final SudokuCell[][] board;

    public AntiConsecutiveConstraint(SudokuCell[][] board) {
        this.board = board;
    }

    @Override
    public boolean isViolated(int row, int col, int num) {
        int val = (num != 0) ? num : board[row][col].getValue();

        int size = board.length;
        if (val == 0) return false;

        for (int[] d : deltas) {
            int r = row + d[0];
            int c = col + d[1];

            if (r < 0 || r >= size || c < 0 || c >= size) continue;

            int otherValue = board[r][c].getValue();

            if (otherValue != 0) {
                if (val + 1 == otherValue || val - 1 == otherValue) return true;
            }
        }
        return false;
    }

    public Constraint copy(Sudoku sudoku) {
        return new AntiConsecutiveConstraint(sudoku.getBoard());
    }
}
