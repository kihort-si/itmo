package web.weblab4.database;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import web.weblab4.models.User;

import java.util.Optional;

@Stateless
public class UserDB {
    @Inject
    private SessionFactory sessionFactory;

    public boolean registerUser(User user) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try (session) {
            session.persist(user);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        }
    }

    public Optional<User> getUserByUsername(String email) {
        User user = null;
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try (session) {
            user = session.createQuery("FROM User WHERE email = :email", User.class)
                    .setParameter("email", email)
                    .uniqueResult();
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
        }
        return Optional.ofNullable(user);
    }

    public void changePassword(String email, String newPassword, String newResetToken) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try (session) {
            session.createQuery("UPDATE User SET password = :password, resetToken = :resetToken WHERE email = :email")
                    .setParameter("password", newPassword)
                    .setParameter("resetToken", newResetToken)
                    .setParameter("email", email)
                    .executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
        }
    }

    public String getEmailByResetToken(String resetToken) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        String email = "";
        try (session) {
            email = (String) session.createQuery("SELECT email FROM User WHERE resetToken = :resetToken")
                    .setParameter("resetToken", resetToken)
                    .uniqueResult();
        } catch (Exception e) {
            transaction.rollback();
        }
        return email;
    }

    public String getRefreshToken(String email) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        String refreshToken = "";
        try (session) {
            refreshToken = (String) session.createQuery("SELECT token FROM User WHERE email = :email")
                    .setParameter("email", email)
                    .uniqueResult();
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
        }
        return refreshToken;
    }

    public void setRefreshToken(String email, String token) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.createQuery("UPDATE User SET token = :refreshToken WHERE email = :email")
                    .setParameter("refreshToken", token)
                    .setParameter("email", email)
                    .executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        } finally {
            session.close();
        }
    }

    public boolean isValidResetToken(String token) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        boolean valid = false;
        try (session) {
            valid = session.createQuery("SELECT CASE WHEN count(u) > 0 THEN true ELSE false END FROM User u WHERE u.resetToken = :resetToken", Boolean.class)
                    .setParameter("resetToken", token)
                    .getSingleResult();
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
        }
        return valid;
    }

}
