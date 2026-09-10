package cz.logicgo.engine.algorithms.mazes;

import cz.logicgo.core.builders.maze.MazeCreation;
import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.gameClasses.maze.MazeUtils.MazeAssociations;
import cz.logicgo.core.gameClasses.maze.dataStructures.StartAndEnd;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.gameModes.*;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.core.gameClasses.maze.dataStructures.helpers.MazeGridConstructor;
import cz.logicgo.core.misc.Pair;
import cz.logicgo.core.misc.StepCounter;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.gameTypes.maze.LevelType;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeAlgorithm;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.MazeDirection;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.RectangularDirection;
import cz.logicgo.engine.algorithms.games.gen.RegionGenerator;
import cz.logicgo.engine.algorithms.mazes.gen.RegionGenConfig;
import cz.logicgo.engine.algorithms.settings.DifficultyChoosing;
import cz.logicgo.engine.algorithms.genStatistics.GenerationStatistics;
import cz.logicgo.engine.context.MazeBuildContext;

import java.util.*;

import static cz.logicgo.core.gameClasses.maze.MazeUtils.*;
import static cz.logicgo.engine.algorithms.mazes.gen.WallsGenerator.*;
import static cz.logicgo.engine.algorithms.mazes.solvers.MazeValidityChecker.checkValidity;
import static cz.logicgo.engine.algorithms.mazes.solvers.MazeValidityChecker.findPath;


public class MazeGenerator {

    private record RegionSetup(
            MazeGrid mazeGrid,
            Map<Integer, MazeGrid> mazeGrids,
            int[][] mask,
            MazeCell[][] finalGrid,
            MazeAssociations associations,
            int startIndex,
            int endIndex,
            List<Integer> solutionRegionPath
    ) {
    }

    private static RegionSetup buildBaseRegionSetup(
            int floorNumber, MazeCreation mazeCreation, MazeType floorType, Random random,
            boolean isFirstFloor, boolean isLastFloor, MazeCell incomingStartCell, MazeBuildContext globalContext) throws Exception {

        int rowCount = mazeCreation.getHeight();
        int colCount = mazeCreation.getWidth();
        int requestedRegions = mazeCreation.getCounts().get(floorNumber);
        MazeAlgorithm mazeAlgorithm = mazeCreation.getMazeAlgorithm();
        var fullMask = mazeCreation.getMask();
        RegionGenConfig config = DifficultyChoosing.getRegionConfig(rowCount, colCount, fullMask, requestedRegions, mazeCreation.getDifficulty());
        StepCounter floorCounter = new StepCounter();
        var variantKey = new GenerationStatistics.MazeVariantKey(floorType, mazeCreation.getDifficulty(), colCount, rowCount);
        Long limit = GenerationStatistics.getMazeVariantLimit(variantKey);

        if (limit == null || limit == Long.MAX_VALUE) {
            var key = new GenerationStatistics.MazeKey(mazeAlgorithm, mazeCreation.getMazeShape(), colCount, rowCount);
            limit = GenerationStatistics.getMazeLimit(key);
        }

        if (limit != null) {
            floorCounter.setLimit(limit * 3);
        }
        MazeBuildContext floorContext = new MazeBuildContext(floorCounter);

        boolean pathFound = false;
        int layoutAttempts = 0;
        MazeAssociations associations = null;
        Integer startIndex = null, endIndex = null;
        List<Integer> solutionRegionPath = new ArrayList<>();
        Map<Integer, MazeGrid> mazeGrids = null;
        int[][] mask = null;
        MazeCell[][] finalGrid = null;
        Map<Integer, List<MazeCell>> edges = new TreeMap<>();

        while (!pathFound) {
            layoutAttempts++;
            floorContext.checkContext();
            edges.clear();

            Map<Integer, int[][]> mazeCells = new TreeMap<>();
            mazeGrids = new TreeMap<>();
            mask = RegionGenerator.generate(config, fullMask, random).grid();

            for (int r = 0; r < rowCount; r++) {
                for (int c = 0; c < colCount; c++) {
                    int index = mask[r][c];
                    if (index >= 0) mazeCells.computeIfAbsent(index, k -> new int[rowCount][colCount])[r][c] = 1;
                }
            }

            List<Long> subSeeds = new ArrayList<>();
            for (int i = 0; i < mazeCells.size(); i++) subSeeds.add(random.nextLong());

            int seedIndex = 0;
            for (Map.Entry<Integer, int[][]> entry : mazeCells.entrySet()) {
                Random subRandom = new Random(subSeeds.get(seedIndex++));
                MazeGridConstructor builder = new MazeGridConstructor()
                        .setRowCount(rowCount).setColCount(colCount)
                        .setRandomInstance(subRandom)
                        .setMazeShape(mazeCreation.getMazeShape())
                        .setMazeType(floorType).setMask(entry.getValue());
                MazeGrid tempGrid = returnNewInstance(builder);

                MazeBuildContext subContext = new MazeBuildContext(floorCounter);
                MazeGeneratorFactory.getGenerator(mazeAlgorithm).runOn(tempGrid, subRandom, subContext);
                mazeGrids.put(entry.getKey(), tempGrid);
            }

            Set<Integer> globalEdgeRegions = new HashSet<>();
            for (Integer regionId : mazeGrids.keySet()) {
                MazeGrid regionGrid = mazeGrids.get(regionId);
                List<MazeCell> globalEdges = getGlobalEdges(regionGrid, fullMask);

                if (!globalEdges.isEmpty()) {
                    globalEdgeRegions.add(regionId);
                    edges.put(regionId, globalEdges);
                } else {
                    edges.put(regionId, getEdges(regionGrid));
                }
            }

            List<Integer> validEnds = new ArrayList<>();
            if (isLastFloor) {
                validEnds.addAll(globalEdgeRegions);
            } else {
                validEnds.addAll(mazeGrids.keySet());
            }

            if (!isFirstFloor && incomingStartCell != null) {
                startIndex = mask[incomingStartCell.getRow()][incomingStartCell.getCol()];
                validEnds.remove(startIndex);
            } else {
                List<Integer> validStarts = new ArrayList<>(globalEdgeRegions);
                if (validStarts.isEmpty()) continue;

                startIndex = validStarts.get(random.nextInt(validStarts.size()));
                validEnds.remove(startIndex);
            }

            if (validEnds.isEmpty()) {
                continue;
            }

            finalGrid = new MazeCell[rowCount][colCount];
            for (MazeGrid subGrid : mazeGrids.values()) {
                MazeCell[][] subInternalGrid = subGrid.getGrid();
                for (int r = 0; r < rowCount; r++) {
                    for (int c = 0; c < colCount; c++) {
                        if (subInternalGrid[r][c] != null) finalGrid[r][c] = subInternalGrid[r][c];
                    }
                }
            }

            MazeGrid tempGridForAssoc = returnNewInstance(new MazeGridConstructor()
                    .setRowCount(rowCount).setColCount(colCount)
                    .setMazeShape(mazeCreation.getMazeShape()).setMask(mask));
            tempGridForAssoc.setGrid(finalGrid);
            associations = getCellsNeighboringASubGrid(tempGridForAssoc);

            Map<Integer, List<Integer>> fullGraph = new TreeMap<>();
            for (Map.Entry<Integer, Set<Integer>> entry : associations.assocSet().entrySet()) {
                List<Integer> neighbors = new ArrayList<>(entry.getValue());
                Collections.sort(neighbors);
                Collections.shuffle(neighbors, random);
                fullGraph.put(entry.getKey(), neighbors);
            }

            solutionRegionPath.clear();
            pathFound = findFirstPath(fullGraph, startIndex, validEnds, mazeGrids.size() - 1, new HashSet<>(), solutionRegionPath, random, floorContext);

            if (pathFound) {
                endIndex = solutionRegionPath.getLast();

                if (isFirstFloor || incomingStartCell == null) {
                    boolean hasValidStartCell = false;
                    for (MazeCell[] row : mazeGrids.get(startIndex).getGrid()) {
                        for (MazeCell cell : row) {
                            if (cell != null && isCellOnMaskEdge(cell.getRow(), cell.getCol(), rowCount, colCount, fullMask)) {
                                hasValidStartCell = true;
                                break;
                            }
                        }
                        if (hasValidStartCell) break;
                    }
                    if (!hasValidStartCell) {
                        pathFound = false;
                        continue;
                    }
                }

                if (isLastFloor) {
                    boolean hasValidEndCell = false;
                    for (MazeCell[] row : mazeGrids.get(endIndex).getGrid()) {
                        for (MazeCell cell : row) {
                            if (cell != null && isCellOnMaskEdge(cell.getRow(), cell.getCol(), rowCount, colCount, fullMask)) {
                                hasValidEndCell = true;
                                break;
                            }
                        }
                        if (hasValidEndCell) break;
                    }
                    if (!hasValidEndCell) {
                        pathFound = false;
                        continue;
                    }
                }
            }
        }
        globalContext.stepCounter().add(floorCounter.get());

        MazeGrid mazeGrid = returnNewInstance(new MazeGridConstructor()
                .setRowCount(rowCount).setColCount(colCount)
                .setRandomInstance(random).setMazeShape(mazeCreation.getMazeShape())
                .setMazeType(floorType).setMask(fullMask));
        mazeGrid.setGrid(finalGrid);

        MazeGrid cornerStartGrid = mazeGrids.get(startIndex);
        if (!isFirstFloor && incomingStartCell != null) {
            cornerStartGrid.setStartCell(cornerStartGrid.getCell(incomingStartCell.getRow(), incomingStartCell.getCol()));
            cornerStartGrid.setStartDirection(RectangularDirection.NORTH);
        } else {
            List<MazeCell> validStartCells = new ArrayList<>();
            for (MazeCell[] row : cornerStartGrid.getGrid()) {
                for (MazeCell cell : row) {
                    if (cell != null && isCellOnMaskEdge(cell.getRow(), cell.getCol(), rowCount, colCount, fullMask)) {
                        validStartCells.add(cell);
                    }
                }
            }

            CellAndDirection start = chooseStartFromList(cornerStartGrid, validStartCells, random);
            cornerStartGrid.setStartCell(start.cell());
            cornerStartGrid.setStartDirection(start.direction());
        }

        MazeGrid cornerEndGrid = mazeGrids.get(endIndex);
        List<MazeCell> validEndCells = new ArrayList<>();

        if (isLastFloor) {
            for (MazeCell[] row : cornerEndGrid.getGrid()) {
                for (MazeCell cell : row) {
                    if (cell != null && isCellOnMaskEdge(cell.getRow(), cell.getCol(), rowCount, colCount, fullMask)) {
                        validEndCells.add(cell);
                    }
                }
            }
        } else {
            for (MazeCell[] row : cornerEndGrid.getGrid()) {
                for (MazeCell cell : row) {
                    if (cell != null) validEndCells.add(cell);
                }
            }
        }

        MazeCell referenceCell = cornerEndGrid.getStartCell();
        if (referenceCell == null) {
            referenceCell = cornerEndGrid.getRandomCell(random);
        }

        HashMap<MazeCell, Integer> endDistances = checkValidity(cornerEndGrid, floorContext);
        CellAndDirection end = pickEndPoint(cornerEndGrid, referenceCell, validEndCells, endDistances, random);

        if (end == null) {
            end = chooseStartFromList(cornerEndGrid, validEndCells, random);
        }

        cornerEndGrid.setEndCell(end.cell());
        cornerEndGrid.setEndDirection(end.direction());

        mazeGrid.setStartCell(cornerStartGrid.getStartCell());
        mazeGrid.setStartDirection(cornerStartGrid.getStartDirection());
        mazeGrid.setEndCell(cornerEndGrid.getEndCell());
        mazeGrid.setEndDirection(cornerEndGrid.getEndDirection());

        return new RegionSetup(mazeGrid, mazeGrids, mask, finalGrid, associations, startIndex, endIndex, solutionRegionPath);
    }

