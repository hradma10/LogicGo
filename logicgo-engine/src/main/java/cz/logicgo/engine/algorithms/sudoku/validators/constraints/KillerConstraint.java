package cz.logicgo.engine.algorithms.sudoku.validators.constraints;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.core.misc.GridCell;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.Constraint;

import java.util.*;

import static cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifierRecords.KillerCage;

public class KillerConstraint implements Constraint {

    private final SudokuCell[][] board;
    private final List<KillerCage> cages;
    private final HashMap<GridCell, KillerCage> cellToCage;
    private final HashMap<KillerCage, Set<Integer>> allowedDigitsPerCage;
    private final int gridSize;

    public KillerConstraint(SudokuCell[][] board, List<KillerCage> cages) {
        this.board = board;
        this.cages = cages;
        this.gridSize = board.length;
        this.cellToCage = new HashMap<>();
        this.allowedDigitsPerCage = new HashMap<>();

        for (KillerCage cage : cages) {
            for (GridCell cell : cage.cells()) {
                cellToCage.put(cell, cage);
            }
            allowedDigitsPerCage.put(cage, calculateAllowedDigits(cage.targetSum(), cage.cells().size()));
        }
    }

    private Set<Integer> calculateAllowedDigits(int targetSum, int cageSize) {
        Set<Integer> validDigits = new HashSet<>();
        findCombinations(targetSum, cageSize, 1, 0, new ArrayList<>(), validDigits);
        return validDigits;
    }

    private void findCombinations(int targetSum, int size, int startDigit, int currentSum, List<Integer> currentCombo, Set<Integer> validDigits) {
        if (currentCombo.size() == size) {
            if (currentSum == targetSum) {
                validDigits.addAll(currentCombo);
            }
            return;
        }

        for (int i = startDigit; i <= gridSize; i++) {
            if (currentSum + i <= targetSum) {
                currentCombo.add(i);
                findCombinations(targetSum, size, i + 1, currentSum + i, currentCombo, validDigits);
                currentCombo.removeLast();
            }
        }
    }

    @Override
    public boolean isViolated(int row, int col, int num) {
        GridCell target = new GridCell(row, col);
        KillerCage cage = cellToCage.get(target);
        if (cage == null) return false;
        if (!allowedDigitsPerCage.get(cage).contains(num)) {
            return true;
        }

        int currentSum = num;
        int emptyCount = 0;
        boolean[] usedInCage = new boolean[gridSize + 1];
        usedInCage[num] = true;

        for (GridCell cell : cage.cells()) {
            if (cell.row() == row && cell.col() == col) continue;

            int cellValue = board[cell.row()][cell.col()].getValue();
            if (cellValue == num) return true;

            if (cellValue == 0) {
                emptyCount++;
            } else {
                currentSum += cellValue;
                usedInCage[cellValue] = true;
            }
        }

        int remainingSum = cage.targetSum() - currentSum;
        if (currentSum + emptyCount > cage.targetSum()) return true;
        if (emptyCount == 0 && currentSum != cage.targetSum()) return true;
        if (emptyCount > 0) {
            if (emptyCount == 1 && (remainingSum > gridSize || remainingSum <= 0 || usedInCage[remainingSum])) {
                return true;
            }
            int minPossibleRemaining = 0;
            int maxPossibleRemaining = 0;

            int foundMin = 0;
            for (int i = 1; i <= gridSize && foundMin < emptyCount; i++) {
                if (!usedInCage[i]) {
                    minPossibleRemaining += i;
                    foundMin++;
                }
            }

            int foundMax = 0;
            for (int i = gridSize; i >= 1 && foundMax < emptyCount; i--) {
                if (!usedInCage[i]) {
                    maxPossibleRemaining += i;
                    foundMax++;
                }
            }
            return remainingSum < minPossibleRemaining || remainingSum > maxPossibleRemaining;
        }

        return false;
    }

    @Override
    public Constraint copy(Sudoku sudoku) {
        return new KillerConstraint(sudoku.getBoard(), cages);
    }
}
