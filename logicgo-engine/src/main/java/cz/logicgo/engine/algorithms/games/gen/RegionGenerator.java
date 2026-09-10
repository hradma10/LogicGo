package cz.logicgo.engine.algorithms.games.gen;


import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.misc.GridCell;
import cz.logicgo.engine.algorithms.mazes.gen.GrowthStrategy;
import cz.logicgo.engine.algorithms.mazes.gen.RegionGenConfig;

import java.util.*;

public class RegionGenerator {

    public record RegionResult(int[][] grid, List<List<GridCell>> regions) {
    }

    public static RegionResult generate(RegionGenConfig regionGenConfig, int[][] mask, Random random) throws ThreadTerminationException {
        int[][] grid = new int[regionGenConfig.height()][regionGenConfig.width()];

        for (int r = 0; r < regionGenConfig.height(); r++) {
            for (int c = 0; c < regionGenConfig.width(); c++) {
                grid[r][c] = (mask[r][c] == 0) ? -2 : -1;
            }
        }

        List<List<GridCell>> regions = new ArrayList<>(regionGenConfig.numRegions());
        for (int i = 0; i < regionGenConfig.numRegions(); i++) {
            regions.add(new ArrayList<>());
        }

        List<GridCell> seeds = placeSeeds(grid, regionGenConfig, random);

        return switch (regionGenConfig.strategy()) {
            case ORGANIC -> growOrganic(grid, regions, seeds, regionGenConfig, random);
            case VORONOI -> growVoronoi(grid, regions, seeds, regionGenConfig, random);
            default -> growOrganic(grid, regions, seeds, regionGenConfig, random);
        };
    }


    private static RegionResult growOrganic(int[][] grid, List<List<GridCell>> regions, List<GridCell> seeds, RegionGenConfig regionGenConfig, Random random) throws ThreadTerminationException {
        Map<Integer, List<GridCell>> regionalFronts = new HashMap<>();

        for (int i = 0; i < seeds.size(); i++) {
            GridCell s = seeds.get(i);
            grid[s.row()][s.col()] = i;
            regions.get(i).add(s);

            List<GridCell> front = new ArrayList<>();
            front.add(s);
            regionalFronts.put(i, front);
        }

        while (!regionalFronts.isEmpty()) {
            checkInterruption();
            int minSize = Integer.MAX_VALUE;
            for (Integer rId : regionalFronts.keySet()) {
                minSize = Math.min(minSize, regions.get(rId).size());
            }
            List<Integer> candidates = new ArrayList<>();
            for (Integer rId : regionalFronts.keySet()) {
                if (regions.get(rId).size() <= minSize + 2) {
                    candidates.add(rId);
                }
            }
            if (candidates.isEmpty()) {
                candidates.addAll(regionalFronts.keySet());
            }
            int currentRegion = candidates.get(random.nextInt(candidates.size()));
            List<GridCell> front = regionalFronts.get(currentRegion);

            int index = random.nextInt(front.size());
            GridCell current = front.get(index);

            List<GridCell> unassignedNeighbors = getNeighbors(grid, current, regionGenConfig.dirs(), -1);

            if (unassignedNeighbors.isEmpty()) {
                GridCell lastCell = front.removeLast();
                if (index < front.size()) {
                    front.set(index, lastCell);
                }
                if (front.isEmpty()) {
                    regionalFronts.remove(currentRegion);
                }
            } else {
                GridCell next = unassignedNeighbors.get(random.nextInt(unassignedNeighbors.size()));
                grid[next.row()][next.col()] = currentRegion;
                regions.get(currentRegion).add(next);
                front.add(next);
            }
        }
        return new RegionResult(grid, regions);
    }

