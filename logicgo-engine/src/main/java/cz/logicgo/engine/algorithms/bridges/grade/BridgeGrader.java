package cz.logicgo.engine.algorithms.bridges.grade;


import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.gameClasses.bridge.Island;
import cz.logicgo.core.misc.enums.Difficulty;

import java.util.*;

public class BridgeGrader {

    private static final int MAX_GRADER_STEPS = 100;

    public static int gradeHashi(Bridge bridge) {
        double totalScore = 0;
        boolean changed = true;

        int maxMultiple = bridge.getMaxMultipleBridges();
        GraderState state = new GraderState(bridge.getIslands(), maxMultiple);

        Map<String, Long> usageStats = new LinkedHashMap<>();

        int stepCount = 0;
        boolean loopLimitReached = false;

        while (changed && !state.isSolved()) {
            stepCount++;
            if (stepCount > MAX_GRADER_STEPS) {

                loopLimitReached = true;
                break;
            }

            changed = false;

            int starting = applyStartingTechniques(state);
            if (starting > 0) {
                totalScore += (1.0 * starting);
                usageStats.put("Starting Techniques", usageStats.getOrDefault("Starting Techniques", 0L) + starting);
                changed = true;
                continue;
            }

            int basic = applyBasicTechniques(state);
            if (basic > 0) {
                totalScore += (2.5 * basic);
                usageStats.put("Basic Techniques", usageStats.getOrDefault("Basic Techniques", 0L) + basic);
                changed = true;
                continue;
            }

            int isolation = applyIsolationTechniques(state);
            if (isolation > 0) {
                totalScore += (15.0 * isolation);
                usageStats.put("Isolation Techniques", usageStats.getOrDefault("Isolation Techniques", 0L) + isolation);
                changed = true;
                continue;
            }

            int advanced = applyAdvancedTechniques(state);
            if (advanced > 0) {
                totalScore += (40.0 * advanced);
                usageStats.put("Advanced Techniques", usageStats.getOrDefault("Advanced Techniques", 0L) + advanced);
                changed = true;
                continue;
            }
        }


        if (loopLimitReached) {
            return 99;
        }

        int remainingUnconnected = state.getMissingBridgesCount();
        int totalBridgesNeeded = bridge.getIslands().stream().mapToInt(Island::getBridgeCount).sum();

        if (remainingUnconnected <= (totalBridgesNeeded * 0.10) || remainingUnconnected < 12) {
            int calculatedDifficulty;

            long advancedCount = usageStats.getOrDefault("Advanced Techniques", 0L);
            long isolationCount = usageStats.getOrDefault("Isolation Techniques", 0L);
            long basicCount = usageStats.getOrDefault("Basic Techniques", 0L);
            long startingCount = usageStats.getOrDefault("Starting Techniques", 0L);

            if (advancedCount > 0) {
                calculatedDifficulty = (advancedCount > 2) ? 9 : 7;
            } else if (isolationCount > 0) {
                if (isolationCount > 6) {
                    calculatedDifficulty = 6;
                } else if (isolationCount > 2) {
                    calculatedDifficulty = 5;
                } else {
                    calculatedDifficulty = 4;
                }
            } else if (basicCount > 0) {
                if (basicCount > 12) {
                    calculatedDifficulty = 4;
                } else if (basicCount > 5) {
                    calculatedDifficulty = 3;
                } else {
                    calculatedDifficulty = 2;
                }
            } else {
                calculatedDifficulty = 1;
            }

            double base = 3.2;
            double scale = 1.8;

            int scoreBasedDiff = 1;
            if (totalScore > 1) {
                scoreBasedDiff = (int) Math.round((Math.log(totalScore) / Math.log(base)) * scale);
            }

            if (scoreBasedDiff > calculatedDifficulty && calculatedDifficulty < 6) {
                calculatedDifficulty++;
            }

            calculatedDifficulty = Math.clamp(calculatedDifficulty, 1, 9);
            return calculatedDifficulty;

        } else {
            return 99;
        }
    }

    public static Difficulty getDifficultyByGrade(int calculatedDiff) {
        if (calculatedDiff == 99) return null;

        if (calculatedDiff <= 4) {
            return Difficulty.EASY;
        } else if (calculatedDiff <= 7) {
            return Difficulty.MEDIUM;
        } else {
            return Difficulty.HARD;
        }
    }

    private static int applyStartingTechniques(GraderState state) {
        int count = 0;
        for (Island island : state.islands) {
            if (state.isCompleted(island)) continue;

            List<GraderEdge> activeEdges = state.getActiveEdges(island);
            int possible = activeEdges.stream().mapToInt(e -> state.getCapacity(e, island)).sum();
            int remaining = state.getRemaining(island);

            if (remaining == possible && remaining > 0) {
                boolean applied = false;
                for (GraderEdge e : activeEdges) {
                    int toAdd = state.getCapacity(e, island);
                    if (toAdd > 0) {
                        state.addBridges(e, toAdd);
                        applied = true;
                    }
                }
                if (applied) count++;
            }
        }
        return count;
    }