    private static boolean isCellOnMaskEdge(int r, int c, int rowCount, int colCount, int[][] fullMask) {
        if (r == 0 || r == rowCount - 1 || c == 0 || c == colCount - 1) {
            return true;
        }

        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        for (int i = 0; i < 4; i++) {
            int nr = r + dr[i];
            int nc = c + dc[i];

            if (fullMask[nr][nc] < 0) {
                return true;
            }
        }
        return false;
    }


    public static Maze createMaze(MazeCreation mazeCreation, MazeBuildContext stepCounter) {
        Maze maze = new Maze(mazeCreation);
        maze.setMazeType(mazeCreation.getMazeType());
        Random random = maze.getRandomInstance();

        MazeType mainMazeType = mazeCreation.getMazeType();
        int floorCount = mazeCreation.getFloorCount();
        maze.setHasMultipleFloors(floorCount > 1);

        List<MazeGrid> generatedFloors = new ArrayList<>();
        HashMap<Integer, Set<MazeCell>> validCellsMap = new HashMap<>();

        try {
            List<Long> floorSeeds = new ArrayList<>();
            if (floorCount == 0) floorCount = 1;
            for (int i = 0; i < floorCount; i++) floorSeeds.add(random.nextLong());

            MazeCell incomingStartCell = null;

            var key = new GenerationStatistics.MazeVariantKey(mainMazeType, mazeCreation.getDifficulty(), mazeCreation.getWidth(), mazeCreation.getHeight());
            Long variantLimit = GenerationStatistics.getMazeVariantLimit(key);
            if (variantLimit != null) {
                stepCounter.stepCounter().setLimit(variantLimit);
            }

            for (int i = 0; i < floorCount; i++) {
                boolean isFirstFloor = (i == 0);
                boolean isLastFloor = (i == floorCount - 1);
                Random floorRandom = new Random(floorSeeds.get(i));

                MazeType floorType = (floorCount > 1) ? mazeCreation.getMazeTypes().get(i) : mazeCreation.getMazeType();

                MazeGrid currentFloor = generateFloorGrid(i, mazeCreation, floorType, floorRandom, isFirstFloor, isLastFloor, incomingStartCell, stepCounter);
                if (currentFloor == null) return null;

                currentFloor.setFloorNumber(i);
                HashMap<MazeCell, Integer> distances = checkValidity(currentFloor, stepCounter);
                validCellsMap.put(i, distances.keySet());

                generatedFloors.add(currentFloor);
                if (!isLastFloor) incomingStartCell = currentFloor.getEndCell();
            }

            if (floorCount > 1) {
                for (int i = 0; i < generatedFloors.size(); i++) {
                    MazeGrid current = generatedFloors.get(i);
                    if (i != 0) current.setPreviousGrid(generatedFloors.get(i - 1));
                    if (i != generatedFloors.size() - 1) current.setNextGrid(generatedFloors.get(i + 1));
                }
            }

            if (floorCount == 1) {
                maze.setMazeGridFloor(generatedFloors.getFirst());
            } else {
                maze.setMazeGridFloors(generatedFloors);
            }

            MazeGrid firstFloor = generatedFloors.getFirst();
            MazeGrid lastFloor = generatedFloors.getLast();

            maze.setStartCell(firstFloor.getStartCell());
            maze.setStartDirection(firstFloor.getStartDirection());
            maze.setEndCell(lastFloor.getEndCell());
            maze.setEndDirection(lastFloor.getEndDirection());

            finalizeValidCells(maze, validCellsMap, mainMazeType);
            return maze;

        } catch (Exception e) {
            return null;
        }
    }

    private static List<MazeCell> buildSolutionPathFinal(List<Integer> actualRegionPath, Map<Integer, MazeGrid> mazeGrids, List<RegionEdge> correctWalls, MazeBuildContext stepCounter) {
        List<MazeCell> solutionPathFinal = new ArrayList<>();
        for (int i = 0; i < actualRegionPath.size(); i++) {
            if (stepCounter != null) stepCounter.stepCounter().increment();

            int rId = actualRegionPath.get(i);
            MazeGrid g = mazeGrids.get(rId);
            MazeCell s = null, e = null;

            if (actualRegionPath.size() == 1) {
                s = g.getStartCell();
                e = g.getEndCell();
            } else if (i == 0) {
                s = g.getStartCell();
                RegionEdge edgeOut = findEdgeBetween(rId, actualRegionPath.get(i + 1), correctWalls);
                if (edgeOut != null) e = getCellForRegion(edgeOut, rId);
            } else if (i == actualRegionPath.size() - 1) {
                RegionEdge edgeIn = findEdgeBetween(actualRegionPath.get(i - 1), rId, correctWalls);
                if (edgeIn != null) s = getCellForRegion(edgeIn, rId);
                e = g.getEndCell();
            } else {
                RegionEdge edgeIn = findEdgeBetween(actualRegionPath.get(i - 1), rId, correctWalls);
                RegionEdge edgeOut = findEdgeBetween(rId, actualRegionPath.get(i + 1), correctWalls);
                if (edgeIn != null) s = getCellForRegion(edgeIn, rId);
                if (edgeOut != null) e = getCellForRegion(edgeOut, rId);
            }

            if (s != null && e != null) {
                try {
                    List<MazeCell> localPath = findPath(s, e, stepCounter);
                    if (localPath != null) {
                        solutionPathFinal.addAll(localPath);
                    } else {

                    }
                } catch (ThreadTerminationException ex) {
                    throw new RuntimeException(ex);
                }
            } else {

            }
        }
        return solutionPathFinal;
    }

    private static void applyBraidFactor(Map<Integer, MazeGrid> mazeGrids, int[][] mask, double factor, Random random, MazeBuildContext stepCounter) {
        for (Map.Entry<Integer, MazeGrid> entry : mazeGrids.entrySet()) {
            int rId = entry.getKey();
            MazeGrid g = entry.getValue();

            List<MazeCell> regionCells = g.getFlattenedGrid().stream()
                    .filter(Objects::nonNull)
                    .filter(c -> mask[c.getRow()][c.getCol()] == rId).toList();

            List<Pair<MazeCell, MazeCell>> removableWalls = new ArrayList<>();
            for (MazeCell cell : regionCells) {
                if (stepCounter != null) stepCounter.stepCounter().increment();
                for (MazeCell neighbor : cell.getNeighbours()) {
                    if (mask[neighbor.getRow()][neighbor.getCol()] == rId
                            && !cell.getLinked().contains(neighbor)
                            && cell.getRow() <= neighbor.getRow() && cell.getCol() <= neighbor.getCol()) {
                        removableWalls.add(new Pair<>(cell, neighbor));
                    }
                }
            }

            Collections.shuffle(removableWalls, random);
            int loopsToAdd = (int) (removableWalls.size() * factor);
            for (int j = 0; j < loopsToAdd && j < removableWalls.size(); j++) {
                MazeCell c1 = removableWalls.get(j).getFirst();
                MazeCell c2 = removableWalls.get(j).getSecond();
                c1.link(c2);
                c2.link(c1);
            }
        }
    }