    private static RegionResult growOrganicLimited(int[][] grid, List<List<GridCell>> regions, List<GridCell> seeds, RegionGenConfig regionGenConfig, Random random) throws ThreadTerminationException {
        List<GridCell> activeFronts = new ArrayList<>();

        for (int i = 0; i < seeds.size(); i++) {
            GridCell s = seeds.get(i);
            grid[s.row()][s.col()] = i;
            regions.get(i).add(s);
            activeFronts.add(s);
        }
        int maxRegionSize = regionGenConfig.width();

        while (!activeFronts.isEmpty()) {
            checkInterruption();

            int index = random.nextInt(activeFronts.size());
            GridCell current = activeFronts.get(index);
            int currentRegion = grid[current.row()][current.col()];
            if (regions.get(currentRegion).size() >= maxRegionSize) {
                GridCell lastCell = activeFronts.removeLast();
                if (index < activeFronts.size()) {
                    activeFronts.set(index, lastCell);
                }
                continue;
            }

            List<GridCell> unassignedNeighbors = getNeighbors(grid, current, regionGenConfig.dirs(), -1);

            if (unassignedNeighbors.isEmpty()) {
                GridCell lastCell = activeFronts.removeLast();
                if (index < activeFronts.size()) {
                    activeFronts.set(index, lastCell);
                }
            } else {
                GridCell next = unassignedNeighbors.get(random.nextInt(unassignedNeighbors.size()));
                grid[next.row()][next.col()] = currentRegion;
                regions.get(currentRegion).add(next);
                activeFronts.add(next);
            }
        }
        return new RegionResult(grid, regions);
    }

    private static RegionResult growVoronoi(int[][] grid, List<List<GridCell>> regions, List<GridCell> seeds, RegionGenConfig config, Random random) throws ThreadTerminationException {
        int[] regionSizes = new int[config.numRegions()];

        for (int r = 0; r < config.height(); r++) {
            for (int c = 0; c < config.width(); c++) {
                if (grid[r][c] == -2) continue;

                int regionId = getClosestRegionId(r, c, seeds);
                grid[r][c] = regionId;
                regions.get(regionId).add(new GridCell(r, c));
                regionSizes[regionId]++;
            }
        }

        for (int i = 0; i < config.swapIterations(); i++) {
            checkInterruption();
            int r = random.nextInt(config.height());
            int c = random.nextInt(config.width());
            int currentRegion = grid[r][c];

            if (regionSizes[currentRegion] <= config.minRegionSize()) continue;

            List<GridCell> difRegionNeighbors = getDifferentNeighbors(grid, r, c, currentRegion, config.dirs());
            if (difRegionNeighbors.isEmpty()) continue;

            GridCell targetNeighbor = difRegionNeighbors.get(random.nextInt(difRegionNeighbors.size()));
            int targetRegion = grid[targetNeighbor.row()][targetNeighbor.col()];

            if (canRemoveCell(grid, r, c, currentRegion, config.dirs())) {
                grid[r][c] = targetRegion;

                GridCell cellToMove = new GridCell(r, c);
                regions.get(currentRegion).remove(cellToMove);
                regions.get(targetRegion).add(cellToMove);
                regionSizes[currentRegion]--;
                regionSizes[targetRegion]++;
            }
        }
        return new RegionResult(grid, regions);
    }

