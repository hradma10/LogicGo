package cz.logicgo.engine.algorithms.sudoku.solvers;


import cz.logicgo.core.exceptions.parallel.LimitReachedException;
import cz.logicgo.core.exceptions.parallel.MultipleSolutionException;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.misc.StepCounter;
import cz.logicgo.engine.algorithms.sudoku.SudokuGame;
import cz.logicgo.engine.algorithms.sudoku.TargetedCell;
import cz.logicgo.engine.algorithms.genStatistics.GenerationStatistics;
import cz.logicgo.engine.algorithms.sudoku.validators.MultiSudokuValidator;
import cz.logicgo.engine.context.SudokuSolveContext;

import java.util.ArrayList;
import java.util.Random;

public class SudokuSequentialSolver {

    public static void solvingSequentialStart(SudokuGame sudokuGame, SudokuSolveContext context)
            throws ThreadTerminationException, LimitReachedException, MultipleSolutionException {

        GenerationStatistics.SudokuKey key = GenerationStatistics.constructKey(sudokuGame.getSudoku());
        Long dynamicStepLimit = GenerationStatistics.getSudokuSolveLimit(key);

        if (dynamicStepLimit != null && (context.stepCounter().getLimit() == null || context.stepCounter().getLimit() == Long.MAX_VALUE)) {
            context.setStepsLimit(dynamicStepLimit);
        }

        solvingSequential(sudokuGame, context);
    }

    private static int solvingSequential(SudokuGame sudokuGame, SudokuSolveContext context)
            throws LimitReachedException, MultipleSolutionException, ThreadTerminationException {

        context.checkContext();
        long currentHash = sudokuGame.getCurrentHash();

        Integer cached = context.getCachedSolutions(currentHash);
        if (cached != null) {
            for (int i = 0; i < cached; i++) {
                if (context.incrementSolutionCountAndGet() > context.getSolutionLimit()) {
                    context.setStopFlag(true);
                    break;
                }
            }
            return cached;
        }

        StepCounter mainStepCounter = context.stepCounter();
        var validator = sudokuGame.getSudokuValidator();
        ArrayList<TargetedCell> emptyCells = validator.getEmptyCells();

        if (emptyCells.isEmpty()) {
            if (context.incrementSolutionCountAndGet() > context.getSolutionLimit()) {
                context.setStopFlag(true);
            } else {
                context.setSolvedGame(sudokuGame);
            }
            return 1;
        }

        Random randomInstance = sudokuGame.getSudoku().getRandomInstance();
        TargetedCell emptyCell = SudokuParallelSolver.getOneWithLeastCandidates(emptyCells, validator, randomInstance);

        if (emptyCell == null) {
            return 0;
        }

        int row = emptyCell.getRow();
        int col = emptyCell.getCol();
        var cellCandidates = validator.getCellCandidates(row, col);

        if (cellCandidates.isEmpty()) {
            context.cacheResult(currentHash, 0);
            return 0;
        }

        if (cellCandidates.size() > 1) {
            int[] hiddenSingle = findFastHiddenSingle(sudokuGame);
            if (hiddenSingle != null) {
                row = hiddenSingle[0];
                col = hiddenSingle[1];
                cellCandidates = new ArrayList<>();
                cellCandidates.add(hiddenSingle[2]);
            }
        }

        int branchSolutions = 0;

        for (var candidate : cellCandidates) {
            mainStepCounter.increment();
            sudokuGame.setNumber(row, col, candidate);

            branchSolutions += solvingSequential(sudokuGame, context);

            sudokuGame.removeNumber(row, col);
            mainStepCounter.increment();

            if (context.shouldStop()) {
                break;
            }
        }

        if (!context.shouldStop()) {
            context.cacheResult(currentHash, branchSolutions);
        }

        return branchSolutions;
    }

    private static int[] findFastHiddenSingle(SudokuGame sudokuGame) {
        var validator = sudokuGame.getSudokuValidator();
        var sudoku = sudokuGame.getSudoku();

        if (validator instanceof MultiSudokuValidator) {
            return null;
        }

        int gridSize = validator.getGridSize();

        for (int r = 0; r < gridSize; r++) {
            for (int cand = 1; cand <= gridSize; cand++) {
                int possibleCols = 0;
                int targetCol = -1;

                for (int c = 0; c < gridSize; c++) {
                    if (sudoku.isZero(r, c) && validator.getCellCandidates(r, c).contains(cand)) {
                        possibleCols++;
                        targetCol = c;
                        if (possibleCols > 1) break;
                    }
                }

                if (possibleCols == 1) {
                    return new int[]{r, targetCol, cand};
                }
            }
        }

        return null;
    }
}
