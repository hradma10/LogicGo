package cz.logicgo.engine.algorithms.sudoku.validators;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.engine.algorithms.sudoku.TargetedCell;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.Constraint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SudokuValidator implements ISudokuValidator {
    private final Sudoku sudoku;
    private final ArrayList<Constraint> constraints;
    private final int gridSize;

    private final int[] removedCandidates;

    public SudokuValidator(Sudoku sudoku, ArrayList<Constraint> constraints) {
        this.sudoku = sudoku;
        this.constraints = constraints;
        this.gridSize = sudoku.getType().getGridSize();
        this.removedCandidates = new int[gridSize * gridSize];
    }

    public SudokuValidator(Sudoku sudoku, SudokuValidator other) {
        this.sudoku = sudoku;
        this.constraints = copyConstraints(sudoku, other);
        this.gridSize = sudoku.getType().getGridSize();

        this.removedCandidates = new int[gridSize * gridSize];
        System.arraycopy(other.removedCandidates, 0, this.removedCandidates, 0, this.removedCandidates.length);
    }

    public void removeCandidate(int row, int col, int num) {
        int cellKey = row * gridSize + col;
        removedCandidates[cellKey] |= (1 << num);
    }

    public void restoreCandidate(int row, int col, int num) {
        int cellKey = row * gridSize + col;
        removedCandidates[cellKey] &= ~(1 << num);
    }

    public void clearRemovedCandidates() {
        Arrays.fill(removedCandidates, 0);
    }

    private static ArrayList<Constraint> copyConstraints(Sudoku sudoku, SudokuValidator other) {
        ArrayList<Constraint> copiedConstraints = new ArrayList<>();
        for (Constraint constraint : other.getConstraints()) {
            Constraint copy = constraint.copy(sudoku);
            copiedConstraints.add(copy);
        }
        return copiedConstraints;
    }

    @Override
    public void fillUnitsInConstraints() {
        SudokuCell[][] board = sudoku.getBoard();
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[0].length; col++) {
                if (board[row][col] != null && board[row][col].getValue() != 0) {
                    update(row, col, board[row][col].getValue());
                }
            }
        }
    }

    public SudokuValidator copy(Sudoku sudoku) {
        return new SudokuValidator(sudoku, this);
    }

    @Override
    public void addConstraints(Constraint... constraint) {
        if (constraint != null && constraint[0] != null) {
            constraints.addAll(List.of(constraint));
        }
    }

    public boolean isValidMove(int row, int col, int num) {
        if (num < 1 || num > gridSize) return false;

        return !exists(row, col, num);
    }

    public ArrayList<TargetedCell> getEmptyCells() {
        ArrayList<TargetedCell> emptyCells = new ArrayList<>();
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (sudoku.isZero(i, j)) {
                    emptyCells.add(new TargetedCell(i, j));
                }
            }
        }
        return emptyCells;
    }

    public ArrayList<Integer> getCellCandidates(int row, int col) {
        ArrayList<Integer> candidates = new ArrayList<>();
        int cellKey = row * gridSize + col;
        int cellRemovedMask = removedCandidates[cellKey];

        for (int num : sudoku.getType().getPossibleNumbers()) {

            if ((cellRemovedMask & (1 << num)) != 0) {
                continue;
            }

            boolean violated = false;
            for (Constraint constraint : constraints) {
                if (constraint.isViolated(row, col, num)) {
                    violated = true;
                    break;
                }
            }
            if (!violated) {
                candidates.add(num);
            }
        }
        return candidates;
    }

    public ArrayList<Integer> getCellCandidates(SudokuCell cell) {
        return getCellCandidates(cell.getRow(), cell.getCol());
    }


    public void update(int row, int col, int num) {
        for (Constraint constraint : constraints) {
            constraint.set(row, col, num);
        }
    }

    public void unset(int row, int col, int num) {
        for (Constraint constraint : constraints) {
            constraint.unset(row, col, num);
        }
    }

    public boolean exists(int row, int col, int num) {
        for (Constraint constraint : constraints) {
            if (constraint.isViolated(row, col, num)) {
                return true;
            }
        }
        return false;
    }

    public Sudoku getSudoku() {
        return sudoku;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public int getGridSize() {
        return gridSize;
    }
}
