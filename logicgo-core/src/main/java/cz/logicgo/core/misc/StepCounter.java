package cz.logicgo.core.misc;

import java.util.concurrent.atomic.AtomicLong;

public class StepCounter {
    private final AtomicLong steps = new AtomicLong();

    public Long limit = null;

    public void increment() {
        steps.incrementAndGet();
    }

    public void add(long n) {
        steps.addAndGet(n);
    }

    public long get() {
        return steps.get();
    }

    public void reset() {
        steps.set(0);
    }

    public Long getLimit() {
        return limit;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }
}
