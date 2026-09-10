package cz.logicgo.engine.algorithms.bridges;

import cz.logicgo.core.builders.bridge.BridgeCreation;
import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.entity.games.bridges.Bridge.ConnectedIslands;
import cz.logicgo.core.exceptions.parallel.LimitReachedException;
import cz.logicgo.core.exceptions.parallel.MultipleSolutionException;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.gameClasses.bridge.BoardSize;
import cz.logicgo.core.gameClasses.bridge.Island;
import cz.logicgo.core.gameClasses.bridge.IslandBridge;
import cz.logicgo.core.misc.DisjointSet;
import cz.logicgo.core.misc.GridCell;
import cz.logicgo.core.misc.StepCounter;
import cz.logicgo.core.misc.UniqueNumberGenerator;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.engine.algorithms.bridges.grade.BridgeGrader;
import cz.logicgo.engine.algorithms.bridges.records.BridgeGenValues;
import cz.logicgo.engine.algorithms.bridges.records.FinalBridgeProperties;
import cz.logicgo.engine.algorithms.settings.DifficultyChoosing.BridgeDifficulty;
import cz.logicgo.engine.algorithms.genStatistics.GenerationStatistics;
import cz.logicgo.engine.context.BridgeCreateContext;
import cz.logicgo.engine.context.BridgeSolveContext;

import java.util.*;

import static cz.logicgo.engine.algorithms.settings.DifficultyChoosing.getBridgeDifficulty;


public class BridgesGenerator {

    public static final int MAX_TRIES = 100;

    public static Bridge generateFullBridges(BridgeCreation bridgeCreation) throws ThreadTerminationException {
        StepCounter stepCounter = new StepCounter();
        return generateFullBridges(bridgeCreation, stepCounter);
    }

    public static Bridge generateFullBridges(BridgeCreation bridgeCreation, StepCounter stepCounter) throws ThreadTerminationException {
        try {
            int width = bridgeCreation.getWidth();
            int height = bridgeCreation.getHeight();
            int numberOfIslands = bridgeCreation.getNumberOfIslands();
            Integer maxMultiple = bridgeCreation.getMaxBridgeCount();

            int candidateCount = bridgeCreation.getDifficulty() == Difficulty.HARD ? 5 : 1;

            Bridge bestBridge = null;
            long maxSolverSteps = -1;

            for (int i = 0; i < candidateCount; i++) {
                Bridge candidateBridge = new Bridge(bridgeCreation);
                StepCounter candidateStepCounter = new StepCounter();

                boolean finished = generate(width, height, numberOfIslands, maxMultiple, candidateBridge, candidateStepCounter);

                if (finished) {
                    long currentSteps = candidateStepCounter.get();
                    if (currentSteps > maxSolverSteps) {
                        maxSolverSteps = currentSteps;
                        bestBridge = candidateBridge;
                    }
                }
            }

            if (bestBridge != null) {
                stepCounter.add(maxSolverSteps);
            }

            return bestBridge;

        } catch (LimitReachedException e) {
            throw new ThreadTerminationException();
        }
    }

