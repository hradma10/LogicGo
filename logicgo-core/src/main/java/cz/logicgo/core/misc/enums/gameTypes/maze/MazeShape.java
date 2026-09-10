package cz.logicgo.core.misc.enums.gameTypes.maze;


import cz.logicgo.core.misc.TranslationLoader;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.HexagonalDirection;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.MazeDirection;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.RectangularDirection;
import cz.logicgo.core.misc.interfaces.PersistableEnum;
import cz.logicgo.core.misc.interfaces.Translatable;

import java.util.List;


public enum MazeShape implements PersistableEnum, Translatable {
    RECTANGULAR(1, "maze.shape.rectangular", RectangularDirection.class, MazeAlgorithm.values()),
    HEXAGONAL(2, "maze.shape.hexagonal", HexagonalDirection.class, getSupportedByNonRectangular());

    final int id;
    final String name;
    final List<MazeAlgorithm> supportedAlgorithms;
    final Class<? extends MazeDirection> associatedDirectionEnum;

    MazeShape(int id, String name, Class<? extends MazeDirection> direction, MazeAlgorithm... supportedAlgorithms) {
        this.id = id;
        this.name = name;
        this.supportedAlgorithms = List.of(supportedAlgorithms);
        this.associatedDirectionEnum = direction;
    }

    public static MazeAlgorithm[] getSupportedByNonRectangular() {
        return List.of(MazeAlgorithm.ALDOUS_BRODER, MazeAlgorithm.HUNT_AND_KILL,
                MazeAlgorithm.RECURSIVE_BACKTRACKER, MazeAlgorithm.PRIM, MazeAlgorithm.WILSON).toArray(new MazeAlgorithm[0]);
    }

    public static MazeShape fromId(int id) {
        for (var shape : values())
            if (shape.getId() == id) {
                return shape;
            }
        return null;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTranslation() {
        return TranslationLoader.getTranslation(name);
    }

    public List<MazeAlgorithm> getSupportedAlgorithms() {
        return supportedAlgorithms;
    }

    public Class<? extends MazeDirection> getAssociatedDirectionEnum() {
        return associatedDirectionEnum;
    }
}
