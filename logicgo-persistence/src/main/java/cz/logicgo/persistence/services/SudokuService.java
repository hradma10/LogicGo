package cz.logicgo.persistence.services;

import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.persistence.dao.SudokuDAO;
import cz.logicgo.persistence.jpa.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.Optional;

import static cz.logicgo.persistence.dao.AbstractDAO.transExec;


public class SudokuService extends GameService {

    public void saveGame(Sudoku sudoku) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            SudokuDAO sudokuDAO = new SudokuDAO(em);
            transExec(em, () -> {
                if (sudoku.getId() == null) {
                    sudokuDAO.save(sudoku);
                } else {
                    sudokuDAO.update(sudoku);
                }
            });
        }
    }

    public Optional<Sudoku> getGameById(long id) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            SudokuDAO sudokuDAO = new SudokuDAO(em);
            return sudokuDAO.findById(id);
        }
    }
}
