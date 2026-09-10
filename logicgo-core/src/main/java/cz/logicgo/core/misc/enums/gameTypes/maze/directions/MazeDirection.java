package cz.logicgo.core.misc.enums.gameTypes.maze.directions;


import cz.logicgo.core.misc.interfaces.PersistableEnum;

import java.util.List;

public sealed interface MazeDirection extends PersistableEnum permits HexagonalDirection, RectangularDirection {
    static byte maskOf(int id) {
        return (byte) (1 << id);
    }

    int getId();

    byte getMask();

    List<? extends MazeDirection> instances();

}
