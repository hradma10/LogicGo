package cz.logicgo.engine.algorithms.sudoku;

import java.util.Objects;

public final class TargetedCell {
    private final int row;
    private final int col;
    private int val;

    public TargetedCell(int row, int col, int val) {
        this.row = row;
        this.col = col;
        this.val = val;
    }

    public TargetedCell(int row, int col) {
        this.row = row;
        this.col = col;
        this.val = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TargetedCell that)) return false;
        return getRow() == that.getRow() && getCol() == that.getCol() && getVal() == that.getVal();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRow(), getCol(), getVal());
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return "TargetedCell[" +
                "row=" + row + ", " +
                "col=" + col + ", " +
                "val=" + val + ']';
    }

}
