package cz.logicgo.persistence.services;

import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.persistence.dao.BridgeDAO;
import cz.logicgo.persistence.jpa.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.Optional;

import static cz.logicgo.persistence.dao.AbstractDAO.transExec;


public class BridgeService extends GameService {
    public void saveGame(Bridge bridge) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            BridgeDAO bridgeDAO = new BridgeDAO(em);

            transExec(em, () -> {
                if (bridge.getId() == null) {
                    bridgeDAO.save(bridge);
                } else {
                    bridge.prePersistUpdate();
                    bridgeDAO.update(bridge);
                }
            });
        }
    }

    public Optional<Bridge> getGameById(long id) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            BridgeDAO mazeDAO = new BridgeDAO(em);
            return mazeDAO.findById(id);
        }
    }
}
