package cz.logicgo.engine.algorithms.shikaku.solver;

import java.util.Objects;

public final class Rect {
    private final int r1;
    private final int c1;
    private final int r2;
    private final int c2;
    public final long colMask;

    public Rect(int r1, int c1, int r2, int c2) {
        this.r1 = r1;
        this.c1 = c1;
        this.r2 = r2;
        this.c2 = c2;

        long mask = 0L;
        for (int c = c1; c <= c2; c++) {
            mask |= (1L << c);
        }
        this.colMask = mask;
    }

    public int r1() {
        return r1;
    }

    public int c1() {
        return c1;
    }

    public int r2() {
        return r2;
    }

    public int c2() {
        return c2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Rect) obj;
        return this.r1 == that.r1 &&
                this.c1 == that.c1 &&
                this.r2 == that.r2 &&
                this.c2 == that.c2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(r1, c1, r2, c2);
    }

    @Override
    public String toString() {
        return "Rect[" +
                "r1=" + r1 + ", " +
                "c1=" + c1 + ", " +
                "r2=" + r2 + ", " +
                "c2=" + c2 + ']';
    }
}
