package cz.logicgo.persistence.dao;

import cz.logicgo.core.entity.user.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;
import java.util.Optional;

public class UserDAO extends AbstractDAO<User> {

    public UserDAO(EntityManager em) {
        super(User.class, em);
    }

    public Optional<User> findByUsername(String username) {
        return em.createNamedQuery("User.findByUsername", User.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    public void removeUser(User user) {
        User managedUser = em.find(User.class, user.getId());
        if (managedUser != null) {
            em.remove(managedUser);
        }
    }

    public Optional<User> findById(long id) {
        return em.createNamedQuery("User.findById", User.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    public List<User> findAllByUsernames(List<String> usernames) {
        if (usernames == null || usernames.isEmpty()) {
            return List.of();
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> user = cq.from(User.class);

        cq.select(user).where(user.get("username").in(usernames));

        return em.createQuery(cq).getResultList();
    }

    public String findHashByUsername(String username) {
        return em.createNamedQuery("User.findPasswordHashByUsername", String.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst()
                .orElse("");
    }

    public boolean hasUsers() {
        try {
            Long count = em.createNamedQuery("User.countAll", Long.class).getSingleResult();

            return count != null && count > 0;

        } catch (Exception e) {
            return false;
        }
    }

    public List<User> findAll() {
        return em.createNamedQuery("User.findAll", User.class).getResultList();
    }
}
