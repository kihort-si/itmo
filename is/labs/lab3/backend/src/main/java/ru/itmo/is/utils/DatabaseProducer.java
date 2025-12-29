package ru.itmo.is.utils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;

@ApplicationScoped
public class DatabaseProducer {

    @PersistenceUnit(unitName = "is-lab-pu")
    @Produces
    @ApplicationScoped
    private EntityManagerFactory entityManagerFactory;

    @PersistenceContext(unitName = "is-lab-pu")
    @Produces
    @jakarta.enterprise.context.RequestScoped
    private EntityManager entityManager;

    public void closeEntityManager(@jakarta.enterprise.inject.Disposes EntityManager em) {
    }
}