    private static int applyBasicTechniques(GraderState state) {
        int count = 0;
        for (Island island : state.islands) {
            if (state.isCompleted(island)) continue;

            List<GraderEdge> activeEdges = state.getActiveEdges(island);
            int possible = activeEdges.stream().mapToInt(e -> state.getCapacity(e, island)).sum();
            int remaining = state.getRemaining(island);

            if (remaining > possible - activeEdges.size()) {
                boolean applied = false;
                for (GraderEdge e : activeEdges) {
                    if (e.currentCount == 0) {
                        state.addBridges(e, 1);
                        applied = true;
                    }
                }
                if (applied) count++;
            }
        }
        return count;
    }

    private static int applyIsolationTechniques(GraderState state) {
        int count = 0;
        List<GraderEdge> edgesToTest = new ArrayList<>(state.edges);

        for (GraderEdge edge : edgesToTest) {
            if (edge.blocked || edge.currentCount > 0) continue;

            state.addBridges(edge, 1);
            boolean isolated = checkPrematureIsolation(state);
            state.removeBridges(edge, 1);

            if (isolated) {
                edge.blocked = true;
                count++;
            }
        }
        return count;
    }

    private static boolean checkPrematureIsolation(GraderState state) {
        int numIslands = state.islands.size();
        if (numIslands <= 1) return false;

        int maxId = 0;
        for (Island i : state.islands) {
            if (i.getId() > maxId) maxId = i.getId();
        }
        boolean[] visited = new boolean[maxId + 1];

        Island startNode = state.islands.get(0);
        for (Island i : state.islands) {
            if (state.getRemaining(i) > 0) {
                startNode = i;
                break;
            }
        }

        int[] stack = new int[numIslands];
        int stackPtr = 0;
        stack[stackPtr++] = startNode.getId();
        visited[startNode.getId()] = true;
        int visitedCount = 1;

        while (stackPtr > 0) {
            int currentId = stack[--stackPtr];

            for (GraderEdge edge : state.edges) {
                if (edge.currentCount > 0) {
                    int neighborId = -1;
                    if (edge.i1.getId() == currentId) neighborId = edge.i2.getId();
                    else if (edge.i2.getId() == currentId) neighborId = edge.i1.getId();

                    if (neighborId != -1 && !visited[neighborId]) {
                        visited[neighborId] = true;
                        visitedCount++;
                        stack[stackPtr++] = neighborId;
                    }
                }
            }
        }

        if (visitedCount > 0 && visitedCount < numIslands) {
            for (Island i : state.islands) {
                if (visited[i.getId()]) {
                    if (state.getRemaining(i) > 0) {
                        return false;
                    }
                }
            }
            return true;
        }

        return false;
    }

    private static int applyAdvancedTechniques(GraderState state) {
        int count = 0;
        List<GraderEdge> edgesToTest = new ArrayList<>(state.edges);

        for (GraderEdge edge : edgesToTest) {
            if (edge.blocked || state.getCapacity(edge, edge.i1) == 0) continue;

            state.addBridges(edge, 1);
            boolean contradiction = checkImmediateCapacityContradiction(state);
            state.removeBridges(edge, 1);

            if (contradiction) {
                edge.blocked = true;
                count++;
            }
        }
        return count;
    }

    private static boolean checkImmediateCapacityContradiction(GraderState state) {
        for (Island island : state.islands) {
            int remaining = state.getRemaining(island);

            if (remaining < 0) return true;

            if (remaining > 0) {
                int totalPossible = 0;
                List<GraderEdge> activeEdges = state.getActiveEdges(island);

                for (GraderEdge activeEdge : activeEdges) {
                    totalPossible += state.getCapacity(activeEdge, island);
                }

                if (remaining > totalPossible) {
                    return true;
                }
            }
        }
        return false;
    }

    private static class GraderState {
        List<Island> islands;
        List<GraderEdge> edges = new ArrayList<>();
        Map<Integer, Integer> currentBridges = new HashMap<>();
        int maxMultiple;

        private Map<GraderEdge, Integer> snapCounts;
        private Map<GraderEdge, Boolean> snapBlocked;
        private Map<Integer, Integer> snapCurrent;

        public GraderState(List<Island> islands, int maxMultiple) {
            this.islands = islands;
            this.maxMultiple = maxMultiple;
            for (Island i : islands) currentBridges.put(i.getId(), 0);
            buildEdges();
        }

        public boolean isSolved() {
            return getMissingBridgesCount() == 0;
        }

        public int getMissingBridgesCount() {
            return islands.stream().mapToInt(this::getRemaining).sum();
        }

        public boolean isCompleted(Island island) {
            return getRemaining(island) == 0;
        }

        public int getRemaining(Island island) {
            return island.getBridgeCount() - currentBridges.get(island.getId());
        }

