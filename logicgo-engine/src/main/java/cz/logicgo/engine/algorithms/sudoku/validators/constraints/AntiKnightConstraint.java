package cz.logicgo.engine.algorithms.sudoku.validators.constraints;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.Constraint;

import static cz.logicgo.engine.algorithms.sudoku.validators.ValidatorUtils.checkMoves;

public class AntiKnightConstraint implements Constraint {

    private final SudokuCell[][] board;
    private static final int[][] KNIGHT_MOVES = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
            {1, -2}, {1, 2}, {2, -1}, {2, 1}
    };

    public AntiKnightConstraint(SudokuCell[][] board) {
        this.board = board;
    }

    @Override
    public boolean isViolated(int row, int col, int num) {
        return checkMoves(row, col, num, board, KNIGHT_MOVES);
    }

    public Constraint copy(Sudoku sudoku) {
        return new AntiKnightConstraint(sudoku.getBoard());
    }
}
