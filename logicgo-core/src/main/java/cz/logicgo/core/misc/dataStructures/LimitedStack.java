package cz.logicgo.core.misc.dataStructures;

import java.util.Stack;


public class LimitedStack<T> extends Stack<T> {
    private static final int DEFAULT_MAX_SIZE = 200;
    private final int maxSize;


    public LimitedStack() {
        super();
        maxSize = DEFAULT_MAX_SIZE;
    }


    public LimitedStack(int size) {
        super();
        maxSize = size;
    }

    @Override
    public T push(T item) {
        if (item == null) {
            throw new NullPointerException();
        }
        if (this.size() == maxSize) {
            this.removeFirst();
        }
        return super.push(item);
    }
}
