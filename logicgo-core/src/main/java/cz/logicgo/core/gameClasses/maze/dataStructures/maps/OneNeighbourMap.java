package cz.logicgo.core.gameClasses.maze.dataStructures.maps;


import cz.logicgo.core.misc.enums.gameTypes.maze.directions.MazeDirection;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class OneNeighbourMap implements NeighbourMap {

    private final HashMap<MazeDirection, MazeCell> neighbours;

    public OneNeighbourMap(Class<? extends MazeDirection> directionType) {
        MazeDirection[] enumConst = directionType.getEnumConstants();
        this.neighbours = new HashMap<>(enumConst.length, 1);
        for (var constants : enumConst) {
            neighbours.put(constants, null);
        }
    }

    @Override
    public void put(MazeDirection direction, MazeCell cell) {
        neighbours.put(direction, cell);
    }

    @Override
    public void put(MazeDirection direction, List<MazeCell> cells) {
        throw new UnsupportedOperationException("Can only have one cell to direction.");
    }

    @Override
    public List<MazeCell> get(MazeDirection direction) {
        if (neighbours.containsKey(direction) && neighbours.get(direction) != null) {
            return List.of(neighbours.get(direction));
        } else return null;

    }

    @Override
    public Set<MazeDirection> keySet() {
        return neighbours.keySet();
    }

    @Override
    public Set<MazeCell> valueSet() {
        return neighbours.values().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean contains(MazeDirection direction) {
        return neighbours.containsKey(direction) && neighbours.get(direction) != null;
    }
}
