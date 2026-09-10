package cz.logicgo.core.misc.enums.settings;


import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.NodeType;

public enum GameSettings implements SettingKey {
    DIFFICULTY("difficulty", Difficulty.class, Difficulty.MEDIUM, NodeType.LIST);

    final String name;
    final Class<?> clazz;
    final Object defaultValue;
    final NodeType nodeType;

    GameSettings(String name, Class<?> clazz, Object defaultValue, NodeType nodeType) {
        this.name = name;
        this.clazz = clazz;
        this.defaultValue = defaultValue;
        this.nodeType = nodeType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNamespace() {
        return "game";
    }

    @Override
    public String getTranslationBase() {
        return "gameSettings";
    }

    @Override
    public Class<?> getClazz() {
        return clazz;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public NodeType getNodeType() {
        return null;
    }
}
