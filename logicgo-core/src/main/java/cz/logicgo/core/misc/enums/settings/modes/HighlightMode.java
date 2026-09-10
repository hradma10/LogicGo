package cz.logicgo.core.misc.enums.settings.modes;


import cz.logicgo.core.misc.interfaces.PersistableEnum;
import cz.logicgo.core.misc.interfaces.Translatable;

import static cz.logicgo.core.misc.Messages.getFormatted;

public enum HighlightMode implements Translatable, PersistableEnum {
    REGIONS(0, "sudokuSettings.highlight.regions"),
    SAME_NUMBERS(1, "sudokuSettings.highlight.number"),
    NONE(2, "sudokuSettings.highlight.off");

    private final int id;
    private final String translationKey;

    HighlightMode(int id, String translationKey) {
        this.id = id;
        this.translationKey = translationKey;
    }

    @Override
    public String getName() {
        return translationKey;
    }

    public String getTranslation() {
        return getFormatted(getName());
    }

    @Override
    public int getId() {
        return id;
    }
}
