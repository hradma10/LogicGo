package cz.logicgo.engine.algorithms.sudoku.validators;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.engine.algorithms.sudoku.TargetedCell;
import cz.logicgo.engine.algorithms.sudoku.multi.SubGrid;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.Constraint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MultiSudokuValidator implements ISudokuValidator {

    private final List<SubGrid> subGrids;
    private final int globalGridSize;
    private final Sudoku globalSudoku;

    public MultiSudokuValidator(Sudoku globalSudoku, List<SubGrid> subGrids, int globalGridSize) {
        this.globalSudoku = globalSudoku;
        this.subGrids = subGrids;
        this.globalGridSize = globalGridSize;
    }

    @Override
    public boolean isValidMove(int row, int col, int num) {
        for (SubGrid sub : subGrids) {
            if (sub.containsGlobal(row, col)) {
                if (!sub.validator().isValidMove(sub.getLocalRow(row), sub.getLocalCol(col), num)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ArrayList<Integer> getCellCandidates(int row, int col) {
        List<ArrayList<Integer>> allCandidates = new ArrayList<>();

        for (SubGrid sub : subGrids) {
            if (sub.containsGlobal(row, col)) {
                allCandidates.add(sub.validator().getCellCandidates(sub.getLocalRow(row), sub.getLocalCol(col)));
            }
        }

        if (allCandidates.isEmpty()) return new ArrayList<>();
        Set<Integer> intersection = new HashSet<>(allCandidates.getFirst());
        for (int i = 1; i < allCandidates.size(); i++) {
            intersection.retainAll(allCandidates.get(i));
        }

        return new ArrayList<>(intersection);
    }

    @Override
    public void update(int row, int col, int num) {
        for (SubGrid sub : subGrids) {
            if (sub.containsGlobal(row, col)) {
                sub.validator().update(sub.getLocalRow(row), sub.getLocalCol(col), num);
            }
        }
    }

    @Override
    public void unset(int row, int col, int num) {
        for (SubGrid sub : subGrids) {
            if (sub.containsGlobal(row, col)) {
                sub.validator().unset(sub.getLocalRow(row), sub.getLocalCol(col), num);
            }
        }
    }

    @Override
    public boolean exists(int row, int col, int num) {
        for (SubGrid sub : subGrids) {
            if (sub.containsGlobal(row, col)) {
                if (sub.validator().exists(sub.getLocalRow(row), sub.getLocalCol(col), num)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ArrayList<TargetedCell> getEmptyCells() {
        ArrayList<TargetedCell> emptyCells = new ArrayList<>();
        SudokuCell[][] board = globalSudoku.getBoard();
        int maxR = board != null ? Math.min(board.length, globalGridSize) : 0;

        for (int r = 0; r < globalGridSize; r++) {
            for (int c = 0; c < globalGridSize; c++) {
                if (isCellInAnySubGrid(r, c)) {
                    boolean isZero = (r >= maxR || board[r] == null || c >= board[r].length || globalSudoku.isZero(r, c));
                    if (isZero) {
                        emptyCells.add(new TargetedCell(r, c));
                    }
                }
            }
        }
        return emptyCells;
    }

    private boolean isCellInAnySubGrid(int row, int col) {
        for (SubGrid sub : subGrids) {
            if (sub.containsGlobal(row, col)) return true;
        }
        return false;
    }

    @Override
    public ISudokuValidator copy(Sudoku sudoku) {
        List<SubGrid> copiedSubGrids = new ArrayList<>();
        for (SubGrid sub : subGrids) {
            copiedSubGrids.add(new SubGrid(sub.rowOffset(), sub.colOffset(), sub.validator().copy(sudoku)));
        }
        return new MultiSudokuValidator(sudoku, copiedSubGrids, this.globalGridSize);
    }

    @Override
    public void fillUnitsInConstraints() {
        SudokuCell[][] board = globalSudoku.getBoard();
        if (board == null) return;

        int maxR = Math.min(board.length, globalGridSize);

        for (int r = 0; r < maxR; r++) {
            if (board[r] == null) continue;
            int maxC = Math.min(board[r].length, globalGridSize);

            for (int c = 0; c < maxC; c++) {
                if (board[r][c] != null && board[r][c].getValue() != 0) {
                    int value = board[r][c].getValue();

                    for (SubGrid sub : subGrids) {
                        if (sub.containsGlobal(r, c)) {
                            int localRow = sub.getLocalRow(r);
                            int localCol = sub.getLocalCol(c);

                            sub.validator().update(localRow, localCol, value);
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getGridSize() {
        return globalGridSize;
    }

    @Override
    public ArrayList<Integer> getCellCandidates(SudokuCell sudokuCell) {
        return getCellCandidates(sudokuCell.getRow(), sudokuCell.getCol());
    }

    @Override
    public void addConstraints(Constraint... constraint) {
    }

    public List<SubGrid> getSubGrids() {
        return subGrids;
    }

    @Override
    public List<Constraint> getConstraints() {
        List<Constraint> allConstraints = new ArrayList<>();
        for (SubGrid sub : subGrids) {
            allConstraints.addAll(sub.validator().getConstraints());
        }
        return allConstraints;
    }
}
