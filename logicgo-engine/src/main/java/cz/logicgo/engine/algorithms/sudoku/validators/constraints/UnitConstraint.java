package cz.logicgo.engine.algorithms.sudoku.validators.constraints;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.Constraint;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.IndexFunction;

import java.util.BitSet;

public class UnitConstraint implements Constraint {
    private final BitSet[] units;


    private final IndexFunction indexFunction;

    public UnitConstraint(int count, int maxVal, IndexFunction indexFunction) {
        this.indexFunction = indexFunction;
        units = new BitSet[count];
        for (int i = 0; i < count; i++) {
            units[i] = new BitSet(maxVal + 1);
        }
    }

    @Override
    public boolean isViolated(int row, int col, int num) {
        int index = indexFunction.index(row, col);
        return units[index].get(num);
    }

    @Override
    public void set(int row, int col, int num) {
        int index = indexFunction.index(row, col);
        units[index].set(num);
    }

    @Override
    public void unset(int row, int col, int num) {
        int index = indexFunction.index(row, col);
        units[index].clear(num);
    }

    public BitSet[] getUnits() {
        return units;
    }

    public IndexFunction getIndexFunction() {
        return indexFunction;
    }

    public Constraint copy(Sudoku sudoku) {
        UnitConstraint copy = new UnitConstraint(units.length, units[0].size() - 1, indexFunction);
        for (int i = 0; i < units.length; i++) {
            copy.units[i] = (BitSet) units[i].clone();
        }
        return copy;
    }
}
