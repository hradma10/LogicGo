package cz.logicgo.core.gameClasses.shikaku;


import cz.logicgo.core.misc.interfaces.Positioned;

import java.io.Serializable;
import java.util.Objects;

public class ShikakuCell implements Serializable, Positioned {
    private final int row;
    private final int col;
    private int clue;
    private int regionId;

    public ShikakuCell(int row, int col, int clue, int regionId) {
        this.row = row;
        this.col = col;
        this.clue = clue;
        this.regionId = regionId;
    }

    public ShikakuCell(int row, int col) {
        this.row = row;
        this.col = col;
        this.clue = 0;
        this.regionId = -1;
    }

    public ShikakuCell(ShikakuCell shikakuCell) {
        this.row = shikakuCell.getRow();
        this.col = shikakuCell.getCol();
        this.clue = shikakuCell.getClue();
        this.regionId = shikakuCell.getRegionId();
    }

    public Integer getRow() {
        return row;
    }

    public Integer getCol() {
        return col;
    }

    public int getClue() {
        return clue;
    }

    public void setClue(int clue) {
        this.clue = clue;
    }

    public int getRegionId() {
        return regionId;
    }

    public void setRegionId(int regionId) {
        this.regionId = regionId;
    }

    public boolean hasClue() {
        return clue > 0;
    }

    public boolean isAssigned() {
        return regionId > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShikakuCell that)) return false;
        return row == that.row &&
                col == that.col &&
                clue == that.clue &&
                regionId == that.regionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col, clue, regionId);
    }
}
