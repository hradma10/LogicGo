package cz.logicgo.core.misc.enums;


import cz.logicgo.core.misc.interfaces.PersistableEnum;
import cz.logicgo.core.misc.interfaces.Translatable;

import static cz.logicgo.core.misc.Messages.getFormatted;


public enum PageLayout implements Translatable, PersistableEnum {
    AUTOMATIC(0, null, "export.layout.automatic"),
    ONE(1, 1, "export.layout.one"),
    TWO(2, 2, "export.layout.two"),
    ;
    private final int id;
    private final Integer count;
    private final String name;

    PageLayout(int id, Integer count, String name) {
        this.id = id;
        this.count = count;
        this.name = name;
    }

    public Integer getGamesPerPage() {
        return count;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getTranslation() {
        return getFormatted(name);
    }

    @Override
    public int getId() {
        return id;
    }
}
