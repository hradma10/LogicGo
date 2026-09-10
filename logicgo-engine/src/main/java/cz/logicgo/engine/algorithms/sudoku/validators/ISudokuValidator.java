package cz.logicgo.engine.algorithms.sudoku.validators;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.engine.algorithms.sudoku.TargetedCell;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.Constraint;

import java.util.ArrayList;
import java.util.List;

public interface ISudokuValidator {
    boolean isValidMove(int row, int col, int num);

    ArrayList<TargetedCell> getEmptyCells();

    ArrayList<Integer> getCellCandidates(int row, int col);

    ArrayList<Integer> getCellCandidates(SudokuCell sudokuCell);

    void update(int row, int col, int num);

    void unset(int row, int col, int num);

    boolean exists(int row, int col, int num);

    ISudokuValidator copy(Sudoku sudoku);

    void addConstraints(Constraint... constraint);

    List<Constraint> getConstraints();

    void fillUnitsInConstraints();

    int getGridSize();
}