    private static boolean generate(int width, int height, int numberOfIslands, Integer maxMultiple, Bridge bridge, StepCounter stepCounter) throws ThreadTerminationException, LimitReachedException {
        var randomInstance = bridge.getRandomInstance();
        BridgeDifficulty bridgeDifficulty = getBridgeDifficulty(bridge.getDifficulty(), randomInstance, new BoardSize(width, height), bridge.getType(), maxMultiple);

        if (numberOfIslands < 2) {
            numberOfIslands = bridgeDifficulty.islandCount();
        }

        int multipleCount = bridgeDifficulty.multipleCount();
        bridge.setMaxMultipleBridges(multipleCount);

        int maxLayoutAttempts = 10;
        int maxFinalizeAttempts = 20;

        if (multipleCount > 2) {
            maxLayoutAttempts = (width >= 12 || height >= 12) ? 60 : 30;
            maxFinalizeAttempts = 100;
        } else if (bridge.getDifficulty() == Difficulty.HARD) {
            maxLayoutAttempts = 25;
            maxFinalizeAttempts = 50;
        }

        int layoutAttempts = 0;

        var key = new GenerationStatistics.BridgeKey(bridge.getMaxMultipleBridges(), bridgeDifficulty.islandCount(), bridge.getDifficulty(), width, height);

        Long limitGen = GenerationStatistics.getBridgeLimit(key);
        Long limitSolve = GenerationStatistics.getBridgeSolveLimit(key);

        while (layoutAttempts < maxLayoutAttempts) {
            layoutAttempts++;
            BridgeGenValues bridgeGenValues;

            try {
                if (Thread.currentThread().isInterrupted()) {
                    throw new ThreadTerminationException();
                }

                stepCounter.reset();
                stepCounter.setLimit(limitGen);
                BridgeCreateContext bridgeCreateContext = new BridgeCreateContext(stepCounter);

                bridgeGenValues = createSolvedBridges(width, height, numberOfIslands, bridge, randomInstance, bridgeCreateContext);

                if (bridgeGenValues.islands().size() != numberOfIslands) {
                    continue;
                }
            } catch (LimitReachedException e) {

                continue;
            }

            reIdIslands(bridgeGenValues.islands());

            int attempts = 0;
            while (attempts < maxFinalizeAttempts) {
                attempts++;
                if (Thread.currentThread().isInterrupted()) {
                    throw new ThreadTerminationException();
                }
                FinalBridgeProperties finalBridgesProps = finalizeBridgeGen(
                        bridgeGenValues.islands(),
                        new ArrayList<>(bridgeGenValues.bridges()),
                        multipleCount,
                        bridgeDifficulty.doubleProbability(),
                        randomInstance
                );

                applyBridgeCounts(bridgeGenValues.islands(), finalBridgesProps.islandBridges());

                try {
                    StepCounter counter = new StepCounter();
                    counter.setLimit(limitSolve);
                    BridgeSolveContext context = new BridgeSolveContext(counter, 2);

                    BridgesSolver solver = new BridgesSolver(bridgeGenValues.islands(), multipleCount, context);
                    solver.solve();

                    bridge.setIslands(bridgeGenValues.islands());
                    bridge.setSolutionBridges(finalBridgesProps.islandBridges());
                    int calculatedDiff = BridgeGrader.gradeHashi(bridge);
                    bridge.setGrade(calculatedDiff);
                    Difficulty gradedDiff = BridgeGrader.getDifficultyByGrade(calculatedDiff);
                    if (gradedDiff != null) {
                        bridge.setDifficulty(gradedDiff);
                    }
                    if (calculatedDiff == 99) {
                        continue;
                    }

                } catch (MultipleSolutionException | LimitReachedException e) {
                    continue;
                }
                centerPuzzle(bridgeGenValues.islands(), width, height);

                bridge.setMultipleBridges(finalBridgesProps.multiBridgeMap());
                bridge.setIslands(bridgeGenValues.islands());
                bridge.setSolutionBridges(finalBridgesProps.islandBridges());
                bridge.setIslandBridges(new ArrayList<>());

                return true;
            }
        }

        return false;
    }

    public static void centerPuzzle(List<Island> islands, int boardWidth, int boardHeight) {
        if (islands.isEmpty()) return;

        int minR = islands.getFirst().getRow(), maxR = minR;
        int minC = islands.getFirst().getCol(), maxC = minC;

        for (Island i : islands) {
            if (i.getRow() < minR) minR = i.getRow();
            if (i.getRow() > maxR) maxR = i.getRow();
            if (i.getCol() < minC) minC = i.getCol();
            if (i.getCol() > maxC) maxC = i.getCol();
        }

        int currentWidth = maxC - minC + 1;
        int currentHeight = maxR - minR + 1;

        int shiftC = (boardWidth - currentWidth) / 2 - minC;
        int shiftR = (boardHeight - currentHeight) / 2 - minR;

        if (shiftC != 0 || shiftR != 0) {
            for (Island i : islands) {
                i.setRow(i.getRow() + shiftR);
                i.setCol(i.getCol() + shiftC);
            }
        }
    }

