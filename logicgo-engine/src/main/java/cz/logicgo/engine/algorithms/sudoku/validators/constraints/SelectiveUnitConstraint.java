package cz.logicgo.engine.algorithms.sudoku.validators.constraints;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.Constraint;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.IndexFunction;

import java.util.BitSet;

public class SelectiveUnitConstraint extends UnitConstraint {

    public SelectiveUnitConstraint(int count, int maxVal, IndexFunction indexFunction) {
        super(count, maxVal, indexFunction);
    }

    @Override
    public boolean isViolated(int row, int col, int num) {
        int idx = getIndexFunction().index(row, col);
        return idx > 0 && this.getUnits()[idx - 1].get(num);
    }

    @Override
    public void set(int row, int col, int num) {
        int idx = getIndexFunction().index(row, col);
        if (idx > 0) this.getUnits()[idx - 1].set(num);
    }

    @Override
    public void unset(int row, int col, int num) {
        int idx = getIndexFunction().index(row, col);
        if (idx > 0) this.getUnits()[idx - 1].clear(num);
    }

    @Override
    public Constraint copy(Sudoku sudoku) {
        BitSet[] units = this.getUnits();
        SelectiveUnitConstraint copy = new SelectiveUnitConstraint(units.length, units[0].size() - 1, getIndexFunction());
        for (int i = 0; i < units.length; i++) {
            copy.getUnits()[i] = (BitSet) units[i].clone();
        }
        return copy;
    }
}
