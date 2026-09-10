package cz.logicgo.core.misc.enums.keys.game.maze;


import cz.logicgo.core.misc.enums.keys.HotkeyEvent;

public enum HexagonalMazeEvents implements HotkeyEvent {

    HEXAGONAL_MOVE_TOP("hexagonalMoveTop"),
    HEXAGONAL_MOVE_TOP_RIGHT("hexagonalMoveTopRight"),
    HEXAGONAL_MOVE_BOTTOM_RIGHT("hexagonalMoveBottomRight"),
    HEXAGONAL_MOVE_BOTTOM("hexagonalMoveBottom"),
    HEXAGONAL_MOVE_BOTTOM_LEFT("hexagonalMoveBottomLeft"),
    HEXAGONAL_MOVE_TOP_LEFT("hexagonalMoveTopLeft"),
    ;

    final String name;

    HexagonalMazeEvents(String name) {
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
        return "settings.tab.hex_maze_hotkeys";
    }
}
