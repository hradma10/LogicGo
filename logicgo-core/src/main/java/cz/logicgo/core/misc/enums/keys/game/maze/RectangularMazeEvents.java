package cz.logicgo.core.misc.enums.keys.game.maze;


import cz.logicgo.core.misc.enums.keys.HotkeyEvent;

public enum RectangularMazeEvents implements HotkeyEvent {

    RECTANGLE_MOVE_UP("rectangleMoveUp"),
    RECTANGLE_MOVE_DOWN("rectangleMoveDown"),
    RECTANGLE_MOVE_LEFT("rectangleMoveLeft"),
    RECTANGLE_MOVE_RIGHT("rectangleMoveRight"),
    ;

    final String name;

    RectangularMazeEvents(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNamespace() {
        return "mazeGame";
    }

    @Override
    public String getSectionTranslationKey() {
        return "settings.tab.rect_maze_hotkeys";
    }
}
