package cz.logicgo.core.misc.enums.hints;


import cz.logicgo.core.misc.interfaces.Translatable;

public enum MazeHintType implements Translatable {
    SHOW_LITTLE_OF_PATH("maze.hint_type.little_of_path"),
    SHOW_ALL_OF_PATH("maze.hint_type.all_of_path"),
    ;

    final String name;

    MazeHintType(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
