package cz.logicgo.core.gameClasses.maze.dataStructures.maps;


import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.MazeDirection;

import java.util.List;
import java.util.Set;

public interface NeighbourMap {

    void put(MazeDirection direction, MazeCell cell);

    void put(MazeDirection direction, List<MazeCell> cells);

    List<MazeCell> get(MazeDirection direction);

    Set<MazeDirection> keySet();

    Set<MazeCell> valueSet();

    boolean contains(MazeDirection direction);
}
