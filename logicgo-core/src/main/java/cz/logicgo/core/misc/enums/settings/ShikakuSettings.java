package cz.logicgo.core.misc.enums.settings;


import cz.logicgo.core.misc.enums.NodeType;

public enum ShikakuSettings implements SettingKey {
    HINTS_ON("hintsOn", Boolean.class, true, NodeType.CHECKBOX),
    SAME_DIMENSION("checkSameDim", Boolean.class, true, NodeType.CHECKBOX),
    TIMER("timer", Boolean.class, true, NodeType.CHECKBOX),

    ;

    final String name;
    final private Class<?> clazz;
    final private Object defaultValue;
    final private NodeType nodeType;
    ShikakuSettings(String name, Class<?> clazz, Object defaultValue, NodeType nodeType) {
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
        return "shikaku";
    }

    @Override
    public String getTranslationBase() {
        return "shikakuSettings";
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
        return nodeType;
    }
}
