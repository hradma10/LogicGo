package cz.logicgo.persistence.services;

import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.persistence.dao.ShikakuDAO;
import cz.logicgo.persistence.jpa.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.Optional;

import static cz.logicgo.persistence.dao.AbstractDAO.transExec;


public class ShikakuService extends GameService {

    public void saveGame(Shikaku shikaku) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            ShikakuDAO shikakuDAO = new ShikakuDAO(em);
            transExec(em, () -> {
                if (shikaku.getId() == null) {
                    shikakuDAO.save(shikaku);
                } else {
                    shikakuDAO.update(shikaku);
                }
            });
        }
    }

    public Optional<Shikaku> getGameById(long id) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            ShikakuDAO shikakuDAO = new ShikakuDAO(em);
            return shikakuDAO.findById(id);
        }
    }
}
