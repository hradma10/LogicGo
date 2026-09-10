package cz.logicgo.engine.algorithms.sudoku.custom.dto;


import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;

import java.util.ArrayList;
import java.util.List;

public class PatternLayouts {

    private List<PatternGroup> patterns;

    public PatternLayouts() {
    }

    public List<PatternGroup> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<PatternGroup> patterns) {
        this.patterns = patterns;
    }

    public static class PatternGroup {
        private int size;

        private ArrayList<SudokuPatternLayout> patterns;

        public PatternGroup() {
        }

        public PatternGroup(int size) {
            this.size = size;
            this.patterns = new ArrayList<>();
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public ArrayList<SudokuPatternLayout> getPatterns() {
            return patterns;
        }

        public void setPatterns(ArrayList<SudokuPatternLayout> patterns) {
            this.patterns = patterns;
        }
    }
}
