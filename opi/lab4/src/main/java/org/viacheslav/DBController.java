package org.viacheslav;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Logger;


import jakarta.persistence.*;
import jakarta.transaction.Transactional;

public class DBController implements Serializable {

    public static final Logger logger = Logger.getLogger("DBController");

    private static volatile DBController instance;

    public static synchronized DBController getInstance() {
        DBController localInstance = instance;
        if (localInstance == null) {
            instance = localInstance = new DBController();
        }
        return localInstance;
    }

    @PersistenceContext
    private EntityManager manager;
    private final EntityTransaction transaction;

    public DBController() {
        EntityManagerFactory managerFactory = Persistence.createEntityManagerFactory("default");
        manager = managerFactory.createEntityManager();
        transaction = manager.getTransaction();
    }

    public ArrayList<Point> getAll() {
        try {
            transaction.begin();

            ArrayList<Point> results = new ArrayList<>(
                    manager.createQuery("select point from Point point", Point.class).getResultList()
            );

            transaction.commit();
            logger.info("все точки из таблицы получены");
            return results;
        }
        catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    public void addPoint(Point point) {
        try {
            transaction.begin();
            manager.persist(point);
            transaction.commit();
            logger.info("Добавлена точка: " + point.toString());
        }
        catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            e.printStackTrace();
        }


    }

    public void clear(String sessionId) {
        transaction.begin();
        manager.createQuery("delete from Point").executeUpdate();
        //manager.createQuery(String.format("delete from Point where session=\"%s\"", sessionId)).executeUpdate();
        logger.info("Таблица очищена");
        transaction.commit();
    }

}
