package cz.logicgo.core.gameClasses.sudoku.wrappers;


import cz.logicgo.core.misc.GridCell;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class AbstractGridLayout implements Serializable {
    protected String name;
    protected String translatedName;
    protected boolean isSelective = false;

    protected Integer[][] grid;

    public AbstractGridLayout() {
    }

    public AbstractGridLayout(String name, String translatedName, boolean isSelective, Integer[][] grid) {
        this.name = name;
        this.translatedName = translatedName;
        this.isSelective = isSelective;
        this.grid = grid;
    }

    public Integer[][] getGrid() {
        return grid;
    }

    public void setGrid(Integer[][] grid) {
        this.grid = grid;
    }

    public ArrayList<GridCell> getCellsBelongToSubgrid(int index) {
        if (isSelective && index == 0) {
            throw new IndexOutOfBoundsException("Cannot get cells for index 0 in a selective layout");
        }

        var list = new ArrayList<GridCell>();

        if (grid == null) return list;

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] != null && grid[i][j] == index) {
                    list.add(new GridCell(i, j));
                }
            }
        }
        return list;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTranslatedName() {
        return translatedName;
    }

    public void setTranslatedName(String translatedName) {
        this.translatedName = translatedName;
    }

    public void setSelective(boolean selective) {
        this.isSelective = selective;
    }

    public boolean isSelective() {
        return isSelective;
    }

    public int size() {
        return grid.length;
    }

    @Override
    public String toString() {
        return name;
    }
}
