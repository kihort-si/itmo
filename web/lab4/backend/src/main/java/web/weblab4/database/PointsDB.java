package web.weblab4.database;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import web.weblab4.models.Point;

import java.util.ArrayList;
import java.util.List;

@Stateless
public class PointsDB {
    @Inject
    SessionFactory sessionFactory;

    public void addPoint(Point point) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try (session) {
            session.persist(point);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
        }
    }

    public List<Point> getResultsByUser(int userId) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        List<Point> points = new ArrayList<>();
        try (session) {
            points = session.createQuery("SELECT p FROM Point p WHERE createdBy = :userId", Point.class)
                    .setParameter("userId", userId)
                    .getResultList();
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
        }
        return points;
    }

    public void removePoints(int userId) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try (session) {
            session.createQuery("DELETE FROM Point p WHERE createdBy = :userId")
                    .setParameter("userId", userId)
                    .executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
        }
    }
}
