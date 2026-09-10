package cz.logicgo.persistence.dao;

import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public abstract class AbstractDAO<T> {

    protected final EntityManager em;
    protected final Class<T> entityClass;


    public AbstractDAO(Class<T> entityClass, EntityManager em) {
        this.entityClass = entityClass;
        this.em = em;
    }

    public void save(T entity) {
        em.persist(entity);
    }


    public void update(T entity) {
        em.merge(entity);
    }

    public void delete(T entity) {
        em.remove(em.contains(entity) ? entity : em.merge(entity));
    }

    public Optional<T> findById(Object id) {
        return Optional.ofNullable(em.find(entityClass, id));
    }

    public void saveAll(List<T> entities) {
        entities.forEach(em::persist);
    }

    public synchronized static void transExec(EntityManager em, Runnable action) {
        try {
            em.getTransaction().begin();
            action.run();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Chyba při databázové transakci", e);
        }
    }
}
