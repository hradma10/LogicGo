package cz.logicgo.core.misc.enums;


import cz.logicgo.core.misc.TranslationLoader;
import cz.logicgo.core.misc.interfaces.PersistableEnum;
import cz.logicgo.core.misc.interfaces.Translatable;

import java.io.Serializable;

public enum TypeGame implements Translatable, Serializable, PersistableEnum {
    SUDOKU(1, "typeGame.sudoku"),
    MAZE(2, "typeGame.maze"),
    BRIDGE(3, "typeGame.bridge"),
    SHIKAKU(4, "typeGame.shikaku"),
    ;

    final int id;
    final String name;

    TypeGame(int id, String name) {
        this.id = id;
        this.name = name;
    }


    public static int toNumber(TypeGame typeGame) {
        return switch (typeGame) {
            case SUDOKU -> 0;
            case MAZE -> 1;
            case BRIDGE -> 2;
            case SHIKAKU -> 3;
        };
    }

    public static TypeGame toInstance(int value) {
        return switch (value) {
            case 0 -> TypeGame.SUDOKU;
            case 1 -> TypeGame.MAZE;
            case 2 -> TypeGame.BRIDGE;
            case 3 -> TypeGame.SHIKAKU;
            default -> throw new IllegalStateException("Unexpected value: " + value);
        };
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTranslation() {
        return TranslationLoader.getTranslation(name);
    }

    @Override
    public int getId() {
        return id;
    }
}
