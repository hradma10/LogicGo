package cz.logicgo.core.model.games.drawable.bounds;

public class CalculateBorderPoints {

    public static final class CoordinatesComputeFromCenter {
        public static double topLeftXFromCenter(Double centerX, Double width) {
            return centerX - width / 2;
        }

        public static double topLeftYFromCenter(Double centerY, Double height) {
            return centerY - height / 2;
        }

        public static double bottomLeftXFromCenter(Double centerX, Double width) {
            return centerX - width / 2;
        }

        public static double bottomLeftYFromCenter(Double centerY, Double height) {
            return centerY + height / 2;
        }

        public static double topRightXFromCenter(Double centerX, Double width) {
            return centerX + width / 2;
        }

        public static double topRightYFromCenter(Double centerY, Double height) {
            return centerY - height / 2;
        }

        public static double bottomRightXFromCenter(Double centerX, Double width) {
            return centerX + width / 2;
        }

        public static double bottomRightYFromCenter(Double centerY, Double height) {
            return centerY + height / 2;
        }

    }

    public static final class CoordinatesComputeFromStaticPoint {
        public static double centerFromTopLeftX(Double staticX, Double width) {
            return staticX + width / 2;
        }

        public static double centerFromTopLeftY(Double staticY, Double height) {
            return staticY + height / 2;
        }

        public static double centerFromBottomLeftX(Double staticX, Double width) {
            return staticX + width / 2;
        }

        public static double centerFromBottomLeftY(Double staticY, Double height) {
            return staticY - height / 2;
        }

        public static double centerFromTopRightX(Double staticX, Double width) {
            return staticX - width / 2;
        }

        public static double centerFromTopRightY(Double staticY, Double height) {
            return staticY + height / 2;
        }

        public static double centerFromBottomRightX(Double staticX, Double width) {
            return staticX - width / 2;
        }

        public static double centerFromBottomRightY(Double staticY, Double height) {
            return staticY - height / 2;
        }

    }
}
