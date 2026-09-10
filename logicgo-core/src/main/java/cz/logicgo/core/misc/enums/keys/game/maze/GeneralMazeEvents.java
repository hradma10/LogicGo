package cz.logicgo.core.misc.enums.keys.game.maze;


import cz.logicgo.core.misc.enums.keys.HotkeyEvent;

public enum GeneralMazeEvents implements HotkeyEvent {
    LEVEL_CHANGE_UP("level_change_up"),
    LEVEL_CHANGE_DOWN("level_change_down");
    final String name;

    GeneralMazeEvents(String name) {
        this.name = name;
    }

    public static HotkeyEvent getEventTypeFromName(String name) {
        return HotkeyEvent.getEventTypeFromName(GeneralMazeEvents.class, name);
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
        return "settings.tab.maze_hotkeys";
    }
}
