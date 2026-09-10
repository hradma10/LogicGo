package cz.logicgo.persistence.services;

import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.persistence.dao.MazeDAO;
import cz.logicgo.persistence.jpa.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.Optional;

import static cz.logicgo.persistence.dao.AbstractDAO.transExec;


public class MazeService extends GameService {

    public void saveGame(Maze maze) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            MazeDAO mazeDAO = new MazeDAO(em);

            maze.prePersistUpdate();

            transExec(em, () -> {
                if (maze.getId() == null) {
                    mazeDAO.save(maze);
                } else {
                    mazeDAO.update(maze);
                }
            });
        }
    }

    public Optional<Maze> getGameById(long id) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            MazeDAO mazeDAO = new MazeDAO(em);
            return mazeDAO.findById(id);
        }
    }
}
