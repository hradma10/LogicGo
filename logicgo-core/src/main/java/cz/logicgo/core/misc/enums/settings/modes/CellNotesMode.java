package cz.logicgo.core.misc.enums.settings.modes;


import cz.logicgo.core.misc.interfaces.PersistableEnum;
import cz.logicgo.core.misc.interfaces.Translatable;

import static cz.logicgo.core.misc.Messages.getFormatted;

public enum CellNotesMode implements Translatable, PersistableEnum {
    AUTO(0, "sudokuSettings.cellNotes.auto"),
    MANUAL(1, "sudokuSettings.cellNotes.manual"),
    NONE(2, "sudokuSettings.cellNotes.off");

    private final int id;
    private final String translationKey;

    CellNotesMode(int id, String translationKey) {
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
