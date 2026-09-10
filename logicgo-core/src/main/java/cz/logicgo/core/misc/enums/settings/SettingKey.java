package cz.logicgo.core.misc.enums.settings;


import cz.logicgo.core.misc.TranslationLoader;
import cz.logicgo.core.misc.enums.NodeType;
import cz.logicgo.core.misc.interfaces.Translatable;

public interface SettingKey extends Translatable {


    default boolean isExcludedFromReset() {
        return false;
    }

    String getName();

    String getNamespace();

    default String getQualifiedName() {
        return getNamespace() + ":" + getName();
    }

    default String getTranslationKey() {
        return getTranslationBase() + "." + getName();
    }

    String getTranslationBase();

    Class<?> getClazz();

    Object getDefaultValue();

    NodeType getNodeType();

    @Override
    default String getTranslation() {
        return TranslationLoader.getTranslation(getTranslationKey());
    }

    default String getDescription() {
        return TranslationLoader.getTranslation(String.format("%s.description", getQualifiedName()));
    }

}
