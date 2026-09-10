package cz.logicgo.core.misc.enums.keys.game;


import cz.logicgo.core.misc.enums.keys.HotkeyEvent;

public enum GeneralGameEvents implements HotkeyEvent {
    SAVE("save"),
    UNDO("undo"),
    REDO("redo"),
    RESET("reset"),
    SOLUTION("solution"),
    EXPORT("export"),
    PAUSE("pause"),
    HINT("hint"),
    EXIT("exit");


    final String name;

    GeneralGameEvents(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNamespace() {
        return "generalGame";
    }

    @Override
    public String getSectionTranslationKey() {
        return "settings.tab.general_game_hotkeys";
    }
}