    private static List<GridCell> placeSeeds(int[][] grid, RegionGenConfig config, Random random) throws ThreadTerminationException {
        List<GridCell> seeds = new ArrayList<>();
        List<GridCell> availableCells = new ArrayList<>();

        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[0].length; c++) {
                if (grid[r][c] == -1) availableCells.add(new GridCell(r, c));
            }
        }

        if (availableCells.isEmpty()) return seeds;

        int target = Math.min(config.numRegions(), availableCells.size());
        double minDistance = Math.sqrt(config.minRegionSize());

        int attempts = 0;
        while (seeds.size() < target && attempts < target * 100) {
            checkInterruption();
            GridCell candidate = availableCells.get(random.nextInt(availableCells.size()));

            if (isFarEnough(seeds, candidate.row(), candidate.col(), minDistance)) {
                seeds.add(candidate);
            }
            attempts++;
        }

        while (seeds.size() < target) {
            GridCell candidate = availableCells.get(random.nextInt(availableCells.size()));
            if (!seeds.contains(candidate)) seeds.add(candidate);
        }

        return seeds;
    }

    private static boolean isFarEnough(List<GridCell> seeds, int r, int c, double minDistance) {
        for (GridCell s : seeds) {
            double dist = Math.hypot(r - s.row(), c - s.col());
            if (dist < minDistance) return false;
        }
        return true;
    }

    private static List<GridCell> getNeighbors(int[][] grid, GridCell cell, int[][] dirs, int targetValue) {
        List<GridCell> list = new ArrayList<>();
        int height = grid.length;
        int width = grid[0].length;

        for (int[] d : dirs) {
            int nr = cell.row() + d[0];
            int nc = cell.col() + d[1];
            if (nr >= 0 && nr < height && nc >= 0 && nc < width && grid[nr][nc] == targetValue) {
                list.add(new GridCell(nr, nc));
            }
        }
        return list;
    }

    private static List<GridCell> getUnassignedNeighbors(int[][] grid, List<GridCell> region, int size, int[][] dirs) {
        Set<GridCell> neighbors = new HashSet<>();

        for (GridCell cell : region) {
            for (int[] dir : dirs) {
                int nRow = cell.row() + dir[0];
                int nCol = cell.col() + dir[1];

                if (nRow >= 0 && nRow < size && nCol >= 0 && nCol < size && grid[nRow][nCol] == -1) {
                    neighbors.add(new GridCell(nRow, nCol));
                }
            }
        }
        return new ArrayList<>(neighbors);
    }

    private static List<GridCell> getDifferentNeighbors(int[][] grid, int r, int c, int myRegion, int[][] dirs) {
        List<GridCell> list = new ArrayList<>();
        int height = grid.length;
        int width = grid[0].length;

        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            if (nr >= 0 && nr < height && nc >= 0 && nc < width && grid[nr][nc] != myRegion) {
                list.add(new GridCell(nr, nc));
            }
        }
        return list;
    }

    private static int getClosestRegionId(int r, int c, List<GridCell> seeds) {
        int bestId = 0;
        int minDist = Integer.MAX_VALUE;

        for (int i = 0; i < seeds.size(); i++) {
            GridCell s = seeds.get(i);
            int dist = Math.abs(r - s.row()) + Math.abs(c - s.col());
            if (dist < minDist) {
                minDist = dist;
                bestId = i;
            }
        }
        return bestId;
    }

    private static boolean canRemoveCell(int[][] grid, int r, int c, int regionId, int[][] dirs) {
        int height = grid.length;
        int width = grid[0].length;
        List<GridCell> sameRegionNeighbors = new ArrayList<>();

        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            if (nr >= 0 && nr < height && nc >= 0 && nc < width && grid[nr][nc] == regionId) {
                sameRegionNeighbors.add(new GridCell(nr, nc));
            }
        }

        if (sameRegionNeighbors.size() <= 1) return true;

        Set<GridCell> reachable = new HashSet<>();
        Queue<GridCell> queue = new LinkedList<>();

        GridCell start = sameRegionNeighbors.getFirst();
        queue.add(start);
        reachable.add(start);

        while (!queue.isEmpty()) {
            GridCell curr = queue.poll();
            for (int[] d : dirs) {
                int nr = curr.row() + d[0];
                int nc = curr.col() + d[1];

                if (nr < 0 || nr >= height || nc < 0 || nc >= width) continue;
                if (grid[nr][nc] != regionId || (nr == r && nc == c)) continue;

                GridCell key = new GridCell(nr, nc);
                if (reachable.add(key)) {
                    queue.add(key);
                }
            }
        }

        for (GridCell neighbor : sameRegionNeighbors) {
            if (!reachable.contains(neighbor)) return false;
        }
        return true;
    }

    private static void checkInterruption() throws ThreadTerminationException {
        if (Thread.currentThread().isInterrupted()) {
            throw new ThreadTerminationException();
        }
    }

    public static int[][] generateIrregularRegions(int size, Random random) throws ThreadTerminationException {
        var strategy = GrowthStrategy.ORGANIC;
        RegionGenConfig regionGenConfig = new RegionGenConfig(size, size, size, size, -1, strategy);
        int[][] grid = new int[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                grid[r][c] = -1;
            }
        }

        List<List<GridCell>> regions = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            regions.add(new ArrayList<>());
        }

        List<GridCell> seeds = placeSeeds(grid, regionGenConfig, random);
        return growOrganicLimited(grid, regions, seeds, regionGenConfig, random).grid();
    }
}
