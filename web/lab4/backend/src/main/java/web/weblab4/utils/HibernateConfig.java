package web.weblab4.utils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import lombok.NoArgsConstructor;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.Serializable;

@NoArgsConstructor
@ApplicationScoped
public class HibernateConfig implements Serializable {
    private SessionFactory sessionFactory;

    @Produces
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @PreDestroy
    public void closeSessionFactory() {
        sessionFactory.close();
    }

    @PostConstruct
    public void init() {
        Configuration configuration = new Configuration().configure("/META-INF/hibernate.cfg.xml");
        sessionFactory = configuration.buildSessionFactory();

    }
}