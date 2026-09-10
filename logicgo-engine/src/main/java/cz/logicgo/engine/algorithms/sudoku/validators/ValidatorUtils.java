package cz.logicgo.engine.algorithms.sudoku.validators;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;

import java.util.HashSet;
import java.util.Set;

public class ValidatorUtils {

    public static boolean checkMoves(int row, int col, int num, SudokuCell[][] board, int[][] moves) {
        if (num == 0) return false;

        int size = board.length;

        for (int[] move : moves) {
            int nRow = row + move[0];
            int nCol = col + move[1];

            if (nRow >= 0 && nRow < size && nCol >= 0 && nCol < size) {
                if (board[nRow][nCol].getValue() == num) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Set<SudokuCell> getConflictingCells(Sudoku sudoku, ISudokuValidator validator) {
        Set<SudokuCell> conflictingSet = new HashSet<>();
        SudokuCell[][] board = sudoku.getBoard();
        int size = board.length;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                SudokuCell cell = board[r][c];

                if (cell != null && cell.getValue() != 0 && cell.isChangeable()) {
                    int val = cell.getValue();

                    validator.unset(r, c, val);

                    if (validator.exists(r, c, val)) {
                        conflictingSet.add(cell);
                    }

                    validator.update(r, c, val);
                }
            }
        }

        return conflictingSet;
    }


    public static boolean[][] getConflictingCellsMatrix(Sudoku sudoku, ISudokuValidator validator) {
        SudokuCell[][] board = sudoku.getBoard();
        int size = board.length;

        boolean[][] conflicts = new boolean[size][size];

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                SudokuCell cell = board[r][c];

                if (cell != null && cell.getValue() != 0 && cell.isChangeable()) {
                    int val = cell.getValue();

                    validator.unset(r, c, val);

                    if (validator.exists(r, c, val)) {
                        conflicts[r][c] = true;
                    }

                    validator.update(r, c, val);
                }
            }
        }

        return conflicts;
    }
}
