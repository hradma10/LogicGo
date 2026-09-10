package cz.logicgo.engine.algorithms.mazes.gen;

import java.util.Objects;


public record RegionGenConfig(int width, int height, int numRegions, int minRegionSize, int swapIterations,
                              GrowthStrategy strategy) {
    private final static int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};


    public RegionGenConfig(int width, int height, int numRegions, int minRegionSize) {
        this(width, height, numRegions, minRegionSize, -1, GrowthStrategy.ORGANIC);
    }

    public RegionGenConfig(int width, int height, int numRegions, int minRegionSize, int swapIterations) {
        this(width, height, numRegions, minRegionSize, swapIterations, GrowthStrategy.VORONOI);
    }

    public int[][] dirs() {
        return dirs;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RegionGenConfig) obj;
        return this.width == that.width &&
                this.height == that.height &&
                this.numRegions == that.numRegions &&
                this.minRegionSize == that.minRegionSize &&
                this.swapIterations == that.swapIterations &&
                Objects.equals(this.strategy, that.strategy) &&
                Objects.equals(dirs, dirs);
    }

    @Override
    public String toString() {
        return "RegionGenConfig[" +
                "width=" + width + ", " +
                "height=" + height + ", " +
                "numRegions=" + numRegions + ", " +
                "minRegionSize=" + minRegionSize + ", " +
                "swapIterations=" + swapIterations + ", " +
                "strategy=" + strategy + ", " +
                "dirs=" + dirs + ']';
    }

}
