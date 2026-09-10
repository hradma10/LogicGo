package cz.logicgo.engine.algorithms.sudoku.custom;


import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.engine.algorithms.sudoku.SudokuGame;

import java.util.HashSet;
import java.util.Set;

import static cz.logicgo.core.gameClasses.sudoku.SudokuUtils.getModifierCells;


public class SudokuConflictingCells {

    public static boolean[][] getConflictingCellsMatrix(SudokuGame sudokuGame) {
        var sudoku = sudokuGame.getSudoku();
        SudokuCell[][] board = sudoku.getBoard();
        int size = board.length;
        boolean[][] conflicts = new boolean[size][size];

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                SudokuCell cell = board[r][c];
                if (cell == null || cell.getValue() == 0 || !cell.isChangeable()) continue;

                int val = cell.getValue();

                Set<SudokuCell> relatedCells = new HashSet<>();

                for (int i = 0; i < size; i++) {
                    if (i != c && board[r][i] != null) relatedCells.add(board[r][i]);
                    if (i != r && board[i][c] != null) relatedCells.add(board[i][c]);
                }

                int myBoxId = sudokuGame.getBoxIdForCell(r, c);
                for (int r2 = 0; r2 < size; r2++) {
                    for (int c2 = 0; c2 < size; c2++) {
                        if ((r2 != r || c2 != c) && board[r2][c2] != null) {
                            if (sudokuGame.getBoxIdForCell(r2, c2) == myBoxId) {
                                relatedCells.add(board[r2][c2]);
                            }
                        }
                    }
                }

                if (sudoku.getModifiers() != null && sudoku.getModifiers().getActiveModifiers() != null) {
                    Set<SudokuCell> modifierCells = getModifierCells(sudoku, cell, sudoku.getModifiers().getActiveModifiers());
                    relatedCells.addAll(modifierCells);
                }

                for (SudokuCell related : relatedCells) {
                    if (related.getRow() == r && related.getCol() == c) continue;

                    if (related.getValue() == val) {
                        conflicts[r][c] = true;
                        conflicts[related.getRow()][related.getCol()] = true;
                    }
                }
            }
        }

        return conflicts;
    }
}
