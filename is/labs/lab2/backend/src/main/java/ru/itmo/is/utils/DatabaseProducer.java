package ru.itmo.is.utils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@ApplicationScoped
public class DatabaseProducer {
    private EntityManagerFactory emf;

    @Produces
    @ApplicationScoped
    public EntityManagerFactory createEntityManagerFactory() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("is-lab-pu");
        }
        return emf;
    }

    @Produces
    @jakarta.enterprise.context.RequestScoped
    public EntityManager createEntityManager(EntityManagerFactory emf) {
        return emf.createEntityManager();
    }

    public void closeEntityManager(@jakarta.enterprise.inject.Disposes EntityManager em) {
        if (em.isOpen()) {
            em.close();
        }
    }
}
