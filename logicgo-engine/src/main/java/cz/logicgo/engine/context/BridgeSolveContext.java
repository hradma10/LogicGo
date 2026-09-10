package cz.logicgo.engine.context;


import cz.logicgo.core.GameContext;
import cz.logicgo.core.misc.StepCounter;
import cz.logicgo.core.exceptions.parallel.LimitReachedException;
import cz.logicgo.core.exceptions.parallel.MultipleSolutionException;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BridgeSolveContext implements GameContext {
    private final AtomicInteger solutionCount = new AtomicInteger(0);
    private volatile boolean stopFlag = false;

    private final StepCounter stepCounter;
    private final int solutionLimit;


    public BridgeSolveContext(StepCounter stepCounter, int solutionLimit) {
        this.stepCounter = stepCounter;
        this.solutionLimit = solutionLimit;
    }

    public BridgeSolveContext(int solutionLimit) {
        this.stepCounter = new StepCounter();
        this.solutionLimit = solutionLimit;
    }

    public BridgeSolveContext(int solutionLimit, ConcurrentHashMap<Long, Integer> sharedCache) {
        this.stepCounter = new StepCounter();
        this.solutionLimit = solutionLimit;
    }

    public void incrementSolutionCountAndGet() {
        int count = solutionCount.incrementAndGet();
        if (count >= solutionLimit) {
            stopFlag = true;
        }
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
        Long limit = stepCounter.getLimit();
        if (limit == null) {
            limit = Long.MAX_VALUE;
        }
        return stepCounter.get() >= limit;
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
    }
}