    private static void addCrossEdges(boolean[][] islands, boolean[][] bridges, List<Island> islandCells, List<IslandBridge> currentBridges, Random random, int probabilityPercent) {
        int height = islands.length;
        int width = islands[0].length;

        int[][] dirs = new int[][]{{0, 1}, {1, 0}};

        for (Island startIsland : islandCells) {
            for (int[] d : dirs) {
                int r = startIsland.getRow() + d[0];
                int c = startIsland.getCol() + d[1];

                while (r >= 0 && r < height && c >= 0 && c < width) {
                    if (bridges[r][c]) break;

                    if (islands[r][c]) {
                        int finalR = r;
                        int finalC = c;
                        Island targetIsland = islandCells.stream()
                                .filter(i -> i.getRow() == finalR && i.getCol() == finalC)
                                .findFirst().orElse(null);

                        if (targetIsland != null) {
                            boolean alreadyConnected = currentBridges.stream().anyMatch(b ->
                                    (b.getStartIsland() == startIsland && b.getEndIsland() == targetIsland) ||
                                            (b.getStartIsland() == targetIsland && b.getEndIsland() == startIsland)
                            );

                            if (!alreadyConnected && random.nextInt(100) < probabilityPercent) {
                                boolean safeToPlace = !isCrossing(startIsland.getRow(), startIsland.getCol(), targetIsland.getRow(), targetIsland.getCol(), currentBridges);

                                if (safeToPlace) {
                                    IslandBridge newBridge = new IslandBridge(startIsland, targetIsland);
                                    currentBridges.add(newBridge);
                                    markBridgeGrid(newBridge, true, bridges);
                                }
                            }
                        }
                        break;
                    }
                    r += d[0];
                    c += d[1];
                }
            }
        }
    }

    public static boolean isValidBridgePlacement(Island startIsland, Island endIsland, boolean[][] islandsPosition, List<IslandBridge> currentBridges) {
        if (startIsland == null || endIsland == null || startIsland.equals(endIsland)) return false;

        int r1 = startIsland.getRow();
        int c1 = startIsland.getCol();
        int r2 = endIsland.getRow();
        int c2 = endIsland.getCol();

        int dRow = Integer.compare(r2, r1);
        int dCol = Integer.compare(c2, c1);

        boolean isOrthogonal = (r1 == r2 || c1 == c2);
        if (!isOrthogonal) return false;

        int r = r1 + dRow;
        int c = c1 + dCol;

        while (r != r2 || c != c2) {
            if (islandsPosition[r][c]) {
                return false;
            }
            r += dRow;
            c += dCol;
        }

        return !isCrossing(r1, c1, r2, c2, currentBridges);
    }

