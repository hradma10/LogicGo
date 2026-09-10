package cz.logicgo.core.misc.interfaces;


import cz.logicgo.core.misc.TranslationLoader;

public interface Translatable {

    String getName();

    default String getTranslation() {
        return TranslationLoader.getTranslation(getName());
    }

}
