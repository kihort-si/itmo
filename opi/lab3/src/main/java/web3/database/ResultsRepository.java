package web3.database;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import web3.Result;

import java.io.Serializable;
import java.util.List;

@ApplicationScoped
@Named("resultRepository")
public class ResultsRepository implements Serializable {
    @Inject
    private SessionFactory sessionFactory;

    private List<Result> results;

    @PostConstruct
    private void init() {
        try {
            results = loadData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Result> getResultsList() {
        return results;
    }

    public void addResult(Result result) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.persist(result);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error while adding result", e);
        } finally {
            session.close();
        }
    }

    public void refreshResults() {
        this.results = loadData(); // Замените на метод, который загружает актуальные данные из БД
    }


    public List<Result> loadData() {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        List<Result> results;
        try {
            tx = session.beginTransaction();
            results = session.createQuery("from Result", Result.class).getResultList();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error while adding result", e);
        } finally {
            session.close();
        }
        return results;
    }

    public void removeResults() {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try (session) {
            tx = session.beginTransaction();
            session.createMutationQuery("delete from Result").executeUpdate();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }
}
