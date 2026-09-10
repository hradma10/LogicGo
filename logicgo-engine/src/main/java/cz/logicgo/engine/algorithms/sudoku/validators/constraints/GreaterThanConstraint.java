package cz.logicgo.engine.algorithms.sudoku.validators.constraints;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.CompType;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.Constraint;

public class GreaterThanConstraint implements Constraint {

    private final SudokuCell[][] cells;
    private final CompType[][] horizontalConstraints;
    private final CompType[][] verticalConstraints;
    private final int gridSize;

    public GreaterThanConstraint(SudokuCell[][] cells, CompType[][] horizontal, CompType[][] vertical) {
        this.cells = cells;
        this.gridSize = cells.length;
        this.horizontalConstraints = horizontal;
        this.verticalConstraints = vertical;
    }

    @Override
    public boolean isViolated(int row, int col, int num) {
        int val = (num != 0) ? num : cells[row][col].getValue();

        if (val == 0) return false;

        if (horizontalConstraints != null && col < gridSize - 1) {
            CompType type = horizontalConstraints[row][col];
            if (type != null) {
                int rightVal = cells[row][col + 1].getValue();
                if (rightVal != 0) {
                    if (type == CompType.BIGGER && val <= rightVal) return true;
                    if (type == CompType.SMALLER && val >= rightVal) return true;
                }
            }
        }

        if (horizontalConstraints != null && col > 0) {
            CompType type = horizontalConstraints[row][col - 1];
            if (type != null) {
                int leftVal = cells[row][col - 1].getValue();
                if (leftVal != 0) {
                    if (type == CompType.BIGGER && leftVal <= val) return true;
                    if (type == CompType.SMALLER && leftVal >= val) return true;
                }
            }
        }

        if (verticalConstraints != null && row < gridSize - 1) {
            CompType type = verticalConstraints[row][col];
            if (type != null) {
                int bottomVal = cells[row + 1][col].getValue();
                if (bottomVal != 0) {
                    if (type == CompType.BIGGER && val <= bottomVal) return true;
                    if (type == CompType.SMALLER && val >= bottomVal) return true;
                }
            }
        }

        if (verticalConstraints != null && row > 0) {
            CompType type = verticalConstraints[row - 1][col];
            if (type != null) {
                int topVal = cells[row - 1][col].getValue();
                if (topVal != 0) {
                    if (type == CompType.BIGGER && topVal <= val) return true;
                    return type == CompType.SMALLER && topVal >= val;
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
        return new GreaterThanConstraint(sudoku.getBoard(), this.horizontalConstraints, this.verticalConstraints);
    }
}