    public static boolean isCrossing(int r1, int c1, int r2, int c2, List<IslandBridge> currentBridges) {
        int minR1 = Math.min(r1, r2);
        int maxR1 = Math.max(r1, r2);
        int minC1 = Math.min(c1, c2);
        int maxC1 = Math.max(c1, c2);

        boolean isHorizontal1 = (r1 == r2);

        for (IslandBridge b : currentBridges) {
            int br1 = b.getStartIsland().getRow();
            int bc1 = b.getStartIsland().getCol();
            int br2 = b.getEndIsland().getRow();
            int bc2 = b.getEndIsland().getCol();

            if ((r1 == br1 && c1 == bc1) || (r1 == br2 && c1 == bc2) ||
                    (r2 == br1 && c2 == bc1) || (r2 == br2 && c2 == bc2)) {
                continue;
            }

            int minR2 = Math.min(br1, br2);
            int maxR2 = Math.max(br1, br2);
            int minC2 = Math.min(bc1, bc2);
            int maxC2 = Math.max(bc1, bc2);

            boolean isHorizontal2 = (br1 == br2);

            if (isHorizontal1 && !isHorizontal2) {
                if (r1 > minR2 && r1 < maxR2 && bc1 > minC1 && bc1 < maxC1) return true;
            } else if (!isHorizontal1 && isHorizontal2) {
                if (br1 > minR1 && br1 < maxR1 && c1 > minC2 && c1 < maxC2) return true;
            } else if (isHorizontal1 && isHorizontal2 && r1 == br1) {
                if (Math.max(minC1, minC2) < Math.min(maxC1, maxC2)) return true;
            } else if (!isHorizontal1 && !isHorizontal2 && c1 == bc1) {
                if (Math.max(minR1, minR2) < Math.min(maxR1, maxR2)) return true;
            }
        }
        return false;
    }

    private static void applyBridgeCounts(List<Island> islands, List<IslandBridge> bridges) {
        islands.forEach(i -> i.setBridgeCount(0));
        Map<Integer, Integer> counts = new HashMap<>();

        for (IslandBridge b : bridges) {
            int u = b.getStartIsland().getId();
            int v = b.getEndIsland().getId();

            int bridgeWeight = Math.max(1, b.getBridgeCount());

            counts.put(u, counts.getOrDefault(u, 0) + bridgeWeight);
            counts.put(v, counts.getOrDefault(v, 0) + bridgeWeight);
        }

        islands.forEach(i -> i.setBridgeCount(counts.getOrDefault(i.getId(), 0)));
    }

    private static FinalBridgeProperties finalizeBridgeGen(ArrayList<Island> islands, ArrayList<IslandBridge> islandBridges, int maxBridges, double doubleProbability, Random randomInstance) {
        Collections.shuffle(islandBridges, randomInstance);
        DisjointSet dsu = new DisjointSet(islands.size());

        for (Island island : islands) {
            dsu.makeSet(island.getId());
        }

        List<IslandBridge> bridges = new ArrayList<>();
        for (IslandBridge bridge : islandBridges) {
            int startId = bridge.getStartIsland().getId();
            int endId = bridge.getEndIsland().getId();

            if (dsu.find(startId) != dsu.find(endId)) {
                dsu.union(startId, endId);
                bridges.add(bridge);
            }
        }

        HashMap<ConnectedIslands, Integer> multiBridgeMap =
                createMultipleBridges(bridges, maxBridges, randomInstance, doubleProbability * 0.5);

        for (IslandBridge baseBridge : bridges) {
            ConnectedIslands key = new ConnectedIslands(baseBridge.getStartIsland(), baseBridge.getEndIsland());
            int targetCount = multiBridgeMap.getOrDefault(key, 1);
            baseBridge.setBridgeCount(targetCount);
        }

        return new FinalBridgeProperties(multiBridgeMap, bridges);
    }

    public static void reIdIslands(List<Island> islands) {
        UniqueNumberGenerator generator = new UniqueNumberGenerator(-1);
        islands.stream().forEachOrdered(island -> island.setId(generator.generateNewId()));
    }

