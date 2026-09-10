package cz.logicgo.core.misc.enums.gameTypes.maze;


import cz.logicgo.core.misc.TranslationLoader;
import cz.logicgo.core.misc.annotations.PreloadCategory;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.GameType;
import cz.logicgo.core.misc.interfaces.PersistableEnum;
import cz.logicgo.core.misc.interfaces.Translatable;

import java.util.ArrayList;
import java.util.List;

@PreloadCategory(folder = "maze")
public enum MazeType implements GameType, PersistableEnum, Translatable {
    CLASSIC(1, "maze.type.classic", allShapes(), allAlgorithms()),
    WALLS(2, "maze.type.breakable_walls", allShapes(), algsSupportingMasks()),
    PORTAL(3, "maze.type.portal", allShapes(), algsSupportingMasks()),
    PATTERN(4, "maze.type.pattern", allShapes(), algsSupportingMasks()),
    CHECKPOINT(5, "maze.type.checkpoint", allShapes(), algsSupportingMasks()),
    WRAP_AROUND(6, "maze.type.wrap_around", rectangular(), allAlgorithms()),
    EXACT_STEPS(7, "maze.type.exact_steps", allShapes(), allAlgorithms()),
    MULTI_LEVEL(8, "maze.type.multi_level", allShapes(), algsSupportingMasks()),
    ;

    final private int id;
    final private String name;
    final private List<MazeShape> supportedShapes;
    final private List<MazeAlgorithm> supportedAlgorithms;
    MazeType(int id, String name, List<MazeShape> supportedShapes, List<MazeAlgorithm> supportedAlgorithms) {
        this.id = id;
        this.name = name;
        this.supportedShapes = supportedShapes;
        this.supportedAlgorithms = supportedAlgorithms;
    }

    public static MazeType getInstanceById(int id) {
        for (MazeType type : MazeType.values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return null;
    }

    private static List<MazeShape> rectangular() {
        return List.of(MazeShape.RECTANGULAR);
    }

    private static List<MazeShape> allShapes() {
        return List.of(MazeShape.values());
    }

    private static List<MazeAlgorithm> algsSupportingMasks() {
        ArrayList<MazeAlgorithm> list = new ArrayList<>(List.of(MazeAlgorithm.values()));
        list.removeAll(List.of(MazeAlgorithm.RECURSIVE_DIVISION, MazeAlgorithm.BINARY_TREE, MazeAlgorithm.ELLER, MazeAlgorithm.SIDEWINDER));
        return list;
    }

    private static List<MazeAlgorithm> allAlgorithms() {
        return List.of(MazeAlgorithm.values());
    }

    public String getName() {
        return name;
    }

    @Override
    public String getTranslation() {
        return TranslationLoader.getTranslation(name);
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public TypeGame getTypeGame() {
        return TypeGame.MAZE;
    }

    public int getId() {
        return id;
    }

    public List<MazeShape> getSupportedShapes() {
        return supportedShapes;
    }

    public List<MazeAlgorithm> getSupportedAlgorithms() {
        return supportedAlgorithms;
    }
}
