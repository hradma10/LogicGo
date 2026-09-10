package cz.logicgo.engine.algorithms.bridges;


import cz.logicgo.core.exceptions.parallel.LimitReachedException;
import cz.logicgo.core.exceptions.parallel.MultipleSolutionException;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.gameClasses.bridge.Island;
import cz.logicgo.core.misc.StepCounter;
import cz.logicgo.engine.context.BridgeSolveContext;

import java.util.*;

public class BridgesSolver {

    private final List<Island> islands;
    private final int maxBridgesPerEdge;
    private List<PotentialEdge> allEdges;
    private BridgeSolveContext context;

    private Map<Integer, Integer> currentBridgesPerIsland;
    private Map<Integer, Integer> remainingPotential;

    public BridgesSolver(List<Island> islands, int maxBridgesPerEdge, BridgeSolveContext context) {
        this.islands = islands;
        this.maxBridgesPerEdge = maxBridgesPerEdge;
        this.context = context;
    }

    public void solve() throws ThreadTerminationException, LimitReachedException, MultipleSolutionException {
        this.currentBridgesPerIsland = new HashMap<>();
        this.remainingPotential = new HashMap<>();

        for (Island island : islands) {
            currentBridgesPerIsland.put(island.getId(), 0);
            remainingPotential.put(island.getId(), 0);
        }

        buildPotentialEdges();
        findIntersections();

        for (PotentialEdge edge : allEdges) {
            remainingPotential.put(edge.i1.getId(), remainingPotential.get(edge.i1.getId()) + maxBridgesPerEdge);
            remainingPotential.put(edge.i2.getId(), remainingPotential.get(edge.i2.getId()) + maxBridgesPerEdge);
        }

        backtrack(0);
    }

    private void backtrack(int edgeIndex) throws ThreadTerminationException, LimitReachedException, MultipleSolutionException {
        context.checkContext();

        StepCounter stepCounter = context.stepCounter();
        stepCounter.increment();

        if (edgeIndex == allEdges.size()) {
            if (!isValidSolution()) return;
            context.incrementSolutionCountAndGet();
            return;
        }

        PotentialEdge edge = allEdges.get(edgeIndex);
        int i1 = edge.i1.getId();
        int i2 = edge.i2.getId();

        remainingPotential.put(i1, remainingPotential.get(i1) - maxBridgesPerEdge);
        remainingPotential.put(i2, remainingPotential.get(i2) - maxBridgesPerEdge);

        for (int bridgesToPlace = 0; bridgesToPlace <= maxBridgesPerEdge; bridgesToPlace++) {

            if (bridgesToPlace > 0) {
                boolean crossesActiveEdge = false;

                for (PotentialEdge crossedEdge : edge.crosses) {
                    if (crossedEdge.currentBridges > 0) {
                        crossesActiveEdge = true;
                        break;
                    }
                }
                if (crossesActiveEdge) break;

                if (currentBridgesPerIsland.get(i1) + bridgesToPlace > edge.i1.getBridgeCount() ||
                        currentBridgesPerIsland.get(i2) + bridgesToPlace > edge.i2.getBridgeCount()) {
                    break;
                }
            }

            if (currentBridgesPerIsland.get(i1) + bridgesToPlace + remainingPotential.get(i1) >= edge.i1.getBridgeCount() &&
                    currentBridgesPerIsland.get(i2) + bridgesToPlace + remainingPotential.get(i2) >= edge.i2.getBridgeCount()) {

                edge.currentBridges = bridgesToPlace;

                if (bridgesToPlace > 0) {
                    currentBridgesPerIsland.put(i1, currentBridgesPerIsland.get(i1) + bridgesToPlace);
                    currentBridgesPerIsland.put(i2, currentBridgesPerIsland.get(i2) + bridgesToPlace);
                }

                backtrack(edgeIndex + 1);

                if (bridgesToPlace > 0) {
                    currentBridgesPerIsland.put(i1, currentBridgesPerIsland.get(i1) - bridgesToPlace);
                    currentBridgesPerIsland.put(i2, currentBridgesPerIsland.get(i2) - bridgesToPlace);
                }
                edge.currentBridges = 0;
            }
        }

        remainingPotential.put(i1, remainingPotential.get(i1) + maxBridgesPerEdge);
        remainingPotential.put(i2, remainingPotential.get(i2) + maxBridgesPerEdge);
    }

    private boolean isValidSolution() {
        for (Island island : islands) {
            if (currentBridgesPerIsland.get(island.getId()).intValue() != island.getBridgeCount()) {
                return false;
            }
        }

        if (islands.isEmpty()) return true;

        Set<Integer> visited = new HashSet<>();
        Queue<Island> queue = new LinkedList<>();

        Island startIsland = islands.getFirst();
        queue.add(startIsland);
        visited.add(startIsland.getId());

        while (!queue.isEmpty()) {
            Island current = queue.poll();

            for (PotentialEdge edge : allEdges) {
                if (edge.currentBridges > 0) {
                    Island neighbor = null;
                    if (edge.i1.getId() == current.getId()) {
                        neighbor = edge.i2;
                    } else if (edge.i2.getId() == current.getId()) {
                        neighbor = edge.i1;
                    }

                    if (neighbor != null && !visited.contains(neighbor.getId())) {
                        visited.add(neighbor.getId());
                        queue.add(neighbor);
                    }
                }
            }
        }

        return visited.size() == islands.size();
    }

