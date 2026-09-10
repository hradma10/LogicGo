package cz.logicgo.persistence.dao;

import cz.logicgo.core.entity.games.mazes.Maze;
import jakarta.persistence.EntityManager;

public class MazeDAO extends GameDAO<Maze> {

    public MazeDAO(EntityManager em) {
        super(Maze.class, em);
    }
}
