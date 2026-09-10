package cz.logicgo.persistence.dao;

import cz.logicgo.core.entity.export.ExportDetail;
import cz.logicgo.core.entity.user.User;
import jakarta.persistence.EntityManager;

import java.util.List;

public class ExportedGamesDAO extends AbstractDAO<ExportDetail> {

    public ExportedGamesDAO(EntityManager em) {
        super(ExportDetail.class, em);
    }

    public List<ExportDetail> findByUser(User user) {
        return em.createNamedQuery("ExportDetail.getByUser", ExportDetail.class)
                .setParameter("user", user)
                .getResultList();
    }

    public void deleteById(long id) {
        em.createNamedQuery("ExportDetail.deleteById")
                .setParameter("id", id)
                .executeUpdate();
    }
}
