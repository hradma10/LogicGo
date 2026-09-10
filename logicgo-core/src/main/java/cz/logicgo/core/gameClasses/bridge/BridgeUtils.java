package cz.logicgo.core.gameClasses.bridge;


import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.misc.GridCell;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BridgeUtils {

    public static boolean[][] createIslandExistenceField(List<Island> islands, int width, int height) {
        boolean[][] islandExistenceField = new boolean[height][width];
        islands.forEach(island -> islandExistenceField[island.getRow()][island.getCol()] = true);
        return islandExistenceField;
    }

    public static void insertRealInstances(HashMap<Integer, Island> islandBridgesMap, List<IslandBridge> islandBridges) {
        islandBridges.forEach(islandBridge -> {
            Island realStartIsland = islandBridgesMap.get(islandBridge.getStartIsland().getId());
            Island realEndIsland = islandBridgesMap.get(islandBridge.getEndIsland().getId());
            islandBridge.setStartIsland(realStartIsland);
            islandBridge.setEndIsland(realEndIsland);
        });
    }

    public static HashMap<Integer, Island> getIntegerIslandHashMapFromIslands(List<Island> islands) {
        return islands.stream().collect(Collectors.toMap(Island::getId, island -> island, (a, b) -> b, HashMap::new));
    }

    public static boolean isGameFinished(Bridge bridge) {
        List<IslandBridge> bridges = bridge.getIslandBridges();
        List<IslandBridge> solutionBridges = bridge.getSolutionBridges();

        if (bridges.size() != solutionBridges.size()) return false;

        var bridgesSet = createNormalizedBridgesSet(bridges);
        var solutionSet = createNormalizedBridgesSet(solutionBridges);

        return bridgesSet.equals(solutionSet);
    }

    private static HashSet<NormalizedBridgeKey> createNormalizedBridgesSet(List<IslandBridge> bridges) {
        HashSet<NormalizedBridgeKey> resultSet = new HashSet<>();

        for (IslandBridge islandBridge : bridges) {
            GridCell c1 = new GridCell(islandBridge.getStartIsland());
            GridCell c2 = new GridCell(islandBridge.getEndIsland());

            GridCell first = isBefore(c1, c2) ? c1 : c2;
            GridCell second = first == c1 ? c2 : c1;

            resultSet.add(new NormalizedBridgeKey(first, second, islandBridge.getBridgeCount()));
        }
        return resultSet;
    }


    private static boolean isBefore(GridCell a, GridCell b) {
        if (a.row() != b.row()) {
            return a.row() < b.row();
        }
        return a.col() < b.col();
    }


    private record NormalizedBridgeKey(GridCell first, GridCell second, int count) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NormalizedBridgeKey(GridCell first1, GridCell second1, int count1))) return false;
            return count == count1 &&
                    Objects.equals(first, first1) &&
                    Objects.equals(second, second1);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second, count);
        }
    }
}
