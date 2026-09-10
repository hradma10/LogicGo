package cz.logicgo.engine.algorithms.sudoku.validators.constraints;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.ParityType;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.Constraint;

public class ParityConstraint implements Constraint {

    private final ParityType[][] parityGrid;

    public ParityConstraint(ParityType[][] parityGrid) {
        this.parityGrid = parityGrid;
    }

    @Override
    public boolean isViolated(int row, int col, int num) {
        ParityType constraint = parityGrid[row][col];
        return !constraint.matches(num);
    }

    @Override
    public void set(int row, int col, int num) {
    }

    @Override
    public void unset(int row, int col, int num) {
    }

    public ParityConstraint copy(Sudoku sudoku) {
        int rows = parityGrid.length;
        int cols = parityGrid[0].length;

        ParityType[][] copiedGrid = new ParityType[rows][cols];

        for (int r = 0; r < rows; r++) {
            System.arraycopy(parityGrid[r], 0, copiedGrid[r], 0, cols);
        }

        return new ParityConstraint(copiedGrid);
    }
}
