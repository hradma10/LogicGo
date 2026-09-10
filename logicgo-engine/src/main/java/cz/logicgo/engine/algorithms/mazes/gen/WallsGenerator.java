package cz.logicgo.engine.algorithms.mazes.gen;


import cz.logicgo.core.misc.Pair;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.engine.context.MazeBuildContext;

import java.util.*;

import static cz.logicgo.core.gameClasses.maze.MazeUtils.*;


public class WallsGenerator {

    public static int countAndSavePathsOfLengthN(
            Map<Integer, List<Integer>> graph,
            int currentRegion,
            int endRegion,
            int stepsRemaining,
            Set<Integer> visited,
            List<Integer> currentPath,
            List<Integer> solutionPath,
            MazeBuildContext stepCounter
    ) {
        if (stepCounter != null) stepCounter.stepCounter().increment();

        currentPath.add(currentRegion);
        visited.add(currentRegion);

        int validPathsCount = 0;

        if (stepsRemaining == 0 && currentRegion == endRegion) {
            if (solutionPath.isEmpty()) {
                solutionPath.addAll(currentPath);
            }
            validPathsCount = 1;
        } else if (currentRegion == endRegion) {
            validPathsCount = 0;
        } else if (stepsRemaining <= 0) {
            validPathsCount = 0;
        } else {
            for (int neighbor : graph.getOrDefault(currentRegion, new ArrayList<>())) {
                if (!visited.contains(neighbor)) {
                    validPathsCount += countAndSavePathsOfLengthN(
                            graph, neighbor, endRegion, stepsRemaining - 1, visited, currentPath, solutionPath, stepCounter
                    );

                    if (validPathsCount > 1) break;
                }
            }
        }
        visited.remove(currentRegion);
        currentPath.removeLast();

        return validPathsCount;
    }

    public static int countAllPaths(
            Map<Integer, List<Integer>> graph,
            int currentRegion,
            int endRegion,
            Set<Integer> visited,
            MazeBuildContext stepCounter
    ) {
        if (stepCounter != null) stepCounter.stepCounter().increment();

        if (currentRegion == endRegion) return 1;

        visited.add(currentRegion);
        int pathCount = 0;

        for (int neighbor : graph.getOrDefault(currentRegion, new ArrayList<>())) {
            if (!visited.contains(neighbor)) {
                pathCount += countAllPaths(graph, neighbor, endRegion, visited, stepCounter);
                if (pathCount > 1) break;
            }
        }

        visited.remove(currentRegion);
        return pathCount;
    }

    public static void addMoreBadPaths(
            MazeGrid grid,
            MazeAssociations associations,
            Map<Integer, List<Integer>> graph,
            List<Pair<Integer, Integer>> priorityAlreadyExistingWalls,
            List<RegionEdge> walls,
            int startRegion,
            int endRegion,
            int wallsCount,
            MazeType mazeType,
            MazeBuildContext stepCounter,
            Random random
    ) {
        List<RegionEdge> allPotentialWalls = getOneEdgePerNeighborPair(associations, random);

        for (Pair<Integer, Integer> pair : priorityAlreadyExistingWalls) {
            int firstRegion = pair.getFirst();
            int secondRegion = pair.getSecond();

            for (RegionEdge edge : allPotentialWalls) {
                if ((edge.regionA() == firstRegion && edge.regionB() == secondRegion) ||
                        (edge.regionA() == secondRegion && edge.regionB() == firstRegion)) {
                    walls.add(edge);
                    break;
                }
            }
        }

        for (RegionEdge wall : allPotentialWalls) {
            int regionA = wall.regionA();
            int regionB = wall.regionB();

            if (graph.get(regionA).contains(regionB)) continue;

            graph.get(regionA).add(regionB);
            graph.get(regionB).add(regionA);

            boolean isValid = false;

            if (mazeType == MazeType.WALLS) {
                int pathsOfLengthN = countAndSavePathsOfLengthN(
                        graph, startRegion, endRegion, wallsCount - 1, new HashSet<>(), new ArrayList<>(), new ArrayList<>(), stepCounter
                );
                isValid = (pathsOfLengthN == 1);
            } else {
                int totalPaths = countAllPaths(graph, startRegion, endRegion, new HashSet<>(), stepCounter);
                isValid = (totalPaths == 1);
            }

            if (isValid) {
                walls.add(wall);
            } else {
                graph.get(regionA).remove((Integer) regionB);
                graph.get(regionB).remove((Integer) regionA);
            }
        }
    }

    public record BreakableWallsResult(List<RegionEdge> walls, List<Integer> solutionPath) {
    }

    public record RegionEdgesResult(List<RegionEdge> mainPathEdges, List<RegionEdge> deadEndEdges) {
    }

