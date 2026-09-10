package cz.logicgo.persistence.services;


import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.misc.enums.Status;
import cz.logicgo.persistence.dao.GameDAO;
import cz.logicgo.persistence.filter.FilterProperties;
import cz.logicgo.persistence.jpa.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

import static cz.logicgo.persistence.dao.AbstractDAO.transExec;


public class GameService {

    public void saveGames(List<Game> gameList) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            GameDAO<Game> gameDAO = new GameDAO<>(em);
            transExec(em, () -> {
                for (Game game : gameList) {
                    if (game.getId() == null) {
                        gameDAO.save(game);
                    } else {
                        gameDAO.update(game);
                    }
                }
            });
        }
    }

    public void deleteById(long id) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            GameDAO<Game> gameDAO = new GameDAO<>(em);
            transExec(em, () -> gameDAO.deleteById(id));
        }
    }

    public void updateGameThumbnail(long id, String name) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            GameDAO<Game> gameDAO = new GameDAO<>(em);
            transExec(em, () -> gameDAO.updateGameThumbnail(id, name));
        }
    }

    public Game getExistingGameWithSeed(long seed, User player) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            GameDAO<Game> gameDAO = new GameDAO<>(em);
            return gameDAO.getExistingGameWithSeed(seed, player);
        }
    }

    public long getCountPlayedGames(User user) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            GameDAO<Game> gameDAO = new GameDAO<>(em);
            return gameDAO.getCountPlayedGames(user);
        }
    }

    public long getCountGamesByStatus(User user, Status status) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            GameDAO<Game> gameDAO = new GameDAO<>(em);
            return gameDAO.getCountGamesByStatus(user, status);
        }
    }

    public Game getLastPlayedNonFinishedGame(User user) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            GameDAO<Game> gameDAO = new GameDAO<>(em);
            return gameDAO.getLastPlayedNonFinishedGame(user);
        }
    }

    public List<String> getAllThumbnailPaths() {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            GameDAO<Game> gameDAO = new GameDAO<>(em);
            return gameDAO.getAllThumbnailPaths();
        }
    }

    public String getGameThumbnail(long id) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            GameDAO<Game> gameDAO = new GameDAO<>(em);
            return gameDAO.getGameThumbnail(id);
        }
    }

    public List<Game> getGamesByFilter(FilterProperties properties) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            GameDAO<Game> gameDAO = new GameDAO<>(em);
            return gameDAO.getGamesByFilter(properties);
        }
    }

    public List<Game> findAll() {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            GameDAO<Game> gameDAO = new GameDAO<>(em);
            return gameDAO.findAll();
        }
    }

    public List<Game> getAllUserGamesById(long userId) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            GameDAO<Game> gameDAO = new GameDAO<>(em);
            return gameDAO.getAllUserGamesById(userId);
        }
    }

    public List<Game> getAllUserGamesByUsername(String username) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            GameDAO<Game> gameDAO = new GameDAO<>(em);
            return gameDAO.getAllUserGamesByUsername(username);
        }
    }

    public List<Game> getLastNumberOfGames(long userId, int numberOfGames) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            GameDAO<Game> gameDAO = new GameDAO<>(em);
            return gameDAO.getLastNumberOfGames(userId, numberOfGames);
        }
    }
}
