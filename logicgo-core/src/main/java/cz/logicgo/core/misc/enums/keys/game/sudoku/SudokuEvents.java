package cz.logicgo.core.misc.enums.keys.game.sudoku;


import cz.logicgo.core.misc.enums.keys.HotkeyEvent;

public enum SudokuEvents implements HotkeyEvent {
    MOVE_UP("moveUp"),
    MOVE_DOWN("moveDown"),
    MOVE_LEFT("moveLeft"),
    MOVE_RIGHT("moveRight"),
    DELETE("delete"),
    NOTES_MODE("notes_mode");

    final String name;

    SudokuEvents(String name) {
        this.name = name;
    }

    public static HotkeyEvent getEventTypeFromName(String name) {
        return HotkeyEvent.getEventTypeFromName(SudokuEvents.class, name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNamespace() {
        return "sudokuGame";
    }

    @Override
    public String getSectionTranslationKey() {
        return "settings.tab.sudoku_hotkeys";
    }

}
