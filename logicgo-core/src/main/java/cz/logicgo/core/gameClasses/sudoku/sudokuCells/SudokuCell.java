package cz.logicgo.core.gameClasses.sudoku.sudokuCells;


import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.core.misc.interfaces.Positioned;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class SudokuCell implements Serializable, Positioned {

    private final int row;
    private final int col;
    private final List<Integer> candidates = new ArrayList<>();
    private int value;
    private boolean staticCell;
    private boolean changeable = true;
    private boolean hints = false;
    private boolean forPrint = false;
    private transient String backgroundColor = "#FFFFFF";
    private SudokuVariant variant;
    private SudokuSize sudokuSize;
    private SudokuPatternLayout layout;
    private boolean conflicting;
    private boolean showCandidates;

    private transient boolean selected = false;
    private transient boolean drawToPrimary = true;

    public SudokuCell(int row, int col, int value, boolean staticCell) {
        this.row = row;
        this.col = col;
        this.value = value;
        this.staticCell = staticCell;
        this.layout = SudokuPatternLayout.createEmptyPattern(SudokuSize.NINE.getGridSize());
    }

    public SudokuCell(int row, int col, int value, boolean staticCell, List<Integer> candidates) {
        this.row = row;
        this.col = col;
        this.value = value;
        this.staticCell = staticCell;
        this.candidates.addAll(candidates);
        this.layout = SudokuPatternLayout.createEmptyPattern(SudokuSize.NINE.getGridSize());
    }

    public SudokuCell(int row, int col, int value) {
        this(row, col, value, false);
    }

    public SudokuCell(int row, int col, boolean staticCell) {
        this(row, col, 0, staticCell);
    }

    public SudokuCell(int row, int col) {
        this(row, col, 0);
    }

    public SudokuCell(SudokuCell cell) {
        this(cell.row, cell.col, cell.value, cell.staticCell, cell.candidates);
        this.sudokuSize = cell.getSudokuSize();
        this.variant = cell.getVariant();
        this.changeable = cell.isChangeable();
        this.hints = cell.isHints();
        this.forPrint = cell.isForPrint();
        this.showCandidates = cell.isShowCandidates();
    }


    public static void copyBoardProperties(SudokuCell[][] source, SudokuCell[][] target) {
        if (source == null || target == null) return;

        int rows = source.length;
        int cols = source[0].length;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                SudokuCell sourceCell = source[r][c];
                SudokuCell targetCell = target[r][c];

                if (sourceCell != null && targetCell != null) {
                    targetCell.setStaticCell(sourceCell.isStaticCell());
                    targetCell.setChangeable(sourceCell.isChangeable());
                    targetCell.setHints(sourceCell.isHints());
                    targetCell.setForPrint(sourceCell.isForPrint());
                    targetCell.setSudokuType(sourceCell.getSudokuType());
                    targetCell.setSelected(sourceCell.isSelected());
                    targetCell.setDrawToPrimary(sourceCell.isDrawToPrimary());
                }
            }
        }
    }

    public SudokuCell deepCopy() {
        SudokuCell newCell = new SudokuCell(this.getRow(), this.getCol(), this.getValue(), this.isStaticCell());
        return deepCopyProperties(newCell);
    }

    protected SudokuCell deepCopyProperties(SudokuCell newCell) {
        newCell.setCandidates(new ArrayList<>(this.candidates));
        newCell.setChangeable(this.isChangeable());
        newCell.setHints(this.isHints());
        newCell.setForPrint(this.isForPrint());
        newCell.setBackgroundColor(this.getBackgroundColor());
        newCell.setSudokuType(this.getSudokuType());
        newCell.setVariant(this.getVariant());
        newCell.setPattern(this.getLayout());
        newCell.setSelected(this.isSelected());
        newCell.setDrawToPrimary(this.isDrawToPrimary());
        return newCell;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Integer getCol() {
        return col;
    }

    public Integer getRow() {
        return row;
    }

    public boolean isStaticCell() {
        return staticCell;
    }

    public void setStaticCell(boolean staticCell) {
        this.staticCell = staticCell;
    }

    public List<Integer> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Integer> candidates) {
        this.candidates.clear();
        this.candidates.addAll(candidates);
    }

    public boolean isChangeable() {
        return changeable;
    }

    public void setChangeable(boolean changeable) {
        this.changeable = changeable;
    }

    public boolean isHints() {
        return hints;
    }

    public void setHints(boolean hints) {
        this.hints = hints;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public boolean isForPrint() {
        return forPrint;
    }

    public void setForPrint(boolean forPrint) {
        this.forPrint = forPrint;
    }

    public SudokuSize getSudokuType() {
        return sudokuSize;
    }

    public void setSudokuType(SudokuSize sudokuSize) {
        this.sudokuSize = sudokuSize;
    }

    public SudokuVariant getVariant() {
        return variant;
    }

    public void setVariant(SudokuVariant variant) {
        this.variant = variant;
    }

    public SudokuSize getSudokuSize() {
        return sudokuSize;
    }

    public void setSudokuSize(SudokuSize sudokuSize) {
        this.sudokuSize = sudokuSize;
    }

    public SudokuPatternLayout getLayout() {
        return layout;
    }

    public SudokuCell setPattern(SudokuPatternLayout layout) {
        this.layout = layout;
        return this;
    }


    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isDrawToPrimary() {
        return drawToPrimary;
    }

    public void setDrawToPrimary(boolean drawToPrimary) {
        this.drawToPrimary = drawToPrimary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SudokuCell cell)) return false;
        return getRow() == cell.getRow() && getCol() == cell.getCol() && getValue() == cell.getValue();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRow(), getCol(), getValue());
    }

    @Override
    public String toString() {
        return "SudokuCell{" +
                "row=" + row +
                ", col=" + col +
                ", value=" + value +
                '}';
    }

    public boolean isConflicting() {
        return conflicting;
    }

    public SudokuCell setConflicting(boolean conflicting) {
        this.conflicting = conflicting;
        return this;
    }

    public boolean isShowCandidates() {
        return showCandidates;
    }

    public SudokuCell setShowCandidates(boolean showCandidates) {
        this.showCandidates = showCandidates;
        return this;
    }
}
