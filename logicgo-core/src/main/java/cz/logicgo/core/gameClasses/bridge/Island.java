package cz.logicgo.core.gameClasses.bridge;


import cz.logicgo.core.misc.interfaces.Positioned;

import java.awt.*;
import java.io.Serializable;
import java.util.Objects;

public class Island implements Serializable, Positioned, BridgeElement {

    private int id;
    private int row;
    private int col;
    private int bridgeCount;

    private BoardSize boardSize;
    private transient String islandColor = null;

    private transient boolean selected = false;
    private transient boolean drawToPrimary = true;


    public Island(int id, int row, int col, int bridgeCount, BoardSize boardSize) {
        this.id = id;
        this.row = row;
        this.col = col;
        this.bridgeCount = bridgeCount;
        this.boardSize = boardSize;
    }

    public Island(Island other) {
        this.id = other.id;
        this.row = other.row;
        this.col = other.col;
        this.bridgeCount = other.bridgeCount;
        this.boardSize = other.boardSize;
        this.islandColor = other.islandColor;
        this.selected = other.selected;
        this.drawToPrimary = other.drawToPrimary;
    }

    public Island(int id, int row, int col, BoardSize boardSize) {
        this(id, row, col, 0, boardSize);
    }

    public Island(int id) {
        this.id = id;
    }

    public int increaseBridgeCount() {
        return ++bridgeCount;
    }

    public int decreaseBridgeCount() {
        return --bridgeCount;
    }

    public int getId() {
        return id;
    }

    public Island setId(int id) {
        this.id = id;
        return this;
    }

    @Override
    public Integer getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    @Override
    public Integer getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getBridgeCount() {
        return bridgeCount;
    }

    public void setBridgeCount(int bridgeCount) {
        this.bridgeCount = bridgeCount;
    }

    public BoardSize getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(BoardSize boardSize) {
        this.boardSize = boardSize;
    }

    public String getIslandColor() {
        return islandColor;
    }

    public Island setIslandColor(String islandColor) {
        this.islandColor = islandColor;
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
        if (!(o instanceof Island island)) return false;
        return id == island.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Island{" + "id=" + id + ", r=" + row + ", c=" + col + ", count=" + bridgeCount + '}';
    }
}
