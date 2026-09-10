package cz.logicgo.core.misc.enums.settings;


import cz.logicgo.core.misc.enums.NodeType;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public enum UserSettings implements SettingKey {
    SAVE_FILE_PATH_EXPORT("saveFilePathExport", String.class, getUserDesktop(), NodeType.NONE, true),
    ;

    final private String name;
    final private Class<?> clazz;
    final private Object defaultValue;
    final private NodeType nodeType;
    final private boolean excludedFromReset;

    UserSettings(String name, Class<?> clazz, Object defaultValue, NodeType nodeType, boolean excludedFromReset) {
        this.name = name;
        this.clazz = clazz;
        this.defaultValue = defaultValue;
        this.nodeType = nodeType;
        this.excludedFromReset = excludedFromReset;
    }

    public static UserSettings getSettingByString(String value) {
        return Arrays.stream(values()).filter(settings -> settings.name.equals(value)).findFirst().orElse(null);
    }

    public static List<UserSettings> getAllSettingNames() {
        return List.of(values());
    }

    private static String getUserDesktop() {
        String userHome = System.getProperty("user.home", ".");
        File desktop = new File(userHome, "Desktop");
        if (desktop.exists() && desktop.isDirectory()) {
            return desktop.getAbsolutePath();
        }
        return userHome;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getNamespace() {
        return "user";
    }

    @Override
    public String getQualifiedName() {
        return String.format("%s:%s", getNamespace(), getName());
    }

    @Override
    public String getTranslationBase() {
        return "userSettings";
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public NodeType getNodeType() {
        return nodeType;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    @Override
    public boolean isExcludedFromReset() {
        return excludedFromReset;
    }
}