        public List<GraderEdge> getActiveEdges(Island island) {
            List<GraderEdge> active = new ArrayList<>();
            for (GraderEdge e : edges) {
                if (e.blocked) continue;
                if ((e.i1.getId() == island.getId() || e.i2.getId() == island.getId()) && !isCrossed(e)) {
                    active.add(e);
                }
            }
            return active;
        }

        private boolean isCrossed(GraderEdge edge) {
            for (GraderEdge cross : edge.crosses) {
                if (cross.currentCount > 0) return true;
            }
            return false;
        }

        public int getCapacity(GraderEdge e, Island targetIsland) {
            if (e.blocked || isCrossed(e)) return 0;
            int spaceOnEdge = maxMultiple - e.currentCount;
            Island other = e.getOther(targetIsland);
            int spaceOnOther = getRemaining(other);
            return Math.min(spaceOnEdge, spaceOnOther);
        }

        public void addBridges(GraderEdge edge, int count) {
            edge.currentCount += count;
            currentBridges.put(edge.i1.getId(), currentBridges.get(edge.i1.getId()) + count);
            currentBridges.put(edge.i2.getId(), currentBridges.get(edge.i2.getId()) + count);
        }

        public void removeBridges(GraderEdge edge, int count) {
            edge.currentCount -= count;
            currentBridges.put(edge.i1.getId(), currentBridges.get(edge.i1.getId()) - count);
            currentBridges.put(edge.i2.getId(), currentBridges.get(edge.i2.getId()) - count);
        }

        public void saveSnapshot() {
            snapCounts = new HashMap<>();
            snapBlocked = new HashMap<>();
            snapCurrent = new HashMap<>(currentBridges);
            for (GraderEdge e : edges) {
                snapCounts.put(e, e.currentCount);
                snapBlocked.put(e, e.blocked);
            }
        }

        public void restoreSnapshot() {
            this.currentBridges.clear();
            this.currentBridges.putAll(snapCurrent);

            for (GraderEdge e : edges) {
                e.currentCount = snapCounts.get(e);
                e.blocked = snapBlocked.get(e);
            }
        }

        private void buildEdges() {
            int maxRow = 0, maxCol = 0;
            for (Island i : islands) {
                if (i.getRow() > maxRow) maxRow = i.getRow();
                if (i.getCol() > maxCol) maxCol = i.getCol();
            }

            Island[][] gridMap = new Island[maxRow + 1][maxCol + 1];
            for (Island i : islands) gridMap[i.getRow()][i.getCol()] = i;

            for (Island start : islands) {
                int c = start.getCol() + 1;
                while (c <= maxCol) {
                    Island target = gridMap[start.getRow()][c];
                    if (target != null) {
                        edges.add(new GraderEdge(start, target));
                        break;
                    }
                    c++;
                }
                int r = start.getRow() + 1;
                while (r <= maxRow) {
                    Island target = gridMap[r][start.getCol()];
                    if (target != null) {
                        edges.add(new GraderEdge(start, target));
                        break;
                    }
                    r++;
                }
            }

            for (int i = 0; i < edges.size(); i++) {
                for (int j = i + 1; j < edges.size(); j++) {
                    GraderEdge e1 = edges.get(i);
                    GraderEdge e2 = edges.get(j);
                    if (doIntersect(e1.i1, e1.i2, e2.i1, e2.i2)) {
                        e1.crosses.add(e2);
                        e2.crosses.add(e1);
                    }
                }
            }
        }

        private boolean doIntersect(Island p1, Island q1, Island p2, Island q2) {
            boolean e1Horizontal = p1.getRow().equals(q1.getRow());
            boolean e2Horizontal = p2.getRow().equals(q2.getRow());
            if (e1Horizontal == e2Horizontal) return false;

            if (e1Horizontal) {
                return p1.getRow() > Math.min(p2.getRow(), q2.getRow()) &&
                        p1.getRow() < Math.max(p2.getRow(), q2.getRow()) &&
                        p2.getCol() > Math.min(p1.getCol(), q1.getCol()) &&
                        p2.getCol() < Math.max(p1.getCol(), q1.getCol());
            } else {
                return p2.getRow() > Math.min(p1.getRow(), q1.getRow()) &&
                        p2.getRow() < Math.max(p1.getRow(), q1.getRow()) &&
                        p1.getCol() > Math.min(p2.getCol(), q2.getCol()) &&
                        p1.getCol() < Math.max(p2.getCol(), q2.getCol());
            }
        }
    }

    private static class GraderEdge {
        Island i1, i2;
        int currentCount = 0;
        boolean blocked = false;
        List<GraderEdge> crosses = new ArrayList<>();

        public GraderEdge(Island i1, Island i2) {
            this.i1 = i1;
            this.i2 = i2;
        }

        public Island getOther(Island base) {
            if (i1.getId() == base.getId()) return i2;
            if (i2.getId() == base.getId()) return i1;
            return null;
        }
    }
}
