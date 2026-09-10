package cz.logicgo.core.misc.enums.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SettingKeyRegistry {
    private static final List<SettingKey> ALL_KEYS = new ArrayList<>();

    static {
        register(UserSettings.values());
        register(SudokuSettings.values());
        register(GameSettings.values());
        register(MazeSettings.values());
        register(BridgeSettings.values());
        register(ShikakuSettings.values());
    }

    private static void register(SettingKey[] keys) {
        Collections.addAll(ALL_KEYS, keys);
    }

    public static List<SettingKey> getAllKeys() {
        return Collections.unmodifiableList(ALL_KEYS);
    }
}
