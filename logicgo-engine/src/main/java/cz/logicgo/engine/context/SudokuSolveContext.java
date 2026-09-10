package cz.logicgo.engine.context;

import cz.logicgo.core.GameContext;
import cz.logicgo.core.exceptions.parallel.LimitReachedException;
import cz.logicgo.core.exceptions.parallel.MultipleSolutionException;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.misc.StepCounter;
import cz.logicgo.engine.algorithms.sudoku.SudokuGame;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SudokuSolveContext implements GameContext {
    private final AtomicInteger solutionCount = new AtomicInteger(0);
    private volatile SudokuGame solvedGame = null;
    private volatile boolean stopFlag = false;

    private final ConcurrentHashMap<Long, Integer> boardCache;
    private final StepCounter stepCounter;
    private final int solutionLimit;


    public SudokuSolveContext(StepCounter stepCounter, int solutionLimit) {
        this.stepCounter = stepCounter;
        this.solutionLimit = solutionLimit;
        this.boardCache = new ConcurrentHashMap<>(16384);
    }

    public SudokuSolveContext(int solutionLimit) {
        this.stepCounter = new StepCounter();
        this.solutionLimit = solutionLimit;
        this.boardCache = new ConcurrentHashMap<>(16384);
    }

    public SudokuSolveContext(int solutionLimit, ConcurrentHashMap<Long, Integer> sharedCache) {
        this.stepCounter = new StepCounter();
        this.solutionLimit = solutionLimit;
        this.boardCache = sharedCache;
    }

    public int incrementSolutionCountAndGet() {
        int count = solutionCount.incrementAndGet();
        if (count >= solutionLimit) {
            stopFlag = true;
        }
        return count;
    }

    public void checkContext() throws MultipleSolutionException, LimitReachedException, ThreadTerminationException {
        if (this.shouldStop()) {
            throw new MultipleSolutionException();
        }

        if (this.overLimit()) {
            throw new LimitReachedException();
        }

        if (Thread.currentThread().isInterrupted()) {
            throw new ThreadTerminationException();
        }
    }

    public boolean overLimit() {
        return stepCounter.get() > stepCounter.getLimit();
    }


    public void setStepsLimit(long stepsLimit) {
        stepCounter.setLimit(stepsLimit);
    }

    public int getSolutionCount() {
        return solutionCount.get();
    }

    public int getSolutionLimit() {
        return solutionLimit;
    }

    public boolean shouldStop() {
        return stopFlag || Thread.currentThread().isInterrupted();
    }

    public Integer getCachedSolutions(long hash) {
        if (boardCache == null) return null;
        return boardCache.get(hash);
    }

    public void cacheResult(long hash, int solutions) {
        if (boardCache != null) {
            boardCache.putIfAbsent(hash, solutions);
        }
    }

    public synchronized void setSolvedGame(SudokuGame game) {
        if (solvedGame == null) {
            solvedGame = game.makeCopy();
        }
    }

    public SudokuGame getSolvedGame() {
        return solvedGame;
    }

    public void setStopFlag(boolean stopFlag) {
        this.stopFlag = stopFlag;
    }

    public StepCounter stepCounter() {
        return stepCounter;
    }

    @Override
    public void reset() {
        stepCounter.reset();
        solutionCount.set(0);
        boardCache.clear();
        stopFlag = false;
        solvedGame = null;
    }
}
