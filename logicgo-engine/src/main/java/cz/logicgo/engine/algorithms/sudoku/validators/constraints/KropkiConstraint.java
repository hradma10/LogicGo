package cz.logicgo.engine.algorithms.sudoku.validators.constraints;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.core.misc.GridCell;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.Constraint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifierRecords.DotColor;
import static cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifierRecords.KropkiDot;

public class KropkiConstraint implements Constraint {

    private final SudokuCell[][] cells;
    private final Map<String, DotColor> markedEdges;

    private final static int[][] deltas = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    public KropkiConstraint(SudokuCell[][] cells, List<KropkiDot> dots) {
        this.cells = cells;
        this.markedEdges = new HashMap<>();

        if (dots != null) {
            for (KropkiDot dot : dots) {
                markedEdges.put(getEdgeKey(dot.first(), dot.second()), dot.color());
            }
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

            String key = getEdgeKey(row, col, r, c);
            DotColor dotColor = markedEdges.get(key);

            if (neighbourValue != 0) {
                boolean isConsecutive = Math.abs(value - neighbourValue) == 1;
                boolean isRatio = (value * 2 == neighbourValue) || (neighbourValue * 2 == value);

                if (dotColor != null) {
                    if (dotColor == DotColor.WHITE && !isConsecutive) return true;
                    if (dotColor == DotColor.BLACK && !isRatio) return true;
                } else {
                    if (isConsecutive || isRatio) return true;
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

    @Override
    public Constraint copy(Sudoku sudoku) {
        return new KropkiConstraint(sudoku.getBoard(), sudoku.getModifiers().getKropki().dots());
    }
}
