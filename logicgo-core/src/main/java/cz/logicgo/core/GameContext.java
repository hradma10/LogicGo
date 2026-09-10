package cz.logicgo.core;


import cz.logicgo.core.exceptions.parallel.LimitReachedException;
import cz.logicgo.core.exceptions.parallel.MultipleSolutionException;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.misc.StepCounter;

public interface GameContext {
    void checkContext() throws MultipleSolutionException, LimitReachedException, ThreadTerminationException;

    boolean overLimit();

    StepCounter stepCounter();

    void reset();
}
