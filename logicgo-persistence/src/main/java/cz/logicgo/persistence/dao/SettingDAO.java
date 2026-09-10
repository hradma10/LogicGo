package cz.logicgo.persistence.dao;

import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.entity.setting.GameSetting;
import cz.logicgo.core.entity.setting.Setting;
import cz.logicgo.core.entity.setting.UserSetting;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.misc.enums.settings.SettingKey;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.HashMap;
import java.util.List;

import static cz.logicgo.core.misc.Helper.mapBy;


public class SettingDAO extends AbstractDAO<Setting> {

    public SettingDAO(EntityManager em) {
        super(Setting.class, em);
    }

    public UserSetting findUserSetting(User user, SettingKey key) {
        try {
            return em.createQuery("SELECT us FROM UserSetting us WHERE us.user = :user AND us.key = :key", UserSetting.class)
                    .setParameter("user", user)
                    .setParameter("key", key)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public HashMap<SettingKey, UserSetting> getAllUserSettings(User user) {
        List<UserSetting> resultList = em.createQuery(
                        "SELECT us FROM UserSetting us WHERE us.user = :user", UserSetting.class)
                .setParameter("user", user)
                .getResultList();

        return mapBy(resultList, UserSetting::getKey);
    }

    public void delete(Setting entity) {
        super.delete(entity);
    }

    public void save(Setting setting) {
        if (setting.getId() == null) {
            em.persist(setting);
        } else {
            em.merge(setting);
        }
    }

    public List<SettingKey> getUserSettingNames(User user) {
        return em.createNamedQuery("UserSetting.findKeysByUser", SettingKey.class)
                .setParameter("user", user)
                .getResultList();
    }

    public GameSetting findGameSetting(Game game, SettingKey key) {
        try {
            return em.createQuery("SELECT gs FROM GameSetting gs WHERE gs.game = :game AND gs.key = :key", GameSetting.class)
                    .setParameter("game", game)
                    .setParameter("key", key)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