    public static BridgeGenValues createSolvedBridges(int width, int height, int numberOfIslands, Bridge bridge, Random random, BridgeCreateContext context) throws ThreadTerminationException, LimitReachedException {
        UniqueNumberGenerator uniqueNumberGenerator = new UniqueNumberGenerator();
        boolean[][] islands = new boolean[height][width];
        boolean[][] bridges = new boolean[height][width];
        ArrayList<Island> islandCells = new ArrayList<>();
        BoardSize boardSize = new BoardSize(width, height);
        Stack<IslandBridge> bridgeStack = new Stack<>();
        Set<GridCell> usedAsFirst = new HashSet<>();

        int row, col;
        StepCounter stepCounter = context.stepCounter();

        while (true) {
            context.checkContext();
            stepCounter.increment();

            do {
                stepCounter.increment();
                row = random.nextInt(height);
                col = random.nextInt(width);
                if (usedAsFirst.size() >= width * height) return new BridgeGenValues(new ArrayList<>(), new Stack<>());
            } while (usedAsFirst.contains(new GridCell(row, col)));

            usedAsFirst.add(new GridCell(row, col));
            int id = uniqueNumberGenerator.generateNewId();
            Island island = new Island(id, row, col, boardSize);
            islandCells.add(island);
            islands[row][col] = true;

            boolean success = placeIsland(numberOfIslands, random, uniqueNumberGenerator, islands, bridges,
                    islandCells, boardSize, bridgeStack, context, bridge.getDifficulty());

            if (success) {
                int prob = switch (bridge.getDifficulty()) {
                    case EASY -> 20;
                    case MEDIUM -> 35;
                    case HARD -> 30;
                    default -> 40;
                };
                addCrossEdges(islands, bridges, islandCells, bridgeStack, random, prob);
                return new BridgeGenValues(islandCells, bridgeStack);
            } else {
                islandCells.remove(island);
                islands[row][col] = false;
            }
        }
    }

    private static boolean placeIsland(int target, Random random, UniqueNumberGenerator idGen,
                                       boolean[][] islands, boolean[][] bridges,
                                       ArrayList<Island> islandCells, BoardSize bs,
                                       Stack<IslandBridge> stack,
                                       BridgeCreateContext context,
                                       Difficulty difficulty) throws ThreadTerminationException, LimitReachedException {

        if (islandCells.size() == target) return true;

        StepCounter stepCounter = context.stepCounter();
        int height = islands.length;
        int width = islands[0].length;
        int tries = 0;

        while (tries < MAX_TRIES) {
            context.checkContext();
            tries++;
            stepCounter.increment();

            Island ref = islandCells.get(random.nextInt(islandCells.size()));
            int dir = random.nextInt(4);

            int dr = 0, dc = 0;
            switch (dir) {
                case 0 -> dc = 1;
                case 1 -> dc = -1;
                case 2 -> dr = 1;
                case 3 -> dr = -1;
            }

            int nr = ref.getRow() + dr;
            int nc = ref.getCol() + dc;
            int dist = 0;

            List<GridCell> possibleSpots = new ArrayList<>();

            while (nr >= 0 && nr < height && nc >= 0 && nc < width) {
                stepCounter.increment();
                if (islands[nr][nc] || bridges[nr][nc]) break;

                boolean isTooClose = checkNearIsland(nr, nc, islands);

                if (dist >= 1 && !isTooClose) {
                    possibleSpots.add(new GridCell(nr, nc));
                }
                nr += dr;
                nc += dc;
                dist++;
            }

            if (possibleSpots.isEmpty()) continue;
            GridCell spot;
            if (difficulty == Difficulty.HARD) {
                int selectedIndex = random.nextInt(possibleSpots.size());
                spot = possibleSpots.get(selectedIndex);
            } else if (difficulty == Difficulty.MEDIUM) {
                int limit = Math.max(1, possibleSpots.size() / 2);
                spot = possibleSpots.get(random.nextInt(limit));
            } else {
                possibleSpots.sort(Comparator.comparingInt(s ->
                        Math.abs(s.row() - ref.getRow()) + Math.abs(s.col() - ref.getCol())
                ));
                int maxIndex = Math.min(1, possibleSpots.size() - 1);
                spot = possibleSpots.get(maxIndex > 0 ? random.nextInt(maxIndex + 1) : 0);
            }

            if (isCrossing(ref.getRow(), ref.getCol(), spot.row(), spot.col(), stack)) {
                continue;
            }

            int newId = idGen.generateNewId();
            Island newIsland = new Island(newId, spot.row(), spot.col(), bs);
            IslandBridge newBridge = new IslandBridge(ref, newIsland);

            islands[spot.row()][spot.col()] = true;
            markBridgeGrid(newBridge, true, bridges);
            islandCells.add(newIsland);
            stack.push(newBridge);

            context.checkContext();

            if (placeIsland(target, random, idGen, islands, bridges, islandCells, bs, stack, context, difficulty)) {
                return true;
            }
            stack.pop();
            islandCells.remove(newIsland);
            markBridgeGrid(newBridge, false, bridges);
            islands[spot.row()][spot.col()] = false;
            context.checkContext();
        }

        return false;
    }

