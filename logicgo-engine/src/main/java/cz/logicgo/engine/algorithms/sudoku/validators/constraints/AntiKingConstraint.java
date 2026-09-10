package cz.logicgo.engine.algorithms.sudoku.validators.constraints;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.Constraint;

import static cz.logicgo.engine.algorithms.sudoku.validators.ValidatorUtils.checkMoves;

public class AntiKingConstraint implements Constraint {

    private final SudokuCell[][] board;
    private static final int[][] KING_DIAGONALS = {
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
    };

    public AntiKingConstraint(SudokuCell[][] board) {
        this.board = board;
    }

    @Override
    public boolean isViolated(int row, int col, int num) {
        return checkMoves(row, col, num, board, KING_DIAGONALS);
    }

    public Constraint copy(Sudoku sudoku) {
        return new AntiKingConstraint(sudoku.getBoard());
    }
}