    private void buildPotentialEdges() {
        allEdges = new ArrayList<>();
        Set<Long> existingPairs = new HashSet<>();

        int maxRow = 0, maxCol = 0;
        for (Island island : islands) {
            if (island.getRow() > maxRow) maxRow = island.getRow();
            if (island.getCol() > maxCol) maxCol = island.getCol();
        }

        Island[][] gridMap = new Island[maxRow + 1][maxCol + 1];
        for (Island island : islands) {
            gridMap[island.getRow()][island.getCol()] = island;
        }

        for (Island start : islands) {

            int c = start.getCol() + 1;
            while (c <= maxCol) {
                Island target = gridMap[start.getRow()][c];
                if (target != null) {
                    long minId = Math.min(start.getId(), target.getId());
                    long maxId = Math.max(start.getId(), target.getId());
                    long pairKey = (minId << 32) | maxId;

                    if (!existingPairs.contains(pairKey)) {
                        existingPairs.add(pairKey);
                        allEdges.add(new PotentialEdge(start, target));
                    }
                    break;
                }
                c++;
            }

            int r = start.getRow() + 1;
            while (r <= maxRow) {
                Island target = gridMap[r][start.getCol()];
                if (target != null) {
                    long minId = Math.min(start.getId(), target.getId());
                    long maxId = Math.max(start.getId(), target.getId());
                    long pairKey = (minId << 32) | maxId;

                    if (!existingPairs.contains(pairKey)) {
                        existingPairs.add(pairKey);
                        allEdges.add(new PotentialEdge(start, target));
                    }
                    break;
                }
                r++;
            }
        }

        Map<Integer, Integer> incidentEdges = new HashMap<>();
        for (PotentialEdge e : allEdges) {
            incidentEdges.put(e.i1.getId(), incidentEdges.getOrDefault(e.i1.getId(), 0) + 1);
            incidentEdges.put(e.i2.getId(), incidentEdges.getOrDefault(e.i2.getId(), 0) + 1);
        }

        allEdges.sort((e1, e2) -> {
            int deg1 = incidentEdges.get(e1.i1.getId()) + incidentEdges.get(e1.i2.getId());
            int deg2 = incidentEdges.get(e2.i1.getId()) + incidentEdges.get(e2.i2.getId());
            if (deg1 != deg2) {
                return Integer.compare(deg1, deg2);
            }
            int req1 = e1.i1.getBridgeCount() + e1.i2.getBridgeCount();
            int req2 = e2.i1.getBridgeCount() + e2.i2.getBridgeCount();
            return Integer.compare(req2, req1);
        });
    }

    private void findIntersections() {
        for (int i = 0; i < allEdges.size(); i++) {
            for (int j = i + 1; j < allEdges.size(); j++) {
                PotentialEdge e1 = allEdges.get(i);
                PotentialEdge e2 = allEdges.get(j);

                if (e1.i1.getId() == e2.i1.getId() || e1.i1.getId() == e2.i2.getId() ||
                        e1.i2.getId() == e2.i1.getId() || e1.i2.getId() == e2.i2.getId()) {
                    continue;
                }

                if (doIntersect(e1.i1, e1.i2, e2.i1, e2.i2)) {
                    e1.crosses.add(e2);
                    e2.crosses.add(e1);
                }
            }
        }
    }

    private boolean doIntersect(Island p1, Island q1, Island p2, Island q2) {
        boolean e1Horizontal = p1.getRow() == q1.getRow();
        boolean e2Horizontal = p2.getRow() == q2.getRow();

        if (e1Horizontal == e2Horizontal) {
            return false;
        }

        if (e1Horizontal) {
            int minC1 = Math.min(p1.getCol(), q1.getCol());
            int maxC1 = Math.max(p1.getCol(), q1.getCol());
            int r1 = p1.getRow();

            int minR2 = Math.min(p2.getRow(), q2.getRow());
            int maxR2 = Math.max(p2.getRow(), q2.getRow());
            int c2 = p2.getCol();

            return r1 > minR2 && r1 < maxR2 && c2 > minC1 && c2 < maxC1;
        } else {
            int minR1 = Math.min(p1.getRow(), q1.getRow());
            int maxR1 = Math.max(p1.getRow(), q1.getRow());
            int c1 = p1.getCol();

            int minC2 = Math.min(p2.getCol(), q2.getCol());
            int maxC2 = Math.max(p2.getCol(), q2.getCol());
            int r2 = p2.getRow();

            return r2 > minR1 && r2 < maxR1 && c1 > minC2 && c1 < maxC2;
        }
    }

    public BridgeSolveContext getContext() {
        return context;
    }

    public BridgesSolver setContext(BridgeSolveContext context) {
        this.context = context;
        return this;
    }

    public static class PotentialEdge {
        Island i1, i2;
        int currentBridges = 0;
        List<PotentialEdge> crosses = new ArrayList<>();

        public PotentialEdge(Island i1, Island i2) {
            this.i1 = i1;
            this.i2 = i2;
        }
    }
}
