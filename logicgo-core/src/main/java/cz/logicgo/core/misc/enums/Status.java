package cz.logicgo.core.misc.enums;


import cz.logicgo.core.misc.TranslationLoader;
import cz.logicgo.core.misc.interfaces.PersistableEnum;
import cz.logicgo.core.misc.interfaces.Translatable;

public enum Status implements Translatable, PersistableEnum {
    IN_PROGRESS(1, "status.inProgress"),
    FINISHED(2, "status.finished"),
    FAILED(3, "status.failed");

    final int id;
    final String name;

    Status(int id, String name) {
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
