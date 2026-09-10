package cz.logicgo.core.model.games.drawable.bounds;

import java.util.Objects;

public final class Point {
    private double x;
    private double y;

    public Point() {
        this(0, 0);
    }

    public Point(Point point) {
        this(point.getX(), point.getY());
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point makeCopy() {
        return new Point(this);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setBothCoordinates(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setBothCoordinates(Point point) {
        setBothCoordinates(point.getX(), point.getY());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point point)) return false;
        return Double.compare(x, point.x) == 0 && Double.compare(y, point.y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Point{x=" + x + ", y=" + y + "}";
    }
}
