package cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces;

@FunctionalInterface
public interface IndexFunction {
    int index(int row, int col);
}