    public static RegionEdgesResult categorizeEdges(List<RegionEdge> allWalls, List<Integer> solutionPath) {
        List<RegionEdge> mainPath = new ArrayList<>();
        List<RegionEdge> deadEnds = new ArrayList<>();

        for (RegionEdge wall : allWalls) {
            boolean isMain = false;
            for (int i = 0; i < solutionPath.size() - 1; i++) {
                int rA = solutionPath.get(i);
                int rB = solutionPath.get(i + 1);
                if ((wall.regionA() == rA && wall.regionB() == rB) || (wall.regionA() == rB && wall.regionB() == rA)) {
                    isMain = true;
                    break;
                }
            }
            if (isMain) mainPath.add(wall);
            else deadEnds.add(wall);
        }
        return new RegionEdgesResult(mainPath, deadEnds);
    }

    public static BreakableWallsResult generateSeparateRegionPuzzleWalls(
            MazeGrid mazeGrid, MazeAssociations associations, Integer startRegion, Integer endRegion,
            Integer wallsCount, List<Integer> solutionPath, MazeBuildContext stepCounter, Random random) {
        return generateSeparateRegionPuzzle(mazeGrid, associations, startRegion, endRegion, wallsCount, solutionPath, MazeType.WALLS, stepCounter, random);
    }

    public static BreakableWallsResult generateSeparateRegionPuzzle(
            MazeGrid mazeGrid, MazeAssociations associations, Integer startRegion, Integer endRegion,
            Integer wallsCount, List<Integer> solutionPath, MazeBuildContext stepCounter, Random random) {
        return generateSeparateRegionPuzzle(mazeGrid, associations, startRegion, endRegion, wallsCount, solutionPath, mazeGrid.getMazeType(), stepCounter, random);
    }

    public static BreakableWallsResult generateSeparateRegionPuzzle(
            MazeGrid mazeGrid, MazeAssociations associations, Integer startRegion, Integer endRegion,
            Integer wallsCount, List<Integer> solutionPath, MazeType mazeType, MazeBuildContext stepCounter, Random random) {

        Map<Integer, List<Integer>> clearGraph = new HashMap<>();
        for (Integer regionId : associations.assocSet().keySet()) {
            clearGraph.put(regionId, new ArrayList<>());
        }

        List<Pair<Integer, Integer>> existing = new ArrayList<>();

        for (int i = 0; i < solutionPath.size() - 1; i++) {
            int rA = solutionPath.get(i);
            int rB = solutionPath.get(i + 1);

            clearGraph.get(rA).add(rB);
            clearGraph.get(rB).add(rA);

            existing.add(new Pair<>(rA, rB));
        }

        List<RegionEdge> walls = new ArrayList<>();

        addMoreBadPaths(mazeGrid, associations, clearGraph, existing, walls, startRegion, endRegion, wallsCount, mazeType, stepCounter, random);

        return new BreakableWallsResult(walls, solutionPath);
    }

    public static boolean findFirstPath(
            Map<Integer, List<Integer>> fullGraph,
            int currentRegion,
            List<Integer> validEndRegions,
            int stepsRemaining,
            Set<Integer> visited,
            List<Integer> path,
            Random random,
            MazeBuildContext stepCounter
    ) {
        if (stepCounter != null) stepCounter.stepCounter().increment();

        path.add(currentRegion);
        visited.add(currentRegion);

        if (stepsRemaining == 0) {
            if (validEndRegions.contains(currentRegion)) {
                return true;
            } else {
                visited.remove(currentRegion);
                path.removeLast();
                return false;
            }
        }

        List<Integer> neighbors = new ArrayList<>(fullGraph.getOrDefault(currentRegion, new ArrayList<>()));
        Collections.shuffle(neighbors, random);

        for (int neighbor : neighbors) {
            if (!visited.contains(neighbor)) {
                if (findFirstPath(fullGraph, neighbor, validEndRegions, stepsRemaining - 1, visited, path, random, stepCounter)) {
                    return true;
                }
            }
        }

        visited.remove(currentRegion);
        path.removeLast();
        return false;
    }

    public static RegionEdge findEdgeBetween(int region1, int region2, List<RegionEdge> walls) {
        for (RegionEdge edge : walls) {
            if ((edge.regionA() == region1 && edge.regionB() == region2) ||
                    (edge.regionA() == region2 && edge.regionB() == region1)) {
                return edge;
            }
        }
        return null;
    }

    public static MazeCell getCellForRegion(RegionEdge edge, int targetRegion) {
        if (edge == null) return null;
        return edge.regionA() == targetRegion ? edge.cellA() : edge.cellB();
    }
}
