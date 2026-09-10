package cz.logicgo.persistence.dao;

import cz.logicgo.core.entity.games.shikaku.Shikaku;
import jakarta.persistence.EntityManager;

public class ShikakuDAO extends GameDAO<Shikaku> {

    public ShikakuDAO(EntityManager em) {
        super(Shikaku.class, em);
    }
}
