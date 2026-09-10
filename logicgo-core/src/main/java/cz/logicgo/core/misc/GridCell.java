package cz.logicgo.core.misc;


import cz.logicgo.core.misc.interfaces.Positioned;

import java.util.Objects;

public record GridCell(int row, int col) {

    public GridCell(int[] pair) {
        this(pair[0], pair[1]);
    }

    public GridCell(Positioned cell) {
        this(cell.getRow(), cell.getCol());
    }

    public GridCell(GridCell other) {
        this(other.row(), other.col());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GridCell(int row1, int col1))) return false;
        return row() == row1 && col() == col1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row(), col());
    }
}
