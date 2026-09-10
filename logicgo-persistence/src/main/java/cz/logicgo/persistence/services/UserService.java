package cz.logicgo.persistence.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.gameClasses.keys.KeyEventDTO;
import cz.logicgo.core.misc.enums.keys.HotkeyEvent;
import cz.logicgo.persistence.dao.UserDAO;
import cz.logicgo.persistence.exceptions.database.user.UserErrorException;
import cz.logicgo.persistence.exceptions.database.user.UserExistsException;
import cz.logicgo.persistence.exceptions.database.user.UserNotExistsException;
import cz.logicgo.persistence.exceptions.database.user.WrongPasswordChangeException;
import cz.logicgo.persistence.jpa.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static cz.logicgo.persistence.dao.AbstractDAO.transExec;


public class UserService {

    private final SettingService settingService = new SettingService();

    public void saveUser(User user) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            UserDAO userDAO = new UserDAO(em);
            transExec(em, () -> userDAO.save(user));
        }
    }

    public void updateUser(User user) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            UserDAO userDAO = new UserDAO(em);
            transExec(em, () -> userDAO.update(user));
        }
    }

    public void removeUser(User user) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            UserDAO userDAO = new UserDAO(em);
            transExec(em, () -> userDAO.removeUser(user));
        }
    }

    public List<User> getAllUsers() {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            UserDAO userDAO = new UserDAO(em);
            return userDAO.findAll();
        }
    }

    public User login(String username, String password) throws UserErrorException {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            UserDAO userDAO = new UserDAO(em);

            Optional<User> optionalUser = userDAO.findByUsername(username);
            if (optionalUser.isEmpty()) {
                throw new UserNotExistsException();
            }

            String hashedPassword = userDAO.findHashByUsername(username);
            if (hashedPassword.isEmpty() || !checkPassword(password, hashedPassword)) {
                throw new UserNotExistsException();
            }

            return optionalUser.get();
        }
    }

    public User getUserById(long id) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            UserDAO userDAO = new UserDAO(em);
            return userDAO.findById(id).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean usersExist() {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            UserDAO userDAO = new UserDAO(em);
            return userDAO.hasUsers();
        } catch (Exception e) {
            return false;
        }
    }

    public User register(String username, String password, Map<HotkeyEvent, KeyEventDTO> defaultHotkeys) throws UserExistsException {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            UserDAO userDAO = new UserDAO(em);

            if (userDAO.findByUsername(username).isPresent()) {
                throw new UserExistsException();
            }

            String hashedPassword = hashPassword(password);
            User newUser = new User(username, hashedPassword);
            newUser.setHasPassword(password != null && !password.isBlank());

            if (defaultHotkeys != null && !defaultHotkeys.isEmpty()) {
                newUser.getSavedHotkeys().putAll(defaultHotkeys);
            }

            transExec(em, () -> userDAO.save(newUser));

            return newUser;
        }
    }

    public User register(String username, String password) throws UserExistsException {
        return register(username, password, Collections.emptyMap());
    }


    public void changePassword(User user, String oldPassword, String newPassword) throws WrongPasswordChangeException {
        if (!checkPassword(oldPassword, user.getHashedPassword())) {
            throw new WrongPasswordChangeException();
        }
        user.setHashedPassword(hashPassword(newPassword));

        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            UserDAO userDAO = new UserDAO(em);
            transExec(em, () -> userDAO.update(user));
        }
    }

    public static boolean checkPassword(String password, String hashedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hashedPassword);
        return result.verified;
    }

    public static String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }
}
