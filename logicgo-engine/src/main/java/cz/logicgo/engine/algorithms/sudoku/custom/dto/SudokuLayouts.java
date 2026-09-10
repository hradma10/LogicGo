package cz.logicgo.engine.algorithms.sudoku.custom.dto;


import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuRegionLayout;

import java.util.List;

public class SudokuLayouts {
    private List<SizeLayouts> sizes;

    public List<SizeLayouts> getLayouts() {
        return sizes;
    }

    public SudokuLayouts(List<SizeLayouts> sizes) {
        this.sizes = sizes;
    }

    public SudokuLayouts() {
    }

    public List<SizeLayouts> getSizes() {
        return sizes;
    }

    public void setSizes(List<SizeLayouts> sizes) {
        this.sizes = sizes;
    }

    public static class SizeLayouts {
        private int size;
        private List<SudokuRegionLayout> layouts;

        public SizeLayouts(int size, List<SudokuRegionLayout> layouts) {
            this.size = size;
            this.layouts = layouts;
        }

        public SizeLayouts() {
        }

        public int getSize() {
            return size;
        }

        public List<SudokuRegionLayout> getRegionLayouts() {
            return layouts;
        }

        public void setLayouts(List<SudokuRegionLayout> layouts) {
            this.layouts = layouts;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }
}