    public static boolean checkNearIsland(int row, int col, boolean[][] islands) {
        int height = islands.length;
        int width = islands[0].length;
        for (int r = Math.max(0, row - 1); r <= Math.min(height - 1, row + 1); r++) {
            for (int c = Math.max(0, col - 1); c <= Math.min(width - 1, col + 1); c++) {
                if (r == row && c == col) continue;

                if (islands[r][c]) return true;
            }
        }
        return false;
    }

    private static HashMap<ConnectedIslands, Integer> createMultipleBridges(
            List<IslandBridge> islandBridges, int maxBridges, Random randomInstance, double probability) {

        HashMap<ConnectedIslands, Integer> multipleBridges = new HashMap<>();
        if (maxBridges <= 1) return multipleBridges;

        Map<Island, Integer> connectionsPerIsland = new HashMap<>();
        for (IslandBridge ib : islandBridges) {
            connectionsPerIsland.merge(ib.getStartIsland(), 1, Integer::sum);
            connectionsPerIsland.merge(ib.getEndIsland(), 1, Integer::sum);
        }

        double decayFactor = Math.max(0.4, 1.0 - (2.0 / maxBridges));

        int highestGenerated = 1;
        ConnectedIslands bestCandidateForMax = null;
        int bestCandidateScore = -1;

        for (IslandBridge ib : islandBridges) {
            int bridgesCount = 1;

            int con1 = connectionsPerIsland.getOrDefault(ib.getStartIsland(), 0);
            int con2 = connectionsPerIsland.getOrDefault(ib.getEndIsland(), 0);

            ConnectedIslands currentKey = new ConnectedIslands(ib.getStartIsland(), ib.getEndIsland());

            int score = con1 + con2;
            if (score > bestCandidateScore) {
                bestCandidateScore = score;
                bestCandidateForMax = currentKey;
            }

            double currentProbability = probability;

            if (con1 >= 3 || con2 >= 3) {
                currentProbability = Math.min(0.60, currentProbability + 0.1);
            }

            for (int i = 1; i < maxBridges; i++) {
                if (randomInstance.nextDouble() < currentProbability) {
                    bridgesCount++;
                    currentProbability *= decayFactor;
                } else {
                    break;
                }
            }

            if (bridgesCount > highestGenerated) {
                highestGenerated = bridgesCount;
            }

            if (bridgesCount > 1) {
                multipleBridges.put(currentKey, bridgesCount);
            }
        }

        if (highestGenerated < maxBridges && bestCandidateForMax != null && islandBridges.size() > 10) {
            multipleBridges.put(bestCandidateForMax, maxBridges);
        }

        return multipleBridges;
    }

    public static void markBridgeGrid(IslandBridge bridge, boolean state, boolean[][] grid) {
        int r1 = bridge.getStartIsland().getRow();
        int c1 = bridge.getStartIsland().getCol();
        int r2 = bridge.getEndIsland().getRow();
        int c2 = bridge.getEndIsland().getCol();

        if (r1 == r2) {
            for (int c = Math.min(c1, c2) + 1; c < Math.max(c1, c2); c++) {
                grid[r1][c] = state;
            }
        } else if (c1 == c2) {
            for (int r = Math.min(r1, r2) + 1; r < Math.max(r1, r2); r++) {
                grid[r][c1] = state;
            }
        }
    }
}
