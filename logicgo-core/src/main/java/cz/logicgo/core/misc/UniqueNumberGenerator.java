package cz.logicgo.core.misc;

import java.util.concurrent.atomic.AtomicInteger;

public class UniqueNumberGenerator {
    private final AtomicInteger currentMaxId;

    public UniqueNumberGenerator() {
        this(0);
    }

    public UniqueNumberGenerator(int startId) {
        this.currentMaxId = new AtomicInteger(startId);
    }

    public int generateNewId() {
        return currentMaxId.incrementAndGet();
    }

    public int getCurrentMaxId() {
        return currentMaxId.get();
    }

    public void setCurrentMaxId(int currentMaxId) {
        this.currentMaxId.set(currentMaxId);
    }
}
