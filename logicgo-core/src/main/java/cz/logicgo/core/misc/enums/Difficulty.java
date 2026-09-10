package cz.logicgo.core.misc.enums;


import cz.logicgo.core.misc.TranslationLoader;
import cz.logicgo.core.misc.interfaces.PersistableEnum;
import cz.logicgo.core.misc.interfaces.Translatable;

public enum Difficulty implements Translatable, PersistableEnum {

    EASY(1, "difficulty.easy"),
    MEDIUM(2, "difficulty.medium"),
    HARD(3, "difficulty.hard");

    final int id;
    final String name;


    Difficulty(int id, String name) {
        this.id = id;
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


    @Override
    public int getId() {
        return id;
    }
}
