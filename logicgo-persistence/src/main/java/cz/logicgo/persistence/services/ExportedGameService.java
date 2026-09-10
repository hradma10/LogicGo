package cz.logicgo.persistence.services;

import cz.logicgo.core.entity.export.ExportDetail;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.persistence.dao.ExportedGamesDAO;
import cz.logicgo.persistence.jpa.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

import static cz.logicgo.persistence.dao.AbstractDAO.transExec;


public class ExportedGameService {
    public void saveDetail(ExportDetail exportDetail) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            ExportedGamesDAO exportedGamesDAO = new ExportedGamesDAO(em);
            transExec(em, () -> exportedGamesDAO.save(exportDetail));
        }
    }

    public void deleteById(long entryId) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            ExportedGamesDAO exportedGamesDAO = new ExportedGamesDAO(em);
            transExec(em, () -> exportedGamesDAO.deleteById(entryId));
        }
    }

    public List<ExportDetail> findByUser(User user) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            ExportedGamesDAO exportedGamesDAO = new ExportedGamesDAO(em);
            return exportedGamesDAO.findByUser(user);
        }
    }
}
