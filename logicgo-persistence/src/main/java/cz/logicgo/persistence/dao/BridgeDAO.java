package cz.logicgo.persistence.dao;

import cz.logicgo.core.entity.games.bridges.Bridge;
import jakarta.persistence.EntityManager;

public class BridgeDAO extends GameDAO<Bridge> {

    public BridgeDAO(EntityManager em) {
        super(Bridge.class, em);
    }
}
