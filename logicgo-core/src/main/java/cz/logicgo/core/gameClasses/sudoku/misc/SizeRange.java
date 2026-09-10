package cz.logicgo.core.gameClasses.sudoku.misc;

public record SizeRange(int minSize, int maxSize) {
        public SizeRange(int size) {
            this(size, size);
        }
    }
