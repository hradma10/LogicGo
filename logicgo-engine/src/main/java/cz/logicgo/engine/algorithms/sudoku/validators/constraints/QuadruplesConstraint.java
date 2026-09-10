package cz.logicgo.engine.algorithms.sudoku.validators.constraints;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.core.misc.GridCell;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.Constraint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifierRecords.QuadrupleMark;

public class QuadruplesConstraint implements Constraint {

    private final SudokuCell[][] board;
    private final List<QuadrupleMark> marks;
    private final HashMap<GridCell, List<Integer>> markLookup;

    public QuadruplesConstraint(SudokuCell[][] board, List<QuadrupleMark> marks) {
        this.board = board;
        this.marks = marks;
        this.markLookup = new HashMap<>();
        if (marks != null) {
            for (QuadrupleMark mark : marks) {
                markLookup.put(mark.topLeft(), mark.values());
            }
        }
    }

    public List<QuadrupleMark> getMarks() {
        return marks;
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
            List<Integer> requiredVals = markLookup.get(intersection);
            if (requiredVals == null) continue;

            int emptyCount = 0;
            Map<Integer, Integer> placedCounts = new HashMap<>();
            Map<Integer, Integer> requiredCounts = new HashMap<>();

            for (Integer req : requiredVals) {
                requiredCounts.put(req, requiredCounts.getOrDefault(req, 0) + 1);
            }

            for (int r = intersection.row(); r <= intersection.row() + 1; r++) {
                for (int c = intersection.col(); c <= intersection.col() + 1; c++) {
                    int val = (r == row && c == col) ? num : board[r][c].getValue();
                    if (val == 0) {
                        emptyCount++;
                    } else {
                        placedCounts.put(val, placedCounts.getOrDefault(val, 0) + 1);
                    }
                }
            }
            int missingCount = 0;
            for (Map.Entry<Integer, Integer> entry : requiredCounts.entrySet()) {
                int reqVal = entry.getKey();
                int reqQty = entry.getValue();
                int placedQty = placedCounts.getOrDefault(reqVal, 0);

                if (reqQty > placedQty) {
                    missingCount += (reqQty - placedQty);
                }
            }
            if (missingCount > emptyCount) return true;
        }

        return false;
    }

    @Override
    public Constraint copy(Sudoku sudoku) {
        return new QuadruplesConstraint(sudoku.getBoard(), marks);
    }
}
