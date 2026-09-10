package cz.logicgo.engine.algorithms.sudoku.validators.constraints;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.core.misc.GridCell;
import cz.logicgo.core.misc.Pair;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.Constraint;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConsecutiveConstraint implements Constraint {

    private final SudokuCell[][] cells;
    private final Set<String> markedEdges;

    final static int[][] deltas = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    public ConsecutiveConstraint(SudokuCell[][] cells, List<Pair<GridCell, GridCell>> pairs) {
        this.cells = cells;
        this.markedEdges = new HashSet<>();

        for (Pair<GridCell, GridCell> p : pairs) {
            markedEdges.add(getEdgeKey(p.getFirst(), p.getSecond()));
        }
    }

    private String getEdgeKey(int r1, int c1, int r2, int c2) {
        if (r1 < r2 || (r1 == r2 && c1 < c2)) {
            return r1 + "." + c1 + "-" + r2 + "." + c2;
        } else {
            return r2 + "." + c2 + "-" + r1 + "." + c1;
        }
    }

    private String getEdgeKey(GridCell c1, GridCell c2) {
        return getEdgeKey(c1.row(), c1.col(), c2.row(), c2.col());
    }

    @Override
    public boolean isViolated(int row, int col, int num) {
        SudokuCell sudokuCell = cells[row][col];
        int value = (num != 0) ? num : sudokuCell.getValue();

        if (value == 0) return false;

        for (int[] d : deltas) {
            int r = row + d[0];
            int c = col + d[1];

            if (r < 0 || r >= cells.length || c < 0 || c >= cells.length) continue;

            SudokuCell neighbour = cells[r][c];
            int neighbourValue = neighbour.getValue();

            if (neighbourValue == 0) continue;

            String key = getEdgeKey(row, col, r, c);
            boolean isMarked = markedEdges.contains(key);

            int diff = Math.abs(value - neighbourValue);

            if (isMarked) {
                if (diff != 1) return true;
            } else {
                if (diff == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void set(int row, int col, int num) {
    }

    @Override
    public void unset(int row, int col, int num) {
    }

    public Constraint copy(Sudoku sudoku) {
        return new ConsecutiveConstraint(sudoku.getBoard(), sudoku.getModifiers().getConsecutive().cells());
    }
}
