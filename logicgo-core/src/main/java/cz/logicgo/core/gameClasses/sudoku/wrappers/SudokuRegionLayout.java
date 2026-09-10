package cz.logicgo.core.gameClasses.sudoku.wrappers;

import java.util.Objects;

public class SudokuRegionLayout extends AbstractGridLayout {
    private int type;
    final static private int CUSTOM_TYPE_ID = 127;

    public SudokuRegionLayout() {

    }

    public SudokuRegionLayout(Integer[][] grid) {
        super("custom", "custom", false, grid);
        this.type = CUSTOM_TYPE_ID;
    }

    public void setRegions(Integer[][] regions) {
        super.setGrid(regions);
    }

    public Integer[][] getRegions() {
        return super.getGrid();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SudokuRegionLayout that)) return false;
        return getType() == that.getType() && Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    public static int getCUSTOM_TYPE_ID() {
        return CUSTOM_TYPE_ID;
    }
}
