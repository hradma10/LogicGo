package cz.logicgo.persistence.services;

import cz.logicgo.core.entity.setting.GameSetting;
import cz.logicgo.core.entity.setting.UserSetting;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.factoryInit.GameInit;
import cz.logicgo.core.misc.enums.settings.SettingKey;
import cz.logicgo.core.misc.enums.settings.SettingKeyRegistry;
import cz.logicgo.core.misc.enums.settings.UserSettings;
import cz.logicgo.persistence.dao.SettingDAO;
import cz.logicgo.persistence.jpa.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cz.logicgo.persistence.dao.AbstractDAO.transExec;


public class UserSettingsService {

    private static final List<SettingKey> settingKeys;

    static {
        settingKeys = SettingKeyRegistry.getAllKeys();
    }

    public UserSettingsService() {
    }

    public Object getSettingValue(User user, UserSettings key) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            SettingDAO settingDAO = new SettingDAO(em);

            UserSetting dbSetting = settingDAO.findUserSetting(user, key);
            if (dbSetting != null) {
                return dbSetting.getTypedValue();
            }
            return key.getDefaultValue();
        }
    }

    public Map<SettingKey, Object> loadAllSettings(User user) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            SettingDAO settingDAO = new SettingDAO(em);

            Map<SettingKey, UserSetting> overrides = settingDAO.getAllUserSettings(user);

            Map<SettingKey, Object> fullSettingsMap = new HashMap<>();

            loadKeysIntoMap(fullSettingsMap, overrides);

            return fullSettingsMap;
        }
    }

    private void loadKeysIntoMap(Map<SettingKey, Object> map, Map<SettingKey, UserSetting> overrides) {
        for (SettingKey key : settingKeys) {
            if (overrides.containsKey(key)) {
                map.put(key, overrides.get(key).getTypedValue());
            } else {
                map.put(key, key.getDefaultValue());
            }
        }
    }

    public void resetSettingsToDefault(User user) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            SettingDAO settingDAO = new SettingDAO(em);
            transExec(em, () -> {
                Map<SettingKey, UserSetting> existingDbSettings = settingDAO.getAllUserSettings(user);

                existingDbSettings.forEach((key, existingSetting) -> {
                    if (!key.isExcludedFromReset()) {
                        settingDAO.delete(existingSetting);
                    }
                });
            });
        }
    }

    public void saveSetting(User user, SettingKey settingKey, Object newValue) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            SettingDAO settingDAO = new SettingDAO(em);
            Map<SettingKey, UserSetting> existingDbSettings = settingDAO.getAllUserSettings(user);
            var optSett = existingDbSettings.entrySet().stream().filter(entry -> entry.getKey() == settingKey).findFirst();
            transExec(em, () -> {
                Object defaultValue = settingKey.getDefaultValue();
                if (optSett.isPresent()) {
                    UserSetting settingInstance = optSett.get().getValue();
                    if (newValue.equals(defaultValue)) {
                        settingDAO.delete(settingInstance);
                    } else {
                        settingInstance.setTypedValue(newValue);
                        settingDAO.save(settingInstance);
                    }
                } else {
                    UserSetting newSetting = new UserSetting(settingKey, newValue, user);
                    settingDAO.save(newSetting);
                }
            });
        }
    }

    public void saveSettingsFromBuffer(User user, Map<SettingKey, Object> buffer) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            SettingDAO settingDAO = new SettingDAO(em);
            Map<SettingKey, UserSetting> existingDbSettings = settingDAO.getAllUserSettings(user);

            transExec(em, () -> {
                for (Map.Entry<SettingKey, Object> entry : buffer.entrySet()) {
                    SettingKey key = entry.getKey();
                    Object newValue = entry.getValue();
                    Object defaultValue = key.getDefaultValue();

                    UserSetting existingSetting = existingDbSettings.get(key);

                    if (newValue.equals(defaultValue)) {
                        if (existingSetting != null) {
                            settingDAO.delete(existingSetting);
                        }
                    } else {
                        if (existingSetting != null) {
                            existingSetting.setTypedValue(newValue);
                            settingDAO.save(existingSetting);
                        } else {
                            UserSetting newSetting = new UserSetting(key, newValue, user);
                            settingDAO.save(newSetting);
                        }
                    }
                }
            });
        }
    }

    public void saveSettingsFromBuffer(GameInit gameInit) {
        if (gameInit == null || gameInit.getPlayer() == null || gameInit.getSettings() == null) {
            return;
        }

        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            SettingDAO settingDAO = new SettingDAO(em);
            var user = gameInit.getPlayer();
            Map<SettingKey, UserSetting> existingDbSettings = settingDAO.getAllUserSettings(user);

            transExec(em, () -> {
                for (Map.Entry<SettingKey, GameSetting> entry : gameInit.getSettings().entrySet()) {
                    SettingKey key = entry.getKey();

                    if (key == null) {
                        continue;
                    }

                    GameSetting gameSetting = entry.getValue();
                    Object newValue = (gameSetting != null) ? gameSetting.getTypedValue() : null;
                    Object defaultValue = key.getDefaultValue();

                    if (newValue == null) {
                        continue;
                    }

                    UserSetting existingSetting = existingDbSettings.get(key);

                    if (newValue.equals(defaultValue)) {
                        if (existingSetting != null) {
                            settingDAO.delete(existingSetting);
                        }
                    } else {
                        if (existingSetting != null) {
                            existingSetting.setTypedValue(newValue);
                            settingDAO.save(existingSetting);
                        } else {
                            UserSetting newSetting = new UserSetting(key, newValue, user);
                            settingDAO.save(newSetting);
                        }
                    }
                }
            });
        }
    }
}
