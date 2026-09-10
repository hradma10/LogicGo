package cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces;


import cz.logicgo.core.entity.games.sudoku.Sudoku;

import java.io.Serializable;

@FunctionalInterface
public interface Constraint extends Serializable {
    boolean isViolated(int row, int col, int num);

    default void set(int row, int col, int num) {
    }

    default void unset(int row, int col, int num) {
    }

    default Constraint copy(Sudoku sudoku) {
        return null;
    }

}
