package cz.logicgo.core.misc.enums;


import cz.logicgo.core.misc.TranslationLoader;
import cz.logicgo.core.misc.interfaces.Translatable;

public enum TabType implements Translatable {
    MAIN("tab.type.main"),
    SUDOKU("tab.type.sudoku_game"),
    BRIDGE("tab.type.bridge_game"),
    MAZE("tab.type.maze_game"),
    SHIKAKU("tab.type.shikaku_game"),
    SUDOKU_SETTINGS("tab.type.sudoku_settings"),
    MAZE_SETTINGS("tab.type.maze_settings"),
    SHIKAKU_SETTINGS("tab.type.shikaku_settings"),
    BRIDGE_SETTINGS("tab.type.bridge_settings"),
    GAME_LIST("tab.type.game_list"),
    SETTINGS("tab.type.settings"),
    HISTORY("tab.type.history"),
    EXPORT_MULTIPLE("tab.type.export");

    private final String name;

    TabType(String name) {
        this.name = name;
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
