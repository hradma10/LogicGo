package cz.logicgo.core.misc.enums;


import cz.logicgo.core.misc.TranslationLoader;
import cz.logicgo.core.misc.interfaces.Translatable;

public enum SortOption implements Translatable {
    LAST_PLAYED("lastPlayed", "sortOption.lastPlayed"),
    TYPE("typeGame", "sortOption.type"),
    STATUS("status", "sortOption.status"),
    ELAPSED_TIME("elapsedTime", "sortOption.elapsedTime"),
    DIFFICULTY("difficulty", "sortOption.difficulty");

    private final String fieldName;
    private final String translationKey;

    SortOption(String fieldName, String translationKey) {
        this.fieldName = fieldName;
        this.translationKey = translationKey;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public String getName() {
        return translationKey;
    }

    @Override
    public String getTranslation() {
        return TranslationLoader.getTranslation(translationKey);
    }
}
