package cz.logicgo.engine.algorithms.sudoku.validators.constraints;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.core.misc.GridCell;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.Constraint;

import java.util.HashMap;
import java.util.List;

import static cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifierRecords.GroupSumMark;

public class GroupSumsConstraint implements Constraint {

    private final SudokuCell[][] board;
    private final List<GroupSumMark> marks;
    private final HashMap<GridCell, Integer> markLookup;

    public GroupSumsConstraint(SudokuCell[][] board, List<GroupSumMark> marks) {
        this.board = board;
        this.marks = marks;
        this.markLookup = new HashMap<>();
        if (marks != null) {
            for (GroupSumMark mark : marks) {
                markLookup.put(mark.topLeft(), mark.targetSum());
            }
        }
    }

    @Override
    public boolean isViolated(int row, int col, int num) {
        if (marks == null || marks.isEmpty()) return false;
        GridCell[] possibleIntersections = {
                new GridCell(row - 1, col - 1),
                new GridCell(row - 1, col),
                new GridCell(row, col - 1),
                new GridCell(row, col)
        };

        for (GridCell intersection : possibleIntersections) {
            Integer targetSum = markLookup.get(intersection);
            if (targetSum == null) continue;

            int sum = 0;
            int emptyCount = 0;

            for (int r = intersection.row(); r <= intersection.row() + 1; r++) {
                for (int c = intersection.col(); c <= intersection.col() + 1; c++) {
                    int val = (r == row && c == col) ? num : board[r][c].getValue();
                    if (val == 0) {
                        emptyCount++;
                    } else {
                        sum += val;
                    }
                }
            }

            if (sum > targetSum) return true;
            if (emptyCount == 0 && sum != targetSum) return true;
        }

        return false;
    }

    public List<GroupSumMark> getMarks() {
        return marks;
    }

    @Override
    public Constraint copy(Sudoku sudoku) {
        return new GroupSumsConstraint(sudoku.getBoard(), marks);
    }
}
