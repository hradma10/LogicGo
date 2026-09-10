package cz.logicgo.engine.algorithms.sudoku.solvers;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.misc.StepCounter;
import cz.logicgo.engine.algorithms.sudoku.SudokuGame;
import cz.logicgo.engine.algorithms.sudoku.TargetedCell;
import cz.logicgo.engine.algorithms.sudoku.validators.ISudokuValidator;
import cz.logicgo.engine.context.SudokuSolveContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;


public class SudokuParallelSolver {

    private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static final int MAX_SOLUTIONS = 1;
    private static final ConcurrentHashMap<UUID, SudokuSolveContext> contexts = new ConcurrentHashMap<>();


    public static SudokuGame solveSudokuParallel(SudokuGame sudokuGame) {
        UUID uuid = UUID.randomUUID();
        StepCounter stepCounter = new StepCounter();
        SudokuSolveContext context = null;
        contexts.put(uuid, context);
        solvingParallelStart(sudokuGame, context);
        contexts.remove(uuid);
        return context.getSolutionCount() == 1 ? context.getSolvedGame() : null;
    }

    public static SudokuGame solveSudokuParallel(Sudoku sudoku) {
        SudokuGame sudokuGame = new SudokuGame(sudoku, true);
        return solveSudokuParallel(sudokuGame);
    }


    public static TargetedCell getOneWithMostCandidates(ArrayList<TargetedCell> cells, ISudokuValidator sudokuValidator, Random random) {
        TargetedCell bestCell = null;
        int maxCandidates = Integer.MIN_VALUE;
        int countWithMax = 0;

        for (TargetedCell cell : cells) {
            int currentSize = sudokuValidator.getCellCandidates(cell.getRow(), cell.getCol()).size();

            if (currentSize > maxCandidates) {
                maxCandidates = currentSize;
                bestCell = cell;
                countWithMax = 1;
            } else if (currentSize == maxCandidates) {
                countWithMax++;
                if (random.nextInt(countWithMax) == 0) {
                    bestCell = cell;
                }
            }
        }

        return bestCell;
    }

    public static TargetedCell getOneWithLeastCandidates(ArrayList<TargetedCell> cells, ISudokuValidator validator, Random random) {
        TargetedCell bestCell = null;
        int minCandidates = Integer.MAX_VALUE;
        int countWithMin = 0;

        for (TargetedCell cell : cells) {
            int currentSize = validator.getCellCandidates(cell.getRow(), cell.getCol()).size();

            if (currentSize == 0) continue;

            if (currentSize < minCandidates) {
                minCandidates = currentSize;
                bestCell = cell;
                countWithMin = 1;
            } else if (currentSize == minCandidates) {
                countWithMin++;
                if (random.nextInt(countWithMin) == 0) {
                    bestCell = cell;
                }
            }
        }

        return bestCell;
    }

    public static void solvingParallelStart(SudokuGame sudokuGame, SudokuSolveContext context) {
        long currentHash = sudokuGame.getCurrentHash();

        Integer cachedSolutions = context.getCachedSolutions(currentHash);
        if (cachedSolutions != null) {
            for (int i = 0; i < cachedSolutions; i++) {
                if (context.incrementSolutionCountAndGet() > MAX_SOLUTIONS) {
                    context.setStopFlag(true);
                    break;
                }
            }
            return;
        }

        StepCounter stepCounter = context.stepCounter();
        List<Future<?>> futures = new ArrayList<>();
        ISudokuValidator validator = sudokuGame.getSudokuValidator();
        ArrayList<TargetedCell> emptyCells = validator.getEmptyCells();

        if (emptyCells.isEmpty()) {
            context.incrementSolutionCountAndGet();
            context.setSolvedGame(sudokuGame);
            context.cacheResult(currentHash, 1);
            return;
        }

        Random randomInstance = sudokuGame.getSudoku().getRandomInstance();
        TargetedCell leastCandidateCell = getOneWithMostCandidates(emptyCells, validator, randomInstance);
        int row = leastCandidateCell.getRow();
        int col = leastCandidateCell.getCol();
        ArrayList<Integer> candidates = validator.getCellCandidates(row, col);

        for (var candidate : candidates) {
            stepCounter.increment();
            SudokuGame sudokuGameCopy = new SudokuGame(sudokuGame);
            sudokuGameCopy.setNumber(row, col, candidate);
            futures.add(executor.submit(() -> {
                try {
                    solvingParallel(sudokuGameCopy, context);
                } catch (ThreadTerminationException ignored) {
                }
            }));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException ignored) {
            }
        }

        context.cacheResult(currentHash, context.getSolutionCount());
    }

    public static boolean solvingParallel(SudokuGame sudokuGame, SudokuSolveContext context) throws ThreadTerminationException {
        if (context.shouldStop() || context.getSolutionCount() > MAX_SOLUTIONS || Thread.currentThread().isInterrupted())
            throw new ThreadTerminationException();
        StepCounter stepCounter = context.stepCounter();
        var validator = sudokuGame.getSudokuValidator();
        ArrayList<TargetedCell> emptyCells = validator.getEmptyCells();

        if (emptyCells.isEmpty()) {
            allCellsFilledHandler(sudokuGame, context);
            return true;
        }
        Random randomInstance = sudokuGame.getSudoku().getRandomInstance();

        TargetedCell emptyCell = getOneWithLeastCandidates(emptyCells, validator, randomInstance);
        int row = emptyCell.getRow();
        int col = emptyCell.getCol();
        var cellCandidates = validator.getCellCandidates(row, col);

        if (cellCandidates.isEmpty()) {
            return true;
        }

        for (var candidate : cellCandidates) {
            stepCounter.increment();
            sudokuGame.setNumber(row, col, candidate);

            solvingParallel(sudokuGame, context);

            if (context.shouldStop() || context.getSolutionCount() > MAX_SOLUTIONS || Thread.currentThread().isInterrupted())
                throw new ThreadTerminationException();
            sudokuGame.removeNumber(row, col);
            stepCounter.increment();
        }
        return false;
    }

    private static void allCellsFilledHandler(SudokuGame sudokuGame, SudokuSolveContext context) throws ThreadTerminationException {
        if (context.incrementSolutionCountAndGet() > MAX_SOLUTIONS) {
            context.setStopFlag(true);
            throw new ThreadTerminationException();
        } else {
            context.setSolvedGame(sudokuGame);
        }
    }

    public static ArrayList<TargetedCell> fillOneCandidateCells(ArrayList<TargetedCell> emptyCells, SudokuGame sudokuGame) {
        ArrayList<TargetedCell> filledCells = new ArrayList<>();
        for (var cell : emptyCells) {
            ArrayList<Integer> candidates = sudokuGame.getSudokuValidator().getCellCandidates(cell.getRow(), cell.getCol());
            if (candidates.size() == 1) {
                int value = candidates.getFirst();
                sudokuGame.setNumber(cell.getRow(), cell.getCol(), value);
                filledCells.add(cell);
                cell.setVal(value);
            }
        }
        return filledCells;
    }

    public static void refillOneCandidateCells(ArrayList<TargetedCell> filledCells, SudokuGame sudokuGame) {
        for (TargetedCell filled : filledCells) {
            sudokuGame.removeNumber(filled.getRow(), filled.getCol());
        }
    }


}
