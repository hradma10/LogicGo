package cz.logicgo.engine.algorithms.sudoku.validators.constraints;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifierRecords.MarkType;
import cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifierRecords.XVPair;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.Constraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class XVConstraint implements Constraint {

    private final SudokuCell[][] board;
    private final HashMap<SudokuCell, List<XVPair>> marksToCell;

    public XVConstraint(List<XVPair> xvMarks, SudokuCell[][] board) {
        this.board = board;
        this.marksToCell = new HashMap<>();

        for (XVPair mark : xvMarks) {
            SudokuCell cell1 = board[mark.first().row()][mark.first().col()];
            SudokuCell cell2 = board[mark.second().row()][mark.second().col()];

            marksToCell.computeIfAbsent(cell1, k -> new ArrayList<>()).add(mark);
            marksToCell.computeIfAbsent(cell2, k -> new ArrayList<>()).add(mark);
        }
    }

    @Override
    public boolean isViolated(int row, int col, int num) {
        List<XVPair> marks = marksToCell.get(board[row][col]);

        if (marks != null) {
            for (XVPair mark : marks) {
                int otherR = (mark.first().row() == row && mark.first().col() == col) ? mark.second().row() : mark.first().row();
                int otherC = (mark.first().col() == col && mark.first().row() == row) ? mark.second().col() : mark.first().col();

                int otherVal = board[otherR][otherC].getValue();
                if (otherVal == 0) continue;

                int sum = num + otherVal;
                if (mark.markType() == MarkType.X && sum != 10) return true;
                if (mark.markType() == MarkType.V && sum != 5) return true;
            }
        }

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : directions) {
            int nR = row + dir[0];
            int nC = col + dir[1];

            if (nR >= 0 && nR < board.length && nC >= 0 && nC < board[0].length) {
                int neighborVal = board[nR][nC].getValue();
                if (neighborVal == 0) continue;

                if (!hasMarkBetween(row, col, nR, nC, marks)) {
                    int sum = num + neighborVal;
                    if (sum == 5 || sum == 10) return true;
                }
            }
        }
        return false;
    }

    private boolean hasMarkBetween(int r1, int c1, int r2, int c2, List<XVPair> marks) {
        if (marks == null) return false;
        for (XVPair m : marks) {
            if ((m.first().row() == r1 && m.first().col() == c1 && m.second().row() == r2 && m.second().col() == c2) ||
                    (m.first().row() == r2 && m.first().col() == c2 && m.second().row() == r1 && m.second().col() == c1)) {
                return true;
            }
        }
        return false;
    }

    public Constraint copy(Sudoku sudoku) {
        return new XVConstraint(sudoku.getModifiers().getXv().marks(), sudoku.getBoard());
    }
}
