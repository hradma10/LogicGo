package cz.logicgo.engine.context;

import cz.logicgo.core.GameContext;
import cz.logicgo.core.exceptions.parallel.LimitReachedException;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.misc.StepCounter;


public record ShikakuCreateContext(StepCounter stepCounter) implements GameContext {


    @Override
    public void checkContext() throws LimitReachedException, ThreadTerminationException {
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

    @Override
    public void reset() {
        stepCounter.reset();
    }
}
