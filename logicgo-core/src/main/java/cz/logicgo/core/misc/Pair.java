package cz.logicgo.core.misc;

import java.io.Serializable;
import java.util.Objects;

public class Pair<K, V> implements Serializable {

    private K first;
    private V second;

    public Pair(K first, V second) {
        this.first = first;
        this.second = second;
    }

    public K getFirst() {
        return first;
    }

    public void setFirst(K first) {
        this.first = first;
    }

    public V getSecond() {
        return second;
    }

    public void setSecond(V second) {
        this.second = second;
    }

    @Override
    public String toString() {
        return first + "=" + second;
    }

    @Override
    public int hashCode() {
        int result = 7;
        result = 31 * result + (first != null ? first.hashCode() : 0);
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Pair<?, ?> other)) return false;

        if (!Objects.equals(first, other.first)) return false;
        return Objects.equals(second, other.second);
    }
}
