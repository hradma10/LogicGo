package cz.logicgo.persistence.dao;


import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.misc.enums.Status;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.persistence.filter.FilterProperties;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;


public class GameDAO<T extends Game> extends AbstractDAO<T> {

    public GameDAO(Class<T> entityClass, EntityManager em) {
        super(entityClass, em);
    }


    @SuppressWarnings("unchecked")
    public GameDAO(EntityManager em) {
        super((Class<T>) Game.class, em);
    }

    public Game getExistingGameWithSeed(long seed, User player) {
        return em.createNamedQuery("Game.gamesWithSeedExistsForPlayer", Game.class)
                .setParameter("seed", seed)
                .setParameter("player", player)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public long getCountPlayedGames(User user) {
        return em.createNamedQuery("Game.gamesPlayed", Long.class)
                .setParameter("user", user)
                .getResultStream()
                .findFirst()
                .orElse(0L);
    }

    public long getCountGamesByStatus(User user, Status status) {
        return em.createNamedQuery("Game.gamesByStatusCount", Long.class)
                .setParameter("user", user)
                .setParameter("status", status)
                .getResultStream()
                .findFirst()
                .orElse(0L);
    }

    public Game getLastPlayedNonFinishedGame(User user) {
        return em.createNamedQuery("Game.lastPlayedNonFinishedGame", Game.class)
                .setParameter("player", user)
                .setParameter("status", Status.IN_PROGRESS)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public List<String> getAllThumbnailPaths() {
        return em.createNamedQuery("Game.getAllThumbnailPaths", String.class).getResultList();
    }

    public List<Game> getGamesByFilter(FilterProperties properties) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Game> query = cb.createQuery(Game.class);
        Root<Game> root = query.from(Game.class);

        List<Predicate> predicates = new ArrayList<>();

        if (properties.getUser() != null) {
            predicates.add(cb.equal(root.get("player"), properties.getUser()));
        }
        if (properties.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), properties.getStatus()));
        }
        if (properties.getDifficulty() != null) {
            predicates.add(cb.equal(root.get("difficulty"), properties.getDifficulty()));
        }
        if (properties.getWidth() != null && properties.getTypeGame() != TypeGame.SUDOKU) {
            predicates.add(cb.equal(root.get("width"), properties.getWidth()));
        }
        if (properties.getHeight() != null && properties.getTypeGame() != TypeGame.SUDOKU) {
            predicates.add(cb.equal(root.get("height"), properties.getHeight()));
        }

        if (properties.getTypeGame() != null) {
            predicates.add(cb.equal(root.get("typeGame"), properties.getTypeGame()));

            switch (properties.getTypeGame()) {
                case SUDOKU -> {
                    Root<Sudoku> sudokuRoot = cb.treat(root, Sudoku.class);
                    if (properties.getSudokuSize() != null)
                        predicates.add(cb.equal(sudokuRoot.get("type"), properties.getSudokuSize()));
                    if (properties.getSudokuVariant() != null)
                        predicates.add(cb.equal(sudokuRoot.get("variant"), properties.getSudokuVariant()));
                }
                case BRIDGE -> {
                    Root<Bridge> bridgeRoot = cb.treat(root, Bridge.class);
                    if (properties.getBridgeType() != null)
                        predicates.add(cb.equal(bridgeRoot.get("type"), properties.getBridgeType()));
                }
                case MAZE -> {
                    Root<Maze> mazeRoot = cb.treat(root, Maze.class);
                    if (properties.getMazeType() != null)
                        predicates.add(cb.equal(mazeRoot.get("mazeType"), properties.getMazeType()));
                    if (properties.getMazeShape() != null)
                        predicates.add(cb.equal(mazeRoot.get("mazeShape"), properties.getMazeShape()));
                    if (properties.getMazeAlgorithm() != null)
                        predicates.add(cb.equal(mazeRoot.get("mazeAlgorithm"), properties.getMazeAlgorithm()));
                }
                case SHIKAKU -> {
                    Root<Shikaku> shikakuRoot = cb.treat(root, Shikaku.class);
                    if (properties.getShikakuType() != null)
                        predicates.add(cb.equal(shikakuRoot.get("shikakuType"), properties.getShikakuType()));
                }
            }
        }

        query.where(predicates.toArray(new Predicate[0]));

        if (properties.getSortOption() != null) {
            String dbField = properties.getSortOption().getFieldName();

            if (properties.isAscending() != null && properties.isAscending()) {
                query.orderBy(cb.asc(root.get(dbField)));
            } else {
                query.orderBy(cb.desc(root.get(dbField)));
            }
        }

        return em.createQuery(query).getResultList();
    }

    public void updateGameThumbnail(long id, String name) {
        em.createNamedQuery("Game.updateThumbnailName")
                .setParameter("name", name)
                .setParameter("id", id)
                .executeUpdate();
    }

    public String getGameThumbnail(long id) {
        return em.createNamedQuery("Game.getThumbnailName", String.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse("");
    }

    public void deleteById(long id) {
        em.createNamedQuery("Game.deleteById")
                .setParameter("id", id)
                .executeUpdate();
    }

    public Game refresh(Game game) {
        em.refresh(game);
        return game;
    }

    public List<T> findAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        Root<T> root = cq.from(entityClass);
        cq.select(root);
        return em.createQuery(cq).getResultList();
    }

    public List<T> getAllUserGamesById(long userId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        Root<T> root = cq.from(entityClass);
        cq.select(root).where(cb.equal(root.get("player").get("id"), userId));
        return em.createQuery(cq).getResultList();
    }

    public List<T> getAllUserGamesByUsername(String username) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        Root<T> root = cq.from(entityClass);
        cq.select(root).where(cb.equal(root.get("player").get("username"), username));
        return em.createQuery(cq).getResultList();
    }

    public List<T> getLastNumberOfGames(long userId, int numberOfGames) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        Root<T> root = cq.from(entityClass);
        cq.select(root).where(cb.equal(root.get("player").get("id"), userId));
        cq.orderBy(cb.desc(root.get("lastPlayed")));
        return em.createQuery(cq).setMaxResults(numberOfGames).getResultList();
    }
}
