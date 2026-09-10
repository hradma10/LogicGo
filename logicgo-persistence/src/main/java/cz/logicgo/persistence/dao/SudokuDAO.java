package cz.logicgo.persistence.dao;

import cz.logicgo.core.entity.games.sudoku.Sudoku;
import jakarta.persistence.EntityManager;

public class SudokuDAO extends GameDAO<Sudoku> {

    public SudokuDAO(EntityManager em) {
        super(Sudoku.class, em);
    }

}
