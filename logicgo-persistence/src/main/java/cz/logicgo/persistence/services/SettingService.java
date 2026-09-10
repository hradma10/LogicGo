package cz.logicgo.persistence.services;

import cz.logicgo.core.entity.setting.Setting;
import cz.logicgo.core.entity.setting.UserSetting;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.misc.enums.settings.SettingKey;
import cz.logicgo.core.misc.enums.settings.UserSettings;
import cz.logicgo.persistence.dao.SettingDAO;
import cz.logicgo.persistence.jpa.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.ArrayList;
import java.util.List;

public class SettingService {

    public void checkUserSettings(User user) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            SettingDAO settingDAO = new SettingDAO(em);

            try {
                em.getTransaction().begin();

                List<UserSettings> settingNames = UserSettings.getAllSettingNames();
                List<SettingKey> settings = settingDAO.getUserSettingNames(user);

                ArrayList<UserSettings> unsetSettings = new ArrayList<>();

                for (UserSettings setting : settingNames) {
                    if (!settings.contains(setting)) {
                        unsetSettings.add(setting);
                    }
                }
                if (!unsetSettings.isEmpty()) {
                    ArrayList<Setting> unsetSettingNames = new ArrayList<>();
                    unsetSettings.forEach(setting -> {
                        Object defaultValue = setting.getDefaultValue();
                        UserSetting newSetting = new UserSetting(setting, defaultValue, user);
                        unsetSettingNames.add(newSetting);
                    });
                    settingDAO.saveAll(unsetSettingNames);
                }

                em.getTransaction().commit();
            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new RuntimeException("Nepodařilo se zkontrolovat a uložit uživatelská nastavení", e);
            }
        }
    }
}
