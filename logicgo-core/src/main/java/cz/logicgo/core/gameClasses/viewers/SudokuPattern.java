package cz.logicgo.core.gameClasses.viewers;


import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;

import java.util.Objects;

public class SudokuPattern {

    private SudokuPatternLayout layout;
    private boolean created;

    public SudokuPatternLayout getLayout() {
        return layout;
    }

    public void setLayout(SudokuPatternLayout layout) {
        this.layout = layout;
    }

    public boolean isCreated() {
        return created;
    }

    public void setCreated(boolean created) {
        this.created = created;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SudokuPattern that)) return false;
        return Objects.equals(layout, that.layout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(layout);
    }
}
