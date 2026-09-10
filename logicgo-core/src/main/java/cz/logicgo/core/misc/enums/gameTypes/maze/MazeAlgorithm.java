package cz.logicgo.core.misc.enums.gameTypes.maze;


import cz.logicgo.core.misc.TranslationLoader;
import cz.logicgo.core.misc.interfaces.PersistableEnum;
import cz.logicgo.core.misc.interfaces.Translatable;


public enum MazeAlgorithm implements PersistableEnum, Translatable {

    KRUSKAL(1, "maze.algorithm.kruskal"),
    ALDOUS_BRODER(2, "maze.algorithm.aldous_broder"),
    BINARY_TREE(3, "maze.algorithm.binary_tree"),
    ELLER(4, "maze.algorithm.eller"),
    HUNT_AND_KILL(5, "maze.algorithm.hunt_and_kill"),
    PRIM(6, "maze.algorithm.prim"),
    RECURSIVE_BACKTRACKER(7, "maze.algorithm.recursive_backtracker"),
    RECURSIVE_DIVISION(8, "maze.algorithm.recursive_division"),
    SIDEWINDER(9, "maze.algorithm.sidewinder"),
    WILSON(10, "maze.algorithm.wilson"),;

    final String name;
    final int id;

    MazeAlgorithm(int id, String name) {
        this.id = id;
        this.name = name;
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

}
