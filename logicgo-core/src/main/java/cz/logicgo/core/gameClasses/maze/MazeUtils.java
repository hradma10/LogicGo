package cz.logicgo.core.gameClasses.maze;

import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.gameModes.*;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.HexagonalGrid;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.RectangularGrid;
import cz.logicgo.core.gameClasses.maze.dataStructures.helpers.MazeGridConstructor;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.HexagonalDirection;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.MazeDirection;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.RectangularDirection;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape.HEXAGONAL;
import static cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape.RECTANGULAR;

public class MazeUtils {
    public static List<MazeCell> getUnvisitedNeighbors(List<MazeCell> neighbors) {
        List<MazeCell> unvisitedNeighbors = new ArrayList<>();
        for (MazeCell neighbor : neighbors) {
            if (neighbor.getLinked().isEmpty()) {
                unvisitedNeighbors.add(neighbor);
            }
        }
        return unvisitedNeighbors;
    }


    public static List<MazeCell> getVisitedNeighbors(List<MazeCell> neighbors) {
        List<MazeCell> unvisitedNeighbors = new ArrayList<>();
        for (MazeCell neighbor : neighbors) {
            if (!neighbor.getLinked().isEmpty()) {
                unvisitedNeighbors.add(neighbor);
            }
        }
        return unvisitedNeighbors;
    }

