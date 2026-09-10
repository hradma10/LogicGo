package cz.logicgo.engine.algorithms.drawing;

import cz.logicgo.core.model.games.drawable.bounds.Point;

public class CohenSutherlandAlgorithm {

    private static final byte INSIDE = 0;
    private static final byte LEFT = 1;
    private static final byte RIGHT = 2;
    private static final byte BOTTOM = 4;
    private static final byte TOP = 8;

    public static boolean isIntersecting(double xMin, double yMin, double xMax, double yMax, Point startPoint, Point endPoint) {
        double sX = startPoint.getX();
        double sY = startPoint.getY();
        double eX = endPoint.getX();
        double eY = endPoint.getY();
        int outCode0 = computeOutCode(xMin, yMin, xMax, yMax, sX, sY);
        int outCode1 = computeOutCode(xMin, yMin, xMax, yMax, eX, eY);
        boolean accept = false;

        while (true) {
            if ((outCode0 == 0 && outCode1 == 0)) {
                accept = true;
                break;
            } else if ((outCode0 & outCode1) != 0) {
                break;
            } else {
                double x, y;
                int codeOut = Math.max(outCode1, outCode0);

                if ((codeOut & TOP) != 0) {
                    x = sX + (eX - sX) * (yMax - sY) / (eY - sY);
                    y = yMax;
                } else if ((codeOut & BOTTOM) != 0) {
                    x = sX + (eX - sX) * (yMin - sY) / (eY - sY);
                    y = yMin;
                } else if ((codeOut & RIGHT) != 0) {
                    y = sY + (eY - sY) * (xMax - sX) / (eX - sX);
                    x = xMax;
                } else {
                    y = sY + (eY - sY) * (xMin - sX) / (eX - sX);
                    x = xMin;
                }

                if (codeOut == outCode0) {
                    sX = x;
                    sY = y;
                    outCode0 = computeOutCode(xMin, yMin, xMax, yMax, sX, sY);
                } else {
                    eX = x;
                    eY = y;
                    outCode1 = computeOutCode(xMin, yMin, xMax, yMax, eX, eY);
                }
            }
        }

        return accept;
    }

    private static int computeOutCode(double xMin, double yMin, double xMax, double yMax, double x, double y) {
        int code = INSIDE;

        if (x < xMin) {
            code |= LEFT;
        } else if (x > xMax) {
            code |= RIGHT;
        }

        if (y < yMin) {
            code |= BOTTOM;
        } else if (y > yMax) {
            code |= TOP;
        }

        return code;
    }

}
