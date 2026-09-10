package cz.logicgo.core.misc.enums;


import cz.logicgo.core.misc.TranslationLoader;
import cz.logicgo.core.misc.interfaces.PersistableEnum;
import cz.logicgo.core.misc.interfaces.Translatable;

public enum PreviewType implements Translatable, PersistableEnum {
    SOLVED(1, "previewType.solved"),
    UNSOLVED(2, "previewType.unsolved"),
    BOTH(3, "previewType.both");

    final int id;
    final String name;
    PreviewType(int id, String name) {
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
