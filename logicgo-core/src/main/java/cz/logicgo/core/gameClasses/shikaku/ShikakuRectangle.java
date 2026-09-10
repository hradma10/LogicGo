package cz.logicgo.core.gameClasses.shikaku;

import java.util.Objects;

public class ShikakuRectangle {
    private final int id;
    private final int minRow;
    private final int maxRow;
    private final int minCol;
    private final int maxCol;
    private boolean hintWrong = false;

    public ShikakuRectangle(int id, ShikakuCell start, ShikakuCell end) {
        this.id = id;
        this.minRow = Math.min(start.getRow(), end.getRow());
        this.maxRow = Math.max(start.getRow(), end.getRow());
        this.minCol = Math.min(start.getCol(), end.getCol());
        this.maxCol = Math.max(start.getCol(), end.getCol());
    }

    public ShikakuRectangle(ShikakuRectangle rectangle) {
        this.id = rectangle.getId();
        this.minRow = rectangle.getMinRow();
        this.maxRow = rectangle.getMaxRow();
        this.minCol = rectangle.getMinCol();
        this.maxCol = rectangle.getMaxCol();
    }

    public ShikakuRectangle(int id, int minRow, int maxRow, int minCol, int maxCol) {
        this.id = id;
        this.minRow = minRow;
        this.maxRow = maxRow;
        this.minCol = minCol;
        this.maxCol = maxCol;
    }

    public boolean isHintWrong() {
        return hintWrong;
    }

    public void setHintWrong(boolean hintWrong) {
        this.hintWrong = hintWrong;
    }

    public boolean contains(ShikakuCell cell) {
        return cell.getRow() >= minRow && cell.getRow() <= maxRow &&
                cell.getCol() >= minCol && cell.getCol() <= maxCol;
    }

    public boolean intersects(ShikakuRectangle other) {
        return this.minRow <= other.maxRow && this.maxRow >= other.minRow &&
                this.minCol <= other.maxCol && this.maxCol >= other.minCol;
    }

    public int getArea() {
        return (maxRow - minRow + 1) * (maxCol - minCol + 1);
    }

    public int getMinRow() {
        return minRow;
    }

    public int getMaxRow() {
        return maxRow;
    }

    public int getMinCol() {
        return minCol;
    }

    public int getMaxCol() {
        return maxCol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShikakuRectangle that)) return false;
        return minRow == that.minRow && maxRow == that.maxRow &&
                minCol == that.minCol && maxCol == that.maxCol;
    }

    @Override
    public String toString() {
        return "ShikakuRectangle{" +
                "id=" + id +
                ", minRow=" + minRow +
                ", maxRow=" + maxRow +
                ", minCol=" + minCol +
                ", maxCol=" + maxCol +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(minRow, maxRow, minCol, maxCol);
    }

    public int getId() {
        return id;
    }


}