    private static MazeGrid generateFloorGrid(int floorNumber,
                                              MazeCreation creation, MazeType floorType, Random baseRandom, boolean isFirstFloor, boolean isLastFloor, MazeCell incomingStartCell, MazeBuildContext stepCounter) throws Exception {

        int maxRetries = 100;
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                Random attemptRandom = new Random(baseRandom.nextLong());
                MazeBuildContext attemptContext = new MazeBuildContext(new StepCounter());

                MazeGrid grid = switch (floorType) {
                    case CLASSIC ->
                            generateClassicFloor(creation, floorType, attemptRandom, isFirstFloor, incomingStartCell, attemptContext);
                    case WALLS ->
                            generateWallsFloor(floorNumber, creation, attemptRandom, isFirstFloor, isLastFloor, incomingStartCell, attemptContext);
                    case PORTAL ->
                            generatePortalFloor(floorNumber, creation, attemptRandom, attemptContext, isFirstFloor, isLastFloor, incomingStartCell);
                    case PATTERN ->
                            generatePatternFloor(floorNumber, creation, attemptRandom, isFirstFloor, isLastFloor, incomingStartCell, attemptContext);
                    case CHECKPOINT ->
                            generateCheckpointFloor(floorNumber, creation, attemptRandom, isFirstFloor, isLastFloor, incomingStartCell, attemptContext);
                    case EXACT_STEPS ->
                            generateExactStepsFloor(floorNumber, creation, attemptRandom, isFirstFloor, isLastFloor, incomingStartCell, attemptContext);
                    case WRAP_AROUND ->
                            generateWrapAroundFloor(floorNumber, creation, attemptRandom, isFirstFloor, incomingStartCell, attemptContext);
                    default -> throw new IllegalArgumentException("Nepodporovaný typ patra: " + floorType);
                };

                stepCounter.stepCounter().add(attemptContext.stepCounter().get());

                grid.setMazeType(floorType);
                return grid;
            } catch (ThreadTerminationException e) {

                throw e;
            } catch (Exception e) {

            }
        }
        throw new Exception("Nepodařilo se vygenerovat patro " + floorNumber + " ani po " + maxRetries + " pokusech.");
    }

    public static Maze createMaze(MazeCreation mazeCreation) {

        MazeBuildContext localCounter = new MazeBuildContext(new StepCounter());
        return createMaze(mazeCreation, localCounter);
    }

    private static MazeGrid generateExactStepsFloor(
            int floorNumber, MazeCreation creation, Random random,
            boolean isFirstFloor, boolean isLastFloor, MazeCell incomingStartCell, MazeBuildContext stepCounter) throws Exception {

        int targetLength = creation.getCounts().get(floorNumber);
        int rowCount = creation.getHeight();
        int colCount = creation.getWidth();
        var fullMask = creation.getMask();
        MazeGrid mazeGrid = generateClassicFloor(creation, MazeType.EXACT_STEPS, random, isFirstFloor, incomingStartCell, stepCounter);

        List<MazeCell> validEnds = new ArrayList<>();
        MazeCell startCell = mazeGrid.getStartCell();
        if (isFirstFloor || incomingStartCell == null) {
            if (!isCellOnMaskEdge(startCell.getRow(), startCell.getCol(), rowCount, colCount, fullMask)) {
                List<MazeCell> allCells = mazeGrid.getFlattenedGrid().stream().filter(Objects::nonNull).toList();
                boolean foundEdgeStart = false;

                for (MazeCell potentialStart : allCells) {
                    if (isCellOnMaskEdge(potentialStart.getRow(), potentialStart.getCol(), rowCount, colCount, fullMask)) {
                        Map<MazeCell, Integer> distFromPotential = getDistances(mazeGrid, potentialStart, stepCounter);
                        boolean hasTargetDist = distFromPotential.values().stream().anyMatch(v -> v == targetLength);

                        if (hasTargetDist) {
                            startCell = potentialStart;
                            mazeGrid.setStartCell(startCell);
                            mazeGrid.setStartDirection(getDirectionToOutside(startCell, mazeGrid, random));
                            foundEdgeStart = true;
                            break;
                        }
                    }
                }
                if (!foundEdgeStart) {
                    throw new Exception("[RETRY] Nepodařilo se najít startovní buňku na reálném okraji masky s požadovanou délkou.");
                }
            }
        }
        Map<MazeCell, Integer> distFromStart = getDistances(mazeGrid, startCell, stepCounter);
        for (Map.Entry<MazeCell, Integer> entry : distFromStart.entrySet()) {
            if (entry.getValue() == targetLength) validEnds.add(entry.getKey());
        }
        if (validEnds.isEmpty()) {
            List<MazeCell> allCells = mazeGrid.getFlattenedGrid().stream().filter(Objects::nonNull).toList();
            boolean foundPair = false;

            for (MazeCell potentialStart : allCells) {
                if (!isFirstFloor && incomingStartCell != null || isCellOnMaskEdge(potentialStart.getRow(), potentialStart.getCol(), rowCount, colCount, fullMask)) {
                    distFromStart = getDistances(mazeGrid, potentialStart, stepCounter);
                    validEnds.clear();

                    for (Map.Entry<MazeCell, Integer> entry : distFromStart.entrySet()) {
                        if (entry.getValue() == targetLength) validEnds.add(entry.getKey());
                    }

                    if (!validEnds.isEmpty()) {
                        startCell = potentialStart;
                        mazeGrid.setStartCell(startCell);
                        if (isFirstFloor || incomingStartCell == null) {
                            mazeGrid.setStartDirection(getDirectionToOutside(startCell, mazeGrid, random));
                        }
                        foundPair = true;
                        break;
                    }
                }
            }

            if (!foundPair) {
                throw new Exception("[RETRY] V bludišti neexistuje žádná dvojice bodů s přesnou délkou trasy: " + targetLength);
            }
        }
        List<MazeCell> validEndsOnEdge = new ArrayList<>();
        if (isLastFloor) {
            for (MazeCell c : validEnds) {
                if (isCellOnMaskEdge(c.getRow(), c.getCol(), rowCount, colCount, fullMask)) {
                    validEndsOnEdge.add(c);
                }
            }
        } else {
            validEndsOnEdge.addAll(validEnds);
        }
        if (validEndsOnEdge.isEmpty()) {
            throw new Exception("[RETRY] Žádný z validních koncových bodů s délkou " + targetLength + " neleží na reálném okraji masky.");
        }

        MazeCell endCell = validEndsOnEdge.get(random.nextInt(validEndsOnEdge.size()));
        mazeGrid.setEndCell(endCell);

        if (isLastFloor) {
            mazeGrid.setEndDirection(getDirectionToOutside(endCell, mazeGrid, random));
        }
        List<MazeCell> solutionPath = findPathBFS(startCell, endCell, stepCounter);
        Map<MazeCell, Integer> distFromEnd = getDistances(mazeGrid, endCell, stepCounter);

        List<Pair<MazeCell, MazeCell>> potentialWalls = new ArrayList<>();
        for (MazeCell cell : mazeGrid.getFlattenedGrid()) {
            if (cell == null) continue;
            for (MazeCell neighbor : cell.getNeighbours()) {
                if (!cell.getLinked().contains(neighbor) &&
                        cell.getRow() <= neighbor.getRow() && cell.getCol() <= neighbor.getCol()) {
                    potentialWalls.add(new Pair<>(cell, neighbor));
                }
            }
        }

        Collections.shuffle(potentialWalls, random);

        long nonNullCellsCount = mazeGrid.getFlattenedGrid().stream().filter(Objects::nonNull).count();
        int maxWallsToBreak = Math.max(2, (int) (nonNullCellsCount * 0.1));
        int brokenCount = 0;

        for (Pair<MazeCell, MazeCell> wall : potentialWalls) {
            if (brokenCount >= maxWallsToBreak) break;

            MazeCell c1 = wall.getFirst();
            MazeCell c2 = wall.getSecond();

            int possiblePath1 = distFromStart.get(c1) + 1 + distFromEnd.get(c2);
            int possiblePath2 = distFromStart.get(c2) + 1 + distFromEnd.get(c1);

            if (possiblePath1 != targetLength && possiblePath2 != targetLength) {
                c1.link(c2);
                c2.link(c1);
                brokenCount++;

                distFromStart = getDistances(mazeGrid, startCell, stepCounter);
                distFromEnd = getDistances(mazeGrid, endCell, stepCounter);
            }
        }

        mazeGrid.getModifiers().add(new ExactStepsModifier(targetLength));
        mazeGrid.getPath().getSolutionPath().clear();
        mazeGrid.getPath().getSolutionPath().addAll(solutionPath);

        return mazeGrid;
    }

    public static Map<MazeCell, Integer> getDistances(MazeGrid grid, MazeCell start, MazeBuildContext stepCounter) {
        Map<MazeCell, Integer> distances = new HashMap<>();
        if (start == null) return distances;

        Queue<MazeCell> queue = new LinkedList<>();
        queue.add(start);
        distances.put(start, 0);

        while (!queue.isEmpty()) {
            if (stepCounter != null) stepCounter.stepCounter().increment();
            MazeCell current = queue.poll();
            int currentDist = distances.get(current);

            for (MazeCell neighbor : current.getLinked()) {
                if (!distances.containsKey(neighbor)) {
                    distances.put(neighbor, currentDist + 1);
                    queue.add(neighbor);
                }
            }
        }

        return distances;
    }

    public static void removeLocalCycles(MazeGrid mazeGrid, List<MazeCell> mainPath, List<Pair<MazeCell, MazeCell>> edgesToKeep, MazeBuildContext stepCounter) {
        if (mainPath == null || mainPath.isEmpty()) return;

        Set<Pair<MazeCell, MazeCell>> protectedLinks = new HashSet<>();
        for (int i = 0; i < mainPath.size() - 1; i++) {
            MazeCell c1 = mainPath.get(i);
            MazeCell c2 = mainPath.get(i + 1);
            protectedLinks.add(new Pair<>(c1, c2));
            protectedLinks.add(new Pair<>(c2, c1));
        }

        if (edgesToKeep != null) {
            for (Pair<MazeCell, MazeCell> edge : edgesToKeep) {
                protectedLinks.add(new Pair<>(edge.getFirst(), edge.getSecond()));
                protectedLinks.add(new Pair<>(edge.getSecond(), edge.getFirst()));
            }
        }

        Map<MazeCell, MazeCell> parentMap = new HashMap<>();
        Queue<MazeCell> queue = new LinkedList<>();

        Set<MazeCell> uniquePath = new LinkedHashSet<>(mainPath);
        for (MazeCell cell : uniquePath) {
            parentMap.put(cell, null);
            queue.add(cell);
        }

        List<Pair<MazeCell, MazeCell>> linksToRemove = new ArrayList<>();

        while (!queue.isEmpty()) {
            if (stepCounter != null) stepCounter.stepCounter().increment();
            MazeCell current = queue.poll();

            for (MazeCell neighbor : current.getLinked()) {
                if (parentMap.get(current) == neighbor) continue;

                Pair<MazeCell, MazeCell> link = new Pair<>(current, neighbor);

                if (parentMap.containsKey(neighbor)) {
                    if (!protectedLinks.contains(link)) {
                        linksToRemove.add(link);
                    }
                } else {
                    parentMap.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        int removedCount = 0;
        for (Pair<MazeCell, MazeCell> link : linksToRemove) {
            MazeCell c1 = link.getFirst();
            MazeCell c2 = link.getSecond();

            if (c1.getLinked().contains(c2)) {
                c1.unlink(c2);
                c2.unlink(c1);
                removedCount++;
            }
        }

    }

    private static MazeGrid generateCheckpointFloor(int floorNumber,
                                                    MazeCreation mazeCreation, Random random,
                                                    boolean isFirstFloor, boolean isLastFloor, MazeCell incomingStartCell, MazeBuildContext stepCounter) throws Exception {

        RegionSetup setup = buildBaseRegionSetup(floorNumber, mazeCreation, MazeType.CHECKPOINT, random, isFirstFloor, isLastFloor, incomingStartCell, stepCounter);

        BreakableWallsResult generatedPuzzle = generateSeparateRegionPuzzle(
                setup.mazeGrid, setup.associations, setup.startIndex, setup.endIndex, setup.mazeGrids.size(), setup.solutionRegionPath, MazeType.CHECKPOINT, stepCounter, random);

        List<RegionEdge> correctWalls = new ArrayList<>(generatedPuzzle.walls());
        List<Integer> actualPath = generatedPuzzle.solutionPath();

        List<RegionEdge> allPotentialWalls = new ArrayList<>();
        if (setup.associations != null && setup.associations.assocSet() != null) {
            for (Map.Entry<Integer, Set<Integer>> entry : setup.associations.assocSet().entrySet()) {
                int rA = entry.getKey();
                for (int rB : entry.getValue()) {
                    if (rA < rB) {
                        for (int r = 0; r < mazeCreation.getHeight(); r++) {
                            for (int c = 0; c < mazeCreation.getWidth(); c++) {
                                MazeCell cellA = setup.finalGrid[r][c];
                                if (cellA == null || setup.mask[r][c] != rA) continue;

                                for (MazeCell cellB : cellA.getNeighbours()) {
                                    if (cellB != null && setup.mask[cellB.getRow()][cellB.getCol()] == rB) {
                                        allPotentialWalls.add(new RegionEdge(rA, cellA, rB, cellB));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (allPotentialWalls.isEmpty()) {
            allPotentialWalls = getOneEdgePerNeighborPair(setup.associations, random);
        }

        List<Pair<MazeCell, MazeCell>> protectedEdges = new ArrayList<>();
        List<Pair<MazeCell, MazeCell>> oneWayCheckpoints = new ArrayList<>();

        if (mazeCreation.getDifficulty() == Difficulty.HARD) {
            List<RegionEdge> longShortcuts = new ArrayList<>();
            for (RegionEdge edge : allPotentialWalls) {
                int iA = actualPath.indexOf(edge.regionA());
                int iB = actualPath.indexOf(edge.regionB());
                if (iA == -1 || iB == -1) continue;

                int minIdx = Math.min(iA, iB);
                int maxIdx = Math.max(iA, iB);

                if (minIdx <= 1 && maxIdx >= actualPath.size() - 2 && (maxIdx - minIdx) > 1) {
                    longShortcuts.add(edge);
                }
            }

            if (!longShortcuts.isEmpty()) {
                RegionEdge forcedEdge = longShortcuts.get(random.nextInt(longShortcuts.size()));
                correctWalls.add(forcedEdge);

                int rMain = actualPath.get(Math.min(actualPath.indexOf(forcedEdge.regionA()), actualPath.indexOf(forcedEdge.regionB())));
                MazeCell cellA = forcedEdge.cellA();
                MazeCell cellB = forcedEdge.cellB();

                MazeCell from = (setup.mask()[cellA.getRow()][cellA.getCol()] == rMain) ? cellA : cellB;
                MazeCell to = (from == cellA) ? cellB : cellA;

                oneWayCheckpoints.add(new Pair<>(from, to));
                protectedEdges.add(new Pair<>(from, to));
            }
        }
        double baseShortcutChance = switch (mazeCreation.getDifficulty()) {
            case EASY -> 0.40;
            case MEDIUM -> 0.55;
            case HARD -> 0.85;
        };

        for (RegionEdge potentialWall : allPotentialWalls) {
            stepCounter.stepCounter().increment();
            int rA = potentialWall.regionA();
            int rB = potentialWall.regionB();
            int indexA = actualPath.indexOf(rA);
            int indexB = actualPath.indexOf(rB);

            boolean alreadyExists = correctWalls.stream()
                    .anyMatch(e -> (e.regionA() == rA && e.regionB() == rB) || (e.regionA() == rB && e.regionB() == rA));

            if (alreadyExists) continue;

            if (indexA != -1 && indexB != -1) {
                int distance = Math.abs(indexA - indexB);
                if (distance >= 2) {
                    double distanceBonus = (distance >= 4) ? 0.20 : 0.0;
                    double finalChance = Math.min(1.0, baseShortcutChance + distanceBonus);

                    if (random.nextDouble() < finalChance) {
                        correctWalls.add(potentialWall);

                        int indexFrom = Math.min(indexA, indexB);
                        int rFrom = actualPath.get(indexFrom);

                        MazeCell cellA = potentialWall.cellA();
                        MazeCell cellB = potentialWall.cellB();

                        MazeCell from = (setup.mask[cellA.getRow()][cellA.getCol()] == rFrom) ? cellA : cellB;
                        MazeCell to = (from == cellA) ? cellB : cellA;

                        oneWayCheckpoints.add(new Pair<>(from, to));
                        protectedEdges.add(new Pair<>(from, to));
                    }
                }
            } else if (indexA == -1 && indexB == -1) {
                if (random.nextDouble() < baseShortcutChance) {
                    correctWalls.add(potentialWall);
                    protectedEdges.add(new Pair<>(potentialWall.cellA(), potentialWall.cellB()));
                }
            } else {
                if (random.nextDouble() < baseShortcutChance) {
                    correctWalls.add(potentialWall);

                    int rMain = (indexA != -1) ? rA : rB;
                    MazeCell cellA = potentialWall.cellA();
                    MazeCell cellB = potentialWall.cellB();

                    MazeCell from = (setup.mask[cellA.getRow()][cellA.getCol()] == rMain) ? cellA : cellB;
                    MazeCell to = (from == cellA) ? cellB : cellA;

                    oneWayCheckpoints.add(new Pair<>(from, to));
                    protectedEdges.add(new Pair<>(from, to));
                }
            }
        }

        List<MazeCell> solutionPathFinal = buildSolutionPathFinal(actualPath, setup.mazeGrids, correctWalls, stepCounter);

        int minExpectedPathLength = (int) (actualPath.size() * 3.2);
        if (solutionPathFinal.size() < minExpectedPathLength) {
            throw new Exception("[RETRY] Vygenerovaná trasa regiony je příliš krátká (" + solutionPathFinal.size() + " buněk). Pregenerovávám.");
        }

        for (RegionEdge edge : correctWalls) edge.cellA().link(edge.cellB());

        setup.mazeGrid.getPath().getSolutionPath().addAll(solutionPathFinal);

        MazeCell startCell = setup.mazeGrid.getStartCell();
        MazeCell endCell = setup.mazeGrid.getEndCell();

        Map<Integer, MazeCell> orderedCheckpoints = new LinkedHashMap<>();
        int checkpointIndex = 1;
        for (int i = 1; i < actualPath.size() - 1; i++) {
            int rId = actualPath.get(i);
            List<MazeCell> pathInRegion = solutionPathFinal.stream()
                    .filter(c -> setup.mask[c.getRow()][c.getCol()] == rId).filter(c -> c != startCell && c != endCell).toList();

            if (!pathInRegion.isEmpty()) {
                orderedCheckpoints.put(checkpointIndex++, pathInRegion.get(pathInRegion.size() / 2));
            }
        }

        for (int i = 0; i < actualPath.size() - 1; i++) {
            RegionEdge connection = findEdgeBetween(actualPath.get(i), actualPath.get(i + 1), correctWalls);
            if (connection != null) {
                MazeCell from = (setup.mask[connection.cellA().getRow()][connection.cellA().getCol()] == actualPath.get(i))
                        ? connection.cellA() : connection.cellB();
                MazeCell to = (from == connection.cellA()) ? connection.cellB() : connection.cellA();
                oneWayCheckpoints.add(new Pair<>(from, to));
                protectedEdges.add(new Pair<>(from, to));
            }
        }

        removeLocalCycles(setup.mazeGrid, solutionPathFinal, protectedEdges, stepCounter);
        setup.mazeGrid.getModifiers().add(new CheckpointModifier(orderedCheckpoints, oneWayCheckpoints));
        insertNeighborsToCells(setup.mazeGrid, mazeCreation.getHeight(), mazeCreation.getWidth(), setup.finalGrid);

        return setup.mazeGrid;
    }

    private static Set<MazeCell> getReachableCells(MazeCell start, MazeBuildContext stepCounter) {
        Set<MazeCell> visited = new HashSet<>();
        Queue<MazeCell> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {

            if (stepCounter != null) stepCounter.stepCounter().increment();
            MazeCell curr = queue.poll();
            for (MazeCell neighbor : curr.getLinked()) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return visited;
    }

    private static MazeGrid generateWrapAroundFloor(
            int floorNumber, MazeCreation creation, Random random,
            boolean isFirstFloor, MazeCell incomingStartCell, MazeBuildContext stepCounter) throws Exception {

        MazeGrid mazeGrid = generateClassicFloor(creation, MazeType.WRAP_AROUND, random, isFirstFloor, incomingStartCell, stepCounter);

        MazeCell startCell = mazeGrid.getStartCell();
        MazeCell endCell = mazeGrid.getEndCell();

        int totalWraps = creation.getCounts().get(floorNumber);
        int mandatoryWraps;
        int decoyWraps;

        switch (creation.getDifficulty()) {
            case EASY -> {
                mandatoryWraps = totalWraps;
                decoyWraps = 0;
            }
            case MEDIUM -> {
                mandatoryWraps = Math.max(1, (int) Math.ceil(totalWraps / 2.0));
                decoyWraps = totalWraps - mandatoryWraps;
            }
            case HARD -> {
                mandatoryWraps = 1 + random.nextInt(2);
                decoyWraps = Math.max(1, totalWraps - mandatoryWraps);
            }
            default -> {
                mandatoryWraps = 1;
                decoyWraps = 0;
            }
        }

        int maxRow = 0, maxCol = 0;
        for (MazeCell cell : mazeGrid.getFlattenedGrid()) {
            if (cell != null) {
                maxRow = Math.max(maxRow, cell.getRow());
                maxCol = Math.max(maxCol, cell.getCol());
            }
        }
        int maxDim = Math.max(maxRow, maxCol);
        int minDistance = (maxDim < 10) ? 2 : Math.max(3, (maxRow + maxCol) / 5);
        int minWrapSpacing = (maxDim < 10) ? 2 : Math.max(4, (maxRow + maxCol) / 8);

        List<Pair<MazeCell, MazeCell>> potentialWraps = new ArrayList<>();

        for (int r = 0; r <= maxRow; r++) {
            MazeCell leftEdge = mazeGrid.getCell(r, 0);
            MazeCell rightEdge = mazeGrid.getCell(r, maxCol);
            if (isFarEnough(leftEdge, startCell, endCell, minDistance) && isFarEnough(rightEdge, startCell, endCell, minDistance)) {
                potentialWraps.add(new Pair<>(leftEdge, rightEdge));
            }
        }
        for (int c = 0; c <= maxCol; c++) {
            MazeCell topEdge = mazeGrid.getCell(0, c);
            MazeCell bottomEdge = mazeGrid.getCell(maxRow, c);
            if (isFarEnough(topEdge, startCell, endCell, minDistance) && isFarEnough(bottomEdge, startCell, endCell, minDistance)) {
                potentialWraps.add(new Pair<>(topEdge, bottomEdge));
            }
        }
        if (potentialWraps.size() < totalWraps) {
            mandatoryWraps = Math.min(mandatoryWraps, potentialWraps.size());
            decoyWraps = Math.min(decoyWraps, potentialWraps.size() - mandatoryWraps);
        }

        List<Pair<MazeCell, MazeCell>> wrapLinks = new ArrayList<>();
        int successfullyAddedMandatory = 0;
        for (int i = 0; i < mandatoryWraps; i++) {
            stepCounter.stepCounter().increment();
            List<MazeCell> currentPath = findPathBFS(mazeGrid.getStartCell(), mazeGrid.getEndCell(), stepCounter);
            if (currentPath == null || currentPath.size() < 6) break;

            int currentPathLen = currentPath.size();

            Pair<MazeCell, MazeCell> absoluteBestWrap = null;
            MazeCell absoluteBestC1 = null, absoluteBestC2 = null;
            int globalMaxPathLength = -1;
            for (int breakAttempt = 0; breakAttempt < 8; breakAttempt++) {
                int breakIndex = 2 + random.nextInt(currentPath.size() - 5);
                MazeCell c1 = currentPath.get(breakIndex);
                MazeCell c2 = currentPath.get(breakIndex + 1);

                if (Math.abs(c1.getRow() - c2.getRow()) + Math.abs(c1.getCol() - c2.getCol()) != 1) continue;

                c1.unlink(c2);
                Set<MazeCell> startTree = getReachableCells(mazeGrid.getStartCell(), stepCounter);

                List<Pair<MazeCell, MazeCell>> validWraps = new ArrayList<>();
                for (Pair<MazeCell, MazeCell> wrap : potentialWraps) {
                    if (wrapLinks.contains(wrap)) continue;

                    boolean isTooClose = false;
                    for (Pair<MazeCell, MazeCell> existing : wrapLinks) {
                        if (Math.abs(wrap.getFirst().getRow() - existing.getFirst().getRow()) + Math.abs(wrap.getFirst().getCol() - existing.getFirst().getCol()) < minWrapSpacing) {
                            isTooClose = true;
                            break;
                        }
                    }

                    if (!isTooClose && startTree.contains(wrap.getFirst()) != startTree.contains(wrap.getSecond())) {
                        validWraps.add(wrap);
                    }
                }

                Collections.shuffle(validWraps, random);
                int tests = Math.min(15, validWraps.size());
                for (int w = 0; w < tests; w++) {
                    Pair<MazeCell, MazeCell> wrap = validWraps.get(w);
                    wrap.getFirst().link(wrap.getSecond());

                    List<MazeCell> testPath = findPathBFS(mazeGrid.getStartCell(), mazeGrid.getEndCell(), stepCounter);
                    int len = (testPath != null) ? testPath.size() : -1;
                    double requiredMultiplier = (creation.getDifficulty() == Difficulty.HARD) ? 1.1 : 1.0;

                    if (len > globalMaxPathLength && len >= (currentPathLen * requiredMultiplier)) {
                        globalMaxPathLength = len;
                        absoluteBestWrap = wrap;
                        absoluteBestC1 = c1;
                        absoluteBestC2 = c2;
                    }
                    wrap.getFirst().unlink(wrap.getSecond());
                }
                c1.link(c2);
            }

            if (absoluteBestWrap != null) {
                absoluteBestC1.unlink(absoluteBestC2);
                absoluteBestWrap.getFirst().link(absoluteBestWrap.getSecond());
                wrapLinks.add(absoluteBestWrap);
                successfullyAddedMandatory++;
            } else {
                break;
            }
        }
        int missingWraps = mandatoryWraps - successfullyAddedMandatory;
        if (missingWraps > 0) {
            decoyWraps += missingWraps;
        }
        Collections.shuffle(potentialWraps, random);
        int addedDecoys = 0;
        Set<MazeCell> currentStartTree = getReachableCells(mazeGrid.getStartCell(), stepCounter);

        for (Pair<MazeCell, MazeCell> wrap : potentialWraps) {
            if (addedDecoys >= decoyWraps) break;
            if (wrapLinks.contains(wrap)) continue;

            boolean isTooClose = false;
            for (Pair<MazeCell, MazeCell> existing : wrapLinks) {
                if (Math.abs(wrap.getFirst().getRow() - existing.getFirst().getRow()) + Math.abs(wrap.getFirst().getCol() - existing.getFirst().getCol()) < minWrapSpacing) {
                    isTooClose = true;
                    break;
                }
            }
            if (isTooClose) continue;

            boolean isFirstReachable = currentStartTree.contains(wrap.getFirst());
            boolean isSecondReachable = currentStartTree.contains(wrap.getSecond());

            if (isFirstReachable == isSecondReachable) {
                wrap.getFirst().link(wrap.getSecond());
                wrapLinks.add(wrap);
                addedDecoys++;
            }
        }

        mazeGrid.getModifiers().add(new WrapAroundModifier(wrapLinks));
        List<MazeCell> finalPath = findPathBFS(mazeGrid.getStartCell(), mazeGrid.getEndCell(), stepCounter);

        if (finalPath != null) {
            mazeGrid.getPath().getSolutionPath().clear();
            mazeGrid.getPath().getSolutionPath().addAll(finalPath);
        }

        return mazeGrid;
    }

    public static List<MazeCell> findPathBFS(MazeCell start, MazeCell end, MazeBuildContext stepCounter) {
        if (start == null || end == null) return new ArrayList<>();

        Queue<MazeCell> queue = new LinkedList<>();
        Map<MazeCell, MazeCell> cameFrom = new HashMap<>();

        queue.add(start);
        cameFrom.put(start, null);
        boolean found = false;

        while (!queue.isEmpty()) {
            if (stepCounter != null) stepCounter.stepCounter().increment();
            MazeCell current = queue.poll();
            if (current.equals(end)) {
                found = true;
                break;
            }
            for (MazeCell neighbor : current.getLinked()) {
                if (!cameFrom.containsKey(neighbor)) {
                    cameFrom.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        List<MazeCell> path = new ArrayList<>();
        if (found) {
            MazeCell current = end;
            while (current != null) {
                path.add(current);
                current = cameFrom.get(current);
            }
            Collections.reverse(path);
        }
        return path;
    }

    private static boolean isFarEnough(MazeCell cell, MazeCell start, MazeCell end, int minDistance) {
        if (cell == null || start == null || end == null) return false;

        int distToStart = Math.abs(cell.getRow() - start.getRow()) + Math.abs(cell.getCol() - start.getCol());
        int distToEnd = Math.abs(cell.getRow() - end.getRow()) + Math.abs(cell.getCol() - end.getCol());
        return distToStart >= minDistance && distToEnd >= minDistance;
    }

    private static MazeGrid generateClassicFloor(
            MazeCreation creation, MazeType floorType, Random random,
            boolean isFirstFloor, MazeCell incomingStartCell, MazeBuildContext globalContext) throws Exception {

        int rowCount = creation.getHeight();
        int colCount = creation.getWidth();

        int[][] mask = (creation.getMask() != null) ? creation.getMask() : new int[rowCount][colCount];
        if (creation.getMask() == null) {
            for (int[] row : mask) Arrays.fill(row, 1);
        }
        StepCounter floorCounter = new StepCounter();
        var variantKey = new GenerationStatistics.MazeVariantKey(floorType, creation.getDifficulty(), colCount, rowCount);
        Long limit = GenerationStatistics.getMazeVariantLimit(variantKey);

        if (limit == null || limit == Long.MAX_VALUE) {
            var key = new GenerationStatistics.MazeKey(creation.getMazeAlgorithm(), creation.getMazeShape(), colCount, rowCount);
            limit = GenerationStatistics.getMazeLimit(key);
        }
        if (limit != null) {
            floorCounter.setLimit(limit);
        }

        MazeBuildContext floorContext = new MazeBuildContext(floorCounter);

        MazeGridConstructor builder = new MazeGridConstructor()
                .setRowCount(rowCount).setColCount(colCount)
                .setMazeShape(creation.getMazeShape())
                .setMazeType(floorType).setRandomInstance(random).setMask(mask);

        MazeGrid mazeGrid = returnNewInstance(builder);
        MazeGeneratorFactory.getGenerator(creation.getMazeAlgorithm()).runOn(mazeGrid, random, floorContext);
        globalContext.stepCounter().add(floorCounter.get());

        List<MazeCell> edges = getEdges(mazeGrid);

        if (isFirstFloor || incomingStartCell == null) {
            var start = chooseStartFromList(mazeGrid, edges, random);
            mazeGrid.setStartCell(start.cell());
            mazeGrid.setStartDirection(start.direction());
        } else {
            mazeGrid.setStartCell(mazeGrid.getCell(incomingStartCell.getRow(), incomingStartCell.getCol()));
            mazeGrid.setStartDirection(RectangularDirection.NORTH);
        }

        HashMap<MazeCell, Integer> distances = checkValidity(mazeGrid, floorContext);
        CellAndDirection end = pickEndPoint(mazeGrid, mazeGrid.getStartCell(), edges, distances, random);

        if (end != null) {
            mazeGrid.setEndCell(end.cell());
            mazeGrid.setEndDirection(end.direction());
        } else {
            MazeCell fallbackEnd = distances.entrySet().stream()
                    .filter(e -> e.getKey() != mazeGrid.getStartCell())
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(mazeGrid.getRandomCell(random));

            mazeGrid.setEndCell(fallbackEnd);
            mazeGrid.setEndDirection(getDirectionToOutside(fallbackEnd, mazeGrid, random));
        }

        List<MazeCell> path = findPath(mazeGrid.getStartCell(), mazeGrid.getEndCell(), floorContext);
        mazeGrid.getPath().getSolutionPath().addAll(path);

        return mazeGrid;
    }

    private static MazeBuildContext createBuildContext(
            MazeAlgorithm algorithm, MazeShape shape, MazeType mazeType,
            Difficulty difficulty, int[][] mask, StepCounter sharedCounter) {
        return new MazeBuildContext(sharedCounter);
    }

    private static MazeGrid generatePatternFloor(
            int floorNumber, MazeCreation mazeCreation, Random random,
            boolean isFirstFloor, boolean isLastFloor, MazeCell incomingStartCell, MazeBuildContext stepCounter) throws Exception {

        RegionSetup setup = buildBaseRegionSetup(floorNumber, mazeCreation, MazeType.PATTERN, random, isFirstFloor, isLastFloor, incomingStartCell, stepCounter);

        BreakableWallsResult generatedPuzzle = generateSeparateRegionPuzzle(
                setup.mazeGrid, setup.associations, setup.startIndex, setup.endIndex, setup.mazeGrids.size(), setup.solutionRegionPath, MazeType.PATTERN, stepCounter, random);

        List<RegionEdge> correctWalls = generatedPuzzle.walls();
        RegionEdgesResult edgeCategories = categorizeEdges(correctWalls, generatedPuzzle.solutionPath());

        double braidFactor = switch (mazeCreation.getDifficulty()) {
            case EASY -> 0.10;
            case MEDIUM -> 0.20;
            case HARD -> 0.35;
        };

        applyBraidFactor(setup.mazeGrids, setup.mask, braidFactor, random, stepCounter);

        List<Integer> actualRegionPath = generatedPuzzle.solutionPath();

        List<MazeCell> solutionPathFinal = buildSolutionPathFinal(actualRegionPath, setup.mazeGrids, correctWalls, stepCounter);

        for (RegionEdge edge : correctWalls) edge.cellA().link(edge.cellB());

        setup.mazeGrid.getPath().getSolutionPath().addAll(solutionPathFinal);

        Map<MazeCell, Integer> cellPatterns = new LinkedHashMap<>();
        int patternLength = switch (mazeCreation.getDifficulty()) {
            case EASY -> random.nextInt(2, 4);
            case MEDIUM -> 4;
            case HARD -> random.nextInt(5, 8);
            default -> 3;
        };

        int step = (patternLength >= 5) ? 2 : 3;

        Map<MazeCell, Integer> distances = new HashMap<>();
        Queue<MazeCell> queue = new LinkedList<>();
        MazeCell startCell = setup.mazeGrid.getStartCell();
        distances.put(startCell, 0);
        queue.add(startCell);

        while (!queue.isEmpty()) {
            stepCounter.stepCounter().increment();
            MazeCell current = queue.poll();
            int dist = distances.get(current);

            for (MazeCell neighbor : current.getLinked()) {
                if (!distances.containsKey(neighbor)) {
                    distances.put(neighbor, dist + 1);
                    queue.add(neighbor);
                }
            }
        }

        Set<MazeCell> solutionPathSet = new HashSet<>(solutionPathFinal);
        Set<Integer> solutionRegions = new HashSet<>(actualRegionPath);
        Set<MazeCell> baitCells = new HashSet<>();

        for (RegionEdge deadEndEdge : edgeCategories.deadEndEdges()) {
            int distA = distances.getOrDefault(deadEndEdge.cellA(), Integer.MAX_VALUE);
            int distB = distances.getOrDefault(deadEndEdge.cellB(), Integer.MAX_VALUE);

            MazeCell from = (distA < distB) ? deadEndEdge.cellA() : deadEndEdge.cellB();
            MazeCell to = (from == deadEndEdge.cellA()) ? deadEndEdge.cellB() : deadEndEdge.cellA();

            baitCells.add(to);
            if (distances.containsKey(to)) {
                int correctIdx = ((distances.get(to) - 1) / step) % patternLength;
                cellPatterns.put(to, correctIdx);
            }
        }
        MazeCell endCell = setup.mazeGrid.getEndCell();
        for (MazeCell cell : setup.mazeGrid.getFlattenedGrid()) {
            stepCounter.stepCounter().increment();
            if (cell == null || cell == startCell || cell == endCell || !distances.containsKey(cell) || baitCells.contains(cell))
                continue;

            int dist = distances.get(cell);
            int rId = setup.mask[cell.getRow()][cell.getCol()];

            if ((dist - 1) % step == 0) {
                int correctIdx = ((dist - 1) / step) % patternLength;
                if (solutionRegions.contains(rId)) {
                    cellPatterns.put(cell, solutionPathSet.contains(cell) ? correctIdx : (random.nextDouble() < 0.7 ? correctIdx : random.nextInt(patternLength)));
                } else {
                    cellPatterns.put(cell, random.nextInt(patternLength));
                }
            } else if (!solutionPathSet.contains(cell) && random.nextDouble() < 0.03) {
                cellPatterns.put(cell, random.nextInt(patternLength));
            }
        }

        setup.mazeGrid.getModifiers().add(new PatternModifier(setup.mazeGrid.getMazeShape(), cellPatterns));
        insertNeighborsToCells(setup.mazeGrid, mazeCreation.getHeight(), mazeCreation.getWidth(), setup.finalGrid);

        return setup.mazeGrid;
    }

    private static MazeGrid generateWallsFloor(int floorNumber,
                                               MazeCreation mazeCreation, Random random,
                                               boolean isFirstFloor, boolean isLastFloor, MazeCell incomingStartCell, MazeBuildContext stepCounter) throws Exception {

        RegionSetup setup = buildBaseRegionSetup(floorNumber, mazeCreation, MazeType.WALLS, random, isFirstFloor, isLastFloor, incomingStartCell, stepCounter);

        BreakableWallsResult generatedPuzzle = generateSeparateRegionPuzzleWalls(
                setup.mazeGrid, setup.associations, setup.startIndex, setup.endIndex, setup.mazeGrids.size(), setup.solutionRegionPath, stepCounter, random);

        List<RegionEdge> walls = generatedPuzzle.walls();
        List<Integer> solutionPath = generatedPuzzle.solutionPath();

        List<MazeCell> solutionPathFinal = buildSolutionPathFinal(solutionPath, setup.mazeGrids, walls, stepCounter);

        for (RegionEdge edge : walls) edge.cellA().link(edge.cellB());

        setup.mazeGrid.getPath().getSolutionPath().addAll(solutionPathFinal);
        setup.mazeGrid.getModifiers().add(new WallModifier(walls, setup.mazeGrids.size() - 1, setup.mazeGrid.getMazeShape()));

        insertNeighborsToCells(setup.mazeGrid, mazeCreation.getHeight(), mazeCreation.getWidth(), setup.finalGrid);
        return setup.mazeGrid;
    }

    private static MazeGrid generatePortalFloor(int floorNumber,
                                                MazeCreation mazeCreation, Random random,
                                                MazeBuildContext globalContext, boolean isFirstFloor, boolean isLastFloor, MazeCell incomingStartCell) throws Exception {

        int rowCount = mazeCreation.getHeight();
        int colCount = mazeCreation.getWidth();
        int numRegions = Math.max(2, mazeCreation.getCounts().get(floorNumber));
        var fullMask = mazeCreation.getMask();
        RegionGenConfig config = DifficultyChoosing.getRegionConfig(rowCount, colCount, fullMask, numRegions, mazeCreation.getDifficulty());
        StepCounter floorCounter = new StepCounter();
        var variantKey = new GenerationStatistics.MazeVariantKey(MazeType.PORTAL, mazeCreation.getDifficulty(), colCount, rowCount);
        Long limit = GenerationStatistics.getMazeVariantLimit(variantKey);

        if (limit == null || limit == Long.MAX_VALUE) {
            var key = new GenerationStatistics.MazeKey(mazeCreation.getMazeAlgorithm(), mazeCreation.getMazeShape(), colCount, rowCount);
            limit = GenerationStatistics.getMazeLimit(key);
        }
        if (limit != null) {
            floorCounter.setLimit(limit * 3);
        }

        MazeBuildContext floorContext = new MazeBuildContext(floorCounter);

        Map<Integer, int[][]> mazeCells = new TreeMap<>();
        int[][] mask = RegionGenerator.generate(config, fullMask, random).grid();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                int index = mask[r][c];
                if (index >= 0) {
                    mazeCells.computeIfAbsent(index, k -> new int[rowCount][colCount])[r][c] = 1;
                }
            }
        }

        List<Long> subSeeds = new ArrayList<>();
        for (int i = 0; i < mazeCells.size(); i++) subSeeds.add(random.nextLong());

        Map<Integer, MazeGrid> mazeGrids = new TreeMap<>();
        int seedIndex = 0;

        for (Map.Entry<Integer, int[][]> entry : mazeCells.entrySet()) {
            Random subRandom = new Random(subSeeds.get(seedIndex++));

            MazeGridConstructor builder = new MazeGridConstructor()
                    .setRowCount(rowCount).setColCount(colCount)
                    .setRandomInstance(subRandom).setMazeShape(mazeCreation.getMazeShape())
                    .setMazeType(MazeType.PORTAL).setMask(entry.getValue());
            mazeGrids.put(entry.getKey(), returnNewInstance(builder));
        }

        Map<MazeGrid, List<MazeCell>> cornerGrids = new LinkedHashMap<>();
        for (Map.Entry<Integer, MazeGrid> entry : mazeGrids.entrySet()) {
            MazeGrid grid = entry.getValue();
            int[][] subMask = mazeCells.get(entry.getKey());
            MazeBuildContext subContext = new MazeBuildContext(floorCounter);
            MazeGeneratorFactory.getGenerator(mazeCreation.getMazeAlgorithm()).runOn(grid, random, subContext);

            List<MazeCell> cellsOnEdge = getGlobalEdges(grid, fullMask);
            if (!cellsOnEdge.isEmpty()) cornerGrids.put(grid, cellsOnEdge);
        }

        if (cornerGrids.isEmpty()) {
            for (Map.Entry<Integer, MazeGrid> entry : mazeGrids.entrySet()) {
                List<MazeCell> cellsOnEdge = getEdges(entry.getValue());
                if (!cellsOnEdge.isEmpty()) cornerGrids.put(entry.getValue(), cellsOnEdge);
            }
        }

        Map.Entry<MazeGrid, List<MazeCell>> cornerCellsStart = null;
        Map.Entry<MazeGrid, List<MazeCell>> cornerCellsEnd;

        if (!isFirstFloor && incomingStartCell != null) {
            int startIndex = mask[incomingStartCell.getRow()][incomingStartCell.getCol()];
            MazeGrid startGrid = mazeGrids.get(startIndex);
            for (var entry : cornerGrids.entrySet()) {
                if (entry.getKey().equals(startGrid)) {
                    cornerCellsStart = entry;
                    break;
                }
            }
        }

        List<Map.Entry<MazeGrid, List<MazeCell>>> sortedCorners = new ArrayList<>(cornerGrids.entrySet());
        sortedCorners.sort(Comparator.comparingInt((Map.Entry<MazeGrid, List<MazeCell>> e) -> e.getValue().getFirst().getRow())
                .thenComparingInt(e -> e.getValue().getFirst().getCol()));

        if (cornerCellsStart == null) {
            cornerCellsStart = sortedCorners.isEmpty() ? new ArrayList<>(cornerGrids.entrySet()).getFirst() : sortedCorners.get(random.nextInt(sortedCorners.size()));
        }

        sortedCorners.remove(cornerCellsStart);
        cornerCellsEnd = sortedCorners.isEmpty() ? cornerCellsStart : sortedCorners.get(random.nextInt(sortedCorners.size()));

        var cornerStartGrid = cornerCellsStart.getKey();
        var cornerEndGrid = cornerCellsEnd.getKey();

        if (isFirstFloor || incomingStartCell == null) {
            boolean hasEdgeCell = false;
            for (MazeCell[] row : cornerStartGrid.getGrid()) {
                for (MazeCell cell : row) {
                    if (cell != null && isCellOnMaskEdge(cell.getRow(), cell.getCol(), rowCount, colCount, fullMask)) {
                        hasEdgeCell = true;
                        break;
                    }
                }
                if (hasEdgeCell) break;
            }
            if (!hasEdgeCell) throw new Exception("[RETRY] Startovní region nemá žádné buňky na reálném okraji masky.");
        }

        if (isLastFloor) {
            boolean hasEdgeCell = false;
            for (MazeCell[] row : cornerEndGrid.getGrid()) {
                for (MazeCell cell : row) {
                    if (cell != null && isCellOnMaskEdge(cell.getRow(), cell.getCol(), rowCount, colCount, fullMask)) {
                        hasEdgeCell = true;
                        break;
                    }
                }
                if (hasEdgeCell) break;
            }
            if (!hasEdgeCell) throw new Exception("[RETRY] Koncový region nemá žádné buňky na reálném okraji masky.");
        }

        Map<MazeGrid, List<MazeCell>> solutionPaths = new LinkedHashMap<>();
        for (Map.Entry<Integer, MazeGrid> entry : mazeGrids.entrySet()) {
            MazeGrid grid = entry.getValue();
            List<MazeCell> edgeCells = new ArrayList<>();
            boolean isEnd = false;

            if (grid.equals(cornerEndGrid)) {
                isEnd = true;
                if (isLastFloor) {
                    for (MazeCell[] row : grid.getGrid()) {
                        for (MazeCell cell : row) {
                            if (cell != null && isCellOnMaskEdge(cell.getRow(), cell.getCol(), rowCount, colCount, fullMask)) {
                                edgeCells.add(cell);
                            }
                        }
                    }
                } else {
                    for (MazeCell[] row : grid.getGrid()) {
                        for (MazeCell cell : row) if (cell != null) edgeCells.add(cell);
                    }
                }
            } else if (grid.equals(cornerStartGrid)) {
                if (isFirstFloor || incomingStartCell == null) {
                    for (MazeCell[] row : grid.getGrid()) {
                        for (MazeCell cell : row) {
                            if (cell != null && isCellOnMaskEdge(cell.getRow(), cell.getCol(), rowCount, colCount, fullMask)) {
                                edgeCells.add(cell);
                            }
                        }
                    }
                } else {
                    for (MazeCell[] row : grid.getGrid()) {
                        for (MazeCell cell : row) if (cell != null) edgeCells.add(cell);
                    }
                }
            } else {
                edgeCells = getEdges(grid);
            }

            if (edgeCells.isEmpty()) {
                edgeCells = getEdges(grid);
            }

            if (grid.equals(cornerStartGrid) && !isFirstFloor && incomingStartCell != null) {
                grid.setStartCell(grid.getCell(incomingStartCell.getRow(), incomingStartCell.getCol()));
                grid.setStartDirection(RectangularDirection.NORTH);
            } else {
                CellAndDirection start = chooseStartFromList(grid, edgeCells, random);
                grid.setStartCell(start.cell());
                grid.setStartDirection(start.direction());
            }

            HashMap<MazeCell, Integer> distances = checkValidity(grid, floorContext);

            CellAndDirection resultEnd = pickEndPoint(grid, grid.getStartCell(), edgeCells, distances, random);
            if (resultEnd == null) {
                resultEnd = chooseStartFromList(grid, edgeCells, random);
            }

            if (isEnd && (isLastFloor || incomingStartCell == null)) {
                grid.setEndCell(grid.getStartCell());
                grid.setEndDirection(grid.getStartDirection());
                grid.setStartCell(resultEnd.cell());
                grid.setStartDirection(resultEnd.direction());
            } else {
                grid.setEndCell(resultEnd.cell());
                grid.setEndDirection(resultEnd.direction());
            }
            solutionPaths.put(grid, findPath(grid.getStartCell(), grid.getEndCell(), floorContext));
        }

        List<MazeGrid> middleGrids = new ArrayList<>();
        for (MazeGrid grid : mazeGrids.values()) {
            if (!grid.equals(cornerStartGrid) && !grid.equals(cornerEndGrid)) middleGrids.add(grid);
        }
        Collections.shuffle(middleGrids, random);

        List<MazeGrid> mainChainMiddle = new ArrayList<>();
        List<MazeGrid> decoyGrids = new ArrayList<>();

        double decoyChance = switch (mazeCreation.getDifficulty()) {
            case EASY -> 0.10;
            case MEDIUM -> 0.3;
            case HARD -> 0.5;
        };

        for (MazeGrid grid : middleGrids) {
            if (random.nextDouble() < decoyChance) {
                decoyGrids.add(grid);
            } else {
                mainChainMiddle.add(grid);
            }
        }

        List<MazeGrid> chain = new ArrayList<>();
        chain.add(cornerStartGrid);
        chain.addAll(mainChainMiddle);
        chain.add(cornerEndGrid);

        List<MazeCell> solutionPath = chain.stream().map(solutionPaths::get).flatMap(List::stream).toList();
        List<Pair<MazeCell, MazeCell>> portals = new ArrayList<>();

        Map<MazeCell, MazeDirection> directionsOfPortals = new LinkedHashMap<>();
        List<MazeCell> cellsWithPortals = new ArrayList<>();

        for (int i = 0; i < chain.size() - 1; i++) {
            MazeCell exitCell = chain.get(i).getEndCell();
            MazeCell entranceCell = chain.get(i + 1).getStartCell();
            exitCell.link(entranceCell);
            portals.add(new Pair<>(entranceCell, exitCell));
            cellsWithPortals.add(entranceCell);
            cellsWithPortals.add(exitCell);
            directionsOfPortals.put(exitCell, chain.get(i).getEndDirection());
            directionsOfPortals.put(entranceCell, chain.get(i + 1).getStartDirection());
        }

        for (MazeGrid decoyGrid : decoyGrids) {
            MazeGrid sourceGrid = chain.get(random.nextInt(Math.max(1, chain.size() - 1)));

            List<MazeCell> availableCells = new ArrayList<>();
            List<MazeCell> deadEnds = new ArrayList<>();

            for (MazeCell[] row : sourceGrid.getGrid()) {
                for (MazeCell c : row) {
                    if (c != null && !cellsWithPortals.contains(c) && c != sourceGrid.getStartCell() && c != sourceGrid.getEndCell()) {
                        availableCells.add(c);
                        if (c.getLinked().size() == 1) {
                            deadEnds.add(c);
                        }
                    }
                }
            }

            if (availableCells.isEmpty()) continue;

            MazeCell fakeExitCell = deadEnds.isEmpty() ? availableCells.get(random.nextInt(availableCells.size())) : deadEnds.get(random.nextInt(deadEnds.size()));
            MazeCell decoyEntrance = decoyGrid.getStartCell();

            fakeExitCell.link(decoyEntrance);
            portals.add(new Pair<>(decoyEntrance, fakeExitCell));
            cellsWithPortals.add(decoyEntrance);
            cellsWithPortals.add(fakeExitCell);

            directionsOfPortals.put(fakeExitCell, decoyGrid.getEndDirection());
            directionsOfPortals.put(decoyEntrance, decoyGrid.getStartDirection());
        }

        for (Pair<MazeCell, MazeCell> portal : portals) portal.getFirst().link(portal.getSecond());

        MazeCell[][] finalGrid = new MazeCell[rowCount][colCount];
        List<MazeGrid> allUsedGrids = new ArrayList<>(chain);
        allUsedGrids.addAll(decoyGrids);

        for (MazeGrid subGrid : allUsedGrids) {
            MazeCell[][] subInternalGrid = subGrid.getGrid();
            for (int r = 0; r < subInternalGrid.length; r++) {
                for (int c = 0; c < subInternalGrid[r].length; c++) {
                    if (subInternalGrid[r][c] != null) finalGrid[r][c] = subInternalGrid[r][c];
                }
            }
        }

        MazeGrid mazeGrid = returnNewInstance(new MazeGridConstructor()
                .setRowCount(rowCount).setColCount(colCount)
                .setRandomInstance(random).setMazeShape(mazeCreation.getMazeShape())
                .setMazeType(MazeType.PORTAL).setMask(fullMask));
        mazeGrid.setGrid(finalGrid);
        mazeGrid.getModifiers().add(new PortalModifier(cellsWithPortals, new HashMap<>(directionsOfPortals), portals, mazeCreation.getMazeShape()));

        mazeGrid.getPath().getSolutionPath().addAll(solutionPath);
        mazeGrid.setStartCell(cornerStartGrid.getStartCell());
        mazeGrid.setStartDirection(cornerStartGrid.getStartDirection());
        mazeGrid.setEndCell(cornerEndGrid.getEndCell());
        mazeGrid.setEndDirection(cornerEndGrid.getEndDirection());

        globalContext.stepCounter().add(floorCounter.get());

        return mazeGrid;
    }

    private static void finalizeValidCells(Maze maze, HashMap<Integer, Set<MazeCell>> validCellsMap, MazeType mainType) {
        if (mainType == MazeType.MULTI_LEVEL) {
            int totalFloors = maze.getMazeGridFloors().size();
            for (Map.Entry<Integer, Set<MazeCell>> entry : validCellsMap.entrySet()) {
                int index = entry.getKey();
                Set<MazeCell> cells = entry.getValue();

                if (index < 0 || index >= totalFloors) continue;

                MazeGrid grid = maze.getMazeGridFloors().get(index).getMazeGrid();
                StartAndEnd sAE = new StartAndEnd(grid.getStartCell(), grid.getStartDirection(), grid.getEndCell(), grid.getEndDirection());
                LevelType levelType = (index == 0) ? LevelType.START : (index == totalFloors - 1) ? LevelType.END : LevelType.MIDDLE;

                if (cells != null) for (MazeCell cell : cells) {
                    cell.setStartAndEnd(sAE);
                    cell.setLevelType(levelType);
                }
            }
        } else {
            StartAndEnd startAndEnd = new StartAndEnd(maze.getStartCell(), maze.getStartDirection(), maze.getEndCell(), maze.getEndDirection());
            for (Set<MazeCell> validCells : validCellsMap.values()) {
                if (validCells != null) for (MazeCell cell : validCells) {
                    cell.setStartAndEnd(startAndEnd);
                    cell.setLevelType(LevelType.NONE);
                }
            }
        }
    }

    public static CellAndDirection chooseStartFromList(MazeGrid grid, List<MazeCell> edgeCells, Random random) throws ThreadTerminationException {
        MazeCell startCell;
        do {
            startCell = edgeCells.get(random.nextInt(edgeCells.size()));
            if (Thread.currentThread().isInterrupted()) throw new ThreadTerminationException();
        } while (startCell == null);

        return new CellAndDirection(startCell, getDirectionToOutside(startCell, grid, random));
    }

    public static CellAndDirection pickEndPoint(MazeGrid mazeGrid, MazeCell startPoint, List<MazeCell> edges, Map<MazeCell, Integer> distances, Random random) throws ThreadTerminationException {
        if (edges == null || edges.isEmpty() || distances == null || distances.isEmpty()) return null;

        List<MazeCell> candidates = edges.stream()
                .filter(Objects::nonNull)
                .filter(c -> c != startPoint && c != mazeGrid.getStartCell())
                .filter(distances::containsKey)
                .sorted(Comparator.<MazeCell>comparingInt(distances::get).reversed()
                        .thenComparingInt(MazeCell::getRow)
                        .thenComparingInt(MazeCell::getCol))
                .toList();

        if (candidates.isEmpty()) return null;

        List<MazeCell> bestCandidates = candidates.subList(0, Math.max(1, (int) (candidates.size() * 0.1)));
        MazeCell endCell = bestCandidates.get(random.nextInt(bestCandidates.size()));

        if (Thread.currentThread().isInterrupted()) throw new ThreadTerminationException();

        return new CellAndDirection(endCell, getDirectionToOutside(endCell, mazeGrid, random));
    }
}
