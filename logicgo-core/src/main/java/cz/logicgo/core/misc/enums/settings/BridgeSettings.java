package cz.logicgo.core.misc.enums.settings;


import cz.logicgo.core.misc.enums.NodeType;

public enum BridgeSettings implements SettingKey {
    HINTS_ON("hintsOn", Boolean.class, true, NodeType.CHECKBOX),
    SAME_DIMENSION("checkSameDim", Boolean.class, true, NodeType.CHECKBOX),
    TIMER("timer", Boolean.class, true, NodeType.CHECKBOX),
    ;

    final String name;
    final private Class<?> clazz;
    final private Object defaultValue;
    final private NodeType nodeType;
    BridgeSettings(String name, Class<?> clazz, Object defaultValue, NodeType nodeType) {
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
        return "bridge";
    }

    @Override
    public String getTranslationBase() {
        return "bridgeSettings";
    }

    @Override
    public Class<?> getClazz() {
        return clazz;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    public NodeType getNodeType() {
        return nodeType;
    }
}
