package cz.logicgo.engine.algorithms.sudoku.multi;


import cz.logicgo.engine.algorithms.sudoku.validators.ISudokuValidator;

public record SubGrid(int rowOffset, int colOffset, ISudokuValidator validator) {

    public boolean containsGlobal(int row, int col) {
        int size = validator.getGridSize();
        return row >= rowOffset && row < rowOffset + size &&
                col >= colOffset && col < colOffset + size;
    }

    public int getLocalRow(int globalRow) {
        return globalRow - rowOffset;
    }

    public int getLocalCol(int globalCol) {
        return globalCol - colOffset;
    }
}
