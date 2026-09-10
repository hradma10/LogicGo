package cz.logicgo.engine.algorithms.mazes;

import cz.logicgo.core.misc.enums.gameTypes.maze.MazeAlgorithm;
import cz.logicgo.engine.algorithms.mazes.algorithmImplementations.*;

import java.util.EnumMap;
import java.util.Map;

public final class MazeGeneratorFactory {

    private static final Map<MazeAlgorithm, MazeGeneratorAction> GENERATORS = new EnumMap<>(MazeAlgorithm.class);

    static {
        GENERATORS.put(MazeAlgorithm.KRUSKAL, KruskalsMazeGenerator::runOn);
        GENERATORS.put(MazeAlgorithm.ALDOUS_BRODER, AldousBroderMazeGenerator::runOn);
        GENERATORS.put(MazeAlgorithm.BINARY_TREE, BinaryTreeMazeGenerator::runOn);
        GENERATORS.put(MazeAlgorithm.ELLER, EllersMazeGenerator::runOn);
        GENERATORS.put(MazeAlgorithm.HUNT_AND_KILL, HuntAndKillMazeGenerator::runOn);
        GENERATORS.put(MazeAlgorithm.PRIM, PrimMazeGenerator::runOn);
        GENERATORS.put(MazeAlgorithm.RECURSIVE_BACKTRACKER, RecursiveBacktrackerMazeGenerator::runOn);
        GENERATORS.put(MazeAlgorithm.RECURSIVE_DIVISION, RecursiveDivisionMazeGenerator::runOn);
        GENERATORS.put(MazeAlgorithm.SIDEWINDER, SidewinderMazeGenerator::runOn);
        GENERATORS.put(MazeAlgorithm.WILSON, WilsonsMazeGenerator::runOn);
    }

    private MazeGeneratorFactory() {}

    public static MazeGeneratorAction getGenerator(MazeAlgorithm algorithm) {
        MazeGeneratorAction generator = GENERATORS.get(algorithm);
        if (generator == null) {
            throw new IllegalArgumentException("No generator registered for algorithm: " + algorithm);
        }
        return generator;
    }
}