    public static int[][] makeEmptyMask(int height, int width) {
        int[][] mask = new int[height][width];
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                mask[r][c] = 1;
            }
        }
        return mask;
    }

    public static boolean isFloorCompleted(MazeGrid grid) {
        if (grid == null) return false;

        Deque<MazeCell> activePath = grid.getPath().getActivePath();
        if (activePath.isEmpty() || activePath.peek() != grid.getEndCell()) {
            return false;
        }

        List<MazeCell> chronologicalPath = new ArrayList<>();
        Iterator<MazeCell> iterator = activePath.descendingIterator();
        while (iterator.hasNext()) {
            chronologicalPath.add(iterator.next());
        }

        List<MazeCell> solutionPath = grid.getPath().getSolutionPath();
        List<MazeCell> cleanFloorPath = removeCycles(chronologicalPath);

        if (solutionPath != null && !cleanFloorPath.equals(solutionPath)) {
            return false;
        }

        return checkModifiersForFloor(grid, chronologicalPath);
    }

    public static Map<MazeCell, Integer> getTopDistances(Map<MazeCell, Integer> distances, double percentage) {
        try {
            if (distances == null || distances.isEmpty()) {
                return Map.of();
            }

            double ratio = (percentage > 1.0) ? percentage / 100.0 : percentage;

            long limit = (long) Math.ceil(distances.size() * ratio);

            return distances.entrySet().stream()
                    .filter(Objects::nonNull)
                    .sorted(Map.Entry.<MazeCell, Integer>comparingByValue().reversed()
                            .thenComparing(e -> e.getKey().getRow())
                            .thenComparing(e -> e.getKey().getCol()))
                    .limit(limit)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));
        } catch (Exception e) {
            return distances;
        }

    }

    public static boolean isNotOnEdge(MazeCell mazeCell, int rowCount, int colCount) {
        int row = mazeCell.getRow();
        int col = mazeCell.getCol();

        return row != 0 && row != rowCount - 1 && col != 0 && col != colCount - 1;

    }

    public record CellAndDirection(MazeCell cell, MazeDirection direction) {
    }

    public static MazeDirection getDirectionToOutside(MazeCell cell, MazeGrid mazeGrid, Random random) {
        List<MazeDirection> possible = new ArrayList<>();

        int r = cell.getRow();
        int c = cell.getCol();

        int[][] mask = mazeGrid.getMask();

        switch (mazeGrid.getMazeShape()) {
            case RECTANGULAR -> {
                for (RectangularDirection dir : RectangularDirection.values()) {
                    int targetRow = r + dir.getDy();
                    int targetCol = c + dir.getDx();

                    if (isTargetOutside(targetRow, targetCol, mazeGrid, mask)) {
                        possible.add(dir);
                    }
                }
            }

            case HEXAGONAL -> {
                for (HexagonalDirection dir : HexagonalDirection.values()) {
                    int targetRow = r + dir.getDy();
                    int targetCol = c + dir.getDx();

                    if (isTargetOutside(targetRow, targetCol, mazeGrid, mask)) {
                        possible.add(dir);
                    }
                }
            }
        }

        if (possible.isEmpty()) {
            return null;
        }

        return possible.size() == 1 ? possible.getFirst() : possible.get(random.nextInt(possible.size()));
    }

    private static boolean isTargetOutside(int r, int c, MazeGrid grid, int[][] mask) {
        if (r < 0 || r >= grid.getRowCount() || c < 0 || c >= grid.getColCount()) {
            return true;
        }

        return mask[r][c] == 0;
    }

    private static boolean isOutOfBounds(int r, int c, MazeGrid grid) {
        return r < 0 || r >= grid.getRowCount() || c < 0 || c >= grid.getColCount();
    }

    public static MazeGrid returnNewInstance(MazeGridConstructor builder) {
        return switch (builder.getMazeShape()) {
            case RECTANGULAR -> new RectangularGrid(builder);
            case HEXAGONAL -> new HexagonalGrid(builder);
        };
    }

    public static int countWalls(MazeGrid mazeGrid, WallModifier wallModifier) {
        if (wallModifier == null) return 0;
        int count = 0;
        var history = mazeGrid.getPath().getActivePath().stream().toList();
        for (RegionEdge edges : wallModifier.getRegionEdges()) {
            if (edges.isTraversed(history)) {
                count++;
            }
        }
        return count;
    }

    public static int countCollectedOrderedCheckpoints(MazeGrid mazeGrid, CheckpointModifier checkpointModifier) {
        if (checkpointModifier == null) return 0;
        int nextExpected = 1;

        List<MazeCell> chronologicalPath = new ArrayList<>();
        Iterator<MazeCell> it = mazeGrid.getPath().getActivePath().descendingIterator();
        while (it.hasNext()) {
            chronologicalPath.add(it.next());
        }

        for (MazeCell visitedCell : chronologicalPath) {
            MazeCell expectedCell = checkpointModifier.getCheckpoints().get(nextExpected);
            if (expectedCell != null && expectedCell.equals(visitedCell)) {
                nextExpected++;
            }
        }

        return nextExpected - 1;
    }


    public static int countSteps(MazeGrid mazeGrid, ExactStepsModifier exactStepsModifier) {
        if (exactStepsModifier == null) return 0;
        return mazeGrid.getPath().getActivePath().size() - 1;
    }

    public static int countCurrentCoins(MazeGrid mazeGrid, TollModifier tollModifier) {
        if (tollModifier == null) return 0;

        int balance = 0;
        Set<MazeCell> processedCells = new HashSet<>();

        List<MazeCell> chronologicalPath = new ArrayList<>();
        Iterator<MazeCell> it = mazeGrid.getPath().getActivePath().descendingIterator();
        while (it.hasNext()) {
            chronologicalPath.add(it.next());
        }

        for (MazeCell cell : chronologicalPath) {
            if (tollModifier.tolls().containsKey(cell) && !processedCells.contains(cell)) {
                balance += tollModifier.tolls().get(cell);
                processedCells.add(cell);
            }
        }
        return balance;
    }

    public static int countPatternProgress(MazeGrid mazeGrid, PatternModifier patternModifier) {

        if (patternModifier == null) return 0;

        int nextExpectedPattern = 0;
        int patternLength = patternModifier.getPatternCount();

        Deque<MazeCell> path = mazeGrid.getPath().getActivePath();

        for (MazeCell visitedCell : path.reversed()) {
            Integer cellPattern = patternModifier.getCellPatterns().get(visitedCell);

            if (cellPattern != null && cellPattern == nextExpectedPattern) {
                nextExpectedPattern = (nextExpectedPattern + 1) % patternLength;
            }
        }

        return nextExpectedPattern;
    }

    public static int[][] initAllTrue(int rowCount, int colCount) {
        int[][] mask = new int[rowCount][colCount];

        Arrays.fill(mask[0], 1);

        IntStream.range(1, rowCount).forEach(i -> System.arraycopy(mask[0], 0, mask[i], 0, colCount));

        return mask;
    }

    public static boolean checkFinishedMaze(Maze maze) {
        for (var floor : maze.getMazeGridFloors()) {
            if (!isFloorCompleted(floor.getMazeGrid())) {
                return false;
            }
        }
        return true;
    }

    private static List<MazeCell> removeCycles(List<MazeCell> path) {
        List<MazeCell> clean = new ArrayList<>();
        for (MazeCell cell : path) {
            if (clean.contains(cell)) {
                int index = clean.indexOf(cell);
                clean = new ArrayList<>(clean.subList(0, index + 1));
            } else {
                clean.add(cell);
            }
        }
        return clean;
    }

    private static boolean checkModifiersForFloor(MazeGrid grid, List<MazeCell> chronologicalPath) {
        CheckpointModifier checkpointMod = null;
        TollModifier tollMod = null;
        PatternModifier patternMod = null;
        OneWayModifier oneWayMod = null;
        ExactStepsModifier exactStepsModifier = null;

        for (var mod : grid.getModifiers()) {
            if (mod instanceof CheckpointModifier c) checkpointMod = c;
            if (mod instanceof TollModifier t) tollMod = t;
            if (mod instanceof PatternModifier p) patternMod = p;
            if (mod instanceof OneWayModifier o) oneWayMod = o;
            if (mod instanceof ExactStepsModifier e) exactStepsModifier = e;
        }

        if (checkpointMod != null) {
            int expectedCheckpointIndex = 0;
            int totalCheckpoints = checkpointMod.getCheckpoints().size();
            for (MazeCell cell : chronologicalPath) {
                if (expectedCheckpointIndex < totalCheckpoints) {
                    if (cell.equals(checkpointMod.getCheckpoints().get(expectedCheckpointIndex))) {
                        expectedCheckpointIndex++;
                    }
                }
            }
            if (expectedCheckpointIndex < totalCheckpoints) return false;
        }
        if (patternMod != null) {
            int patternStep = 0;
            int patternLength = patternMod.getPatternCount();
            for (MazeCell cell : chronologicalPath) {
                Integer cellSymbol = patternMod.getCellPatterns().get(cell);
                if (cellSymbol != null) {
                    if (cellSymbol != (patternStep % patternLength)) return false;
                    patternStep++;
                }
            }
        }
        if (exactStepsModifier != null) {
            int steps = chronologicalPath.isEmpty() ? 0 : chronologicalPath.size() - 1;
            return steps == exactStepsModifier.targetSteps();
        }

        return true;
    }

    public static List<MazeCell> getEdges(MazeGrid mazeGrid) {
        if (mazeGrid == null) return new ArrayList<>();

        int[][] mask = mazeGrid.getMask();
        int rows = mazeGrid.getRowCount();
        int cols = mazeGrid.getColCount();

        List<MazeCell> edgeCells = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (mask[r][c] != 1) continue;

                boolean isEdge = false;
                for (int[] dir : directions) {
                    int nr = r + dir[0];
                    int nc = c + dir[1];

                    if (nr < 0 || nr >= rows || nc < 0 || nc >= cols || mask[nr][nc] == 0) {
                        isEdge = true;
                        break;
                    }
                }

                if (isEdge) {
                    MazeCell cell = mazeGrid.getCell(r, c);
                    if (cell != null) {
                        edgeCells.add(cell);
                    }
                }
            }
        }
        return edgeCells;
    }

    public static void insertNeighborsToCells(MazeGrid mazeGrid, int rowCount, int colCount, MazeCell[][] finalGrid) {
        MazeShape shape = mazeGrid.getMazeShape();
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                MazeCell cell = finalGrid[r][c];
                if (cell == null) continue;

                switch (shape) {
                    case RECTANGULAR -> linkRect(r, c, rowCount, colCount, finalGrid, cell);
                    case HEXAGONAL -> linkHex(r, c, rowCount, colCount, finalGrid, cell);
                }
            }
        }
    }

    private static void linkRect(int r, int c, int rs, int cs, MazeCell[][] grid, MazeCell cell) {
        for (RectangularDirection dir : RectangularDirection.values()) {
            putNeighbor(grid, cell, dir, r + dir.getDy(), c + dir.getDx(), rs, cs);
        }
    }

    private static void linkHex(int r, int c, int rs, int cs, MazeCell[][] grid, MazeCell cell) {
        boolean isOdd = (c % 2 != 0);

        for (HexagonalDirection dir : HexagonalDirection.values()) {
            int nr = r;
            int nc = c;

            switch (dir) {
                case NORTH -> nr = r - 1;
                case SOUTH -> nr = r + 1;
                case NORTH_WEST -> {
                    nc = c - 1;
                    nr = isOdd ? r : r - 1;
                }
                case SOUTH_WEST -> {
                    nc = c - 1;
                    nr = isOdd ? r + 1 : r;
                }
                case NORTH_EAST -> {
                    nc = c + 1;
                    nr = isOdd ? r : r - 1;
                }
                case SOUTH_EAST -> {
                    nc = c + 1;
                    nr = isOdd ? r + 1 : r;
                }
            }
            putNeighbor(grid, cell, dir, nr, nc, rs, cs);
        }
    }

    private static void putNeighbor(MazeCell[][] grid, MazeCell cell, MazeDirection dir, int nr, int nc, int rows, int cols) {
        if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
            MazeCell neighbor = grid[nr][nc];
            if (neighbor != null) {
                cell.put(dir, neighbor);
            } else {
            }
        }
    }

    public static void insertNeighborsFromOldGrid(MazeGrid oldMazeGrid, MazeCell[][] newGrid) {
        int rowCount = oldMazeGrid.getRowCount();
        int colCount = oldMazeGrid.getColCount();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                MazeCell oldCell = oldMazeGrid.getCell(r, c);
                MazeCell newCell = newGrid[r][c];

                if (oldCell == null || newCell == null) continue;

                for (MazeDirection dir : oldCell.getNeighbourDirections()) {
                    MazeCell oldNeighbor = oldCell.getNeighbourFromDirection(dir);

                    if (oldNeighbor != null) {
                        int nRow = oldNeighbor.getRow();
                        int nCol = oldNeighbor.getCol();

                        MazeCell newNeighbor = newGrid[nRow][nCol];

                        newCell.put(dir, newNeighbor);
                    }
                }
            }
        }
    }


    public record MazeAssociations(
            HashMap<Integer, List<MazeCell>> cellsToRegion,
            HashMap<MazeCell, Integer> cellToRegion,
            Map<Integer, Map<Integer, List<RegionEdge>>> edgesToRegions,
            Map<Integer, Set<Integer>> assocSet,
            Map<MazeCell, List<MazeCell>> neighboringCells
    ) {
    }

    public static Comparator<MazeCell> rowColComparator = Comparator
            .comparingInt(MazeCell::getRow)
            .thenComparingInt(MazeCell::getCol);


    public record RegionEdge(int regionA, MazeCell cellA, int regionB, MazeCell cellB) {

        public RegionEdge {
            if (regionA > regionB) {

                int tempRegion = regionA;
                regionA = regionB;
                regionB = tempRegion;

                MazeCell tempCell = cellA;
                cellA = cellB;
                cellB = tempCell;
            }
        }

        public boolean isTraversed(List<MazeCell> history) {
            for (int i = 0; i < history.size() - 1; i++) {
                MazeCell current = history.get(i);
                MazeCell next = history.get(i + 1);

                if ((current == cellA && next == cellB) || (current == cellB && next == cellA)) {
                    return true;
                }
            }
            return false;
        }


        @Override
        public boolean equals(Object o) {
            if (!(o instanceof RegionEdge(int a, MazeCell cellA, int b, MazeCell cellB))) return false;
            return regionA() == a && regionB() == b && Objects.equals(cellA(), cellA) && Objects.equals(cellB(), cellB);
        }

        @Override
        public int hashCode() {
            return Objects.hash(regionA(), cellA(), regionB(), cellB());
        }
    }

    public static List<RegionEdge> getOneEdgePerNeighborPair(MazeAssociations mazeAssociations, Random random) {
        List<RegionEdge> edges = new ArrayList<>();

        Map<Integer, Map<Integer, List<RegionEdge>>> map = mazeAssociations.edgesToRegions();

        Comparator<RegionEdge> edgeComparator = Comparator
                .comparing(RegionEdge::cellA, rowColComparator)
                .thenComparing(RegionEdge::cellB, rowColComparator);

        for (Map.Entry<Integer, Map<Integer, List<RegionEdge>>> entry : map.entrySet()) {
            int regionA = entry.getKey();
            Map<Integer, List<RegionEdge>> neighborsMap = entry.getValue();

            for (Map.Entry<Integer, List<RegionEdge>> neighborEntry : neighborsMap.entrySet()) {
                int regionB = neighborEntry.getKey();

                if (regionA < regionB) {
                    List<RegionEdge> allEdgesBetween = neighborEntry.getValue();

                    if (!allEdgesBetween.isEmpty()) {

                        allEdgesBetween.sort(edgeComparator);

                        int randomIndex = random.nextInt(allEdgesBetween.size());
                        edges.add(allEdgesBetween.get(randomIndex));
                    }
                }
            }
        }

        return edges;
    }

    public static MazeAssociations getCellsNeighboringASubGrid(MazeGrid mazeGrid) {
        int[][] mask = mazeGrid.getMask();
        int rows = mazeGrid.getRowCount();
        int cols = mazeGrid.getColCount();
        HashMap<Integer, List<MazeCell>> allCellsToRegion = new HashMap<>();
        HashMap<MazeCell, Integer> cellToRegion = new HashMap<>();
        HashMap<Integer, Map<Integer, List<RegionEdge>>> map = new HashMap<>();
        HashMap<Integer, Set<Integer>> neighbours = new HashMap<>();
        HashMap<MazeCell, List<MazeCell>> neighboringCells = new HashMap<>();

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};


        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                int index = mask[r][c];

                MazeCell currentCell = mazeGrid.getCell(r, c);
                if (currentCell == null) continue;

                Map<Integer, List<RegionEdge>> assocMap = map.computeIfAbsent(index, k -> new HashMap<>());
                Set<Integer> assocSet = neighbours.computeIfAbsent(index, k -> new HashSet<>());
                allCellsToRegion.computeIfAbsent(index, k -> new ArrayList<>()).add(currentCell);
                cellToRegion.put(currentCell, index);

                for (int[] dir : directions) {
                    int nr = r + dir[0];
                    int nc = c + dir[1];

                    if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) {
                        continue;
                    }

                    MazeCell neighborCell = mazeGrid.getCell(nr, nc);
                    if (neighborCell == null) {
                        continue;
                    }

                    int indexOfNeighbor = mask[nr][nc];

                    if (index == indexOfNeighbor) continue;

                    assocSet.add(indexOfNeighbor);
                    RegionEdge edge = new RegionEdge(index, currentCell, indexOfNeighbor, neighborCell);

                    assocMap.computeIfAbsent(indexOfNeighbor, k -> new ArrayList<>()).add(edge);
                    neighboringCells.computeIfAbsent(currentCell, k -> new ArrayList<>()).add(neighborCell);

                }

            }
        }

        return new MazeAssociations(allCellsToRegion, cellToRegion, map, neighbours, neighboringCells);
    }

    public static List<MazeCell> getGlobalEdges(MazeGrid mazeGrid, int[][] globalMask) {
        List<MazeCell> globalEdgeCells = new ArrayList<>();

        int[][] subMask = mazeGrid.getMask();

        int sidesCount = Math.toIntExact(Arrays.stream(mazeGrid.getMazeShape().getAssociatedDirectionEnum().getEnumConstants()).count());

        for (MazeCell cell : mazeGrid.getFlattenedGrid()) {
            if (cell == null) continue;
            int r = cell.getRow();
            int c = cell.getCol();

            if (subMask[r][c] != 1) continue;

            List<MazeCell> neighbors = cell.getNeighbours();

            boolean isOuter = neighbors.size() < sidesCount;

            if (!isOuter) {
                for (MazeCell n : neighbors) {
                    if (globalMask[n.getRow()][n.getCol()] == -1) {
                        isOuter = true;
                        break;
                    }
                }
            }

            if (isOuter) {
                globalEdgeCells.add(cell);
            }
        }
        return globalEdgeCells;
    }

}
