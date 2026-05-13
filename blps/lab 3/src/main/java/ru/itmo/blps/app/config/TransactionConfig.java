package ru.itmo.blps.app.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@EnableTransactionManagement
public class TransactionConfig {

    @Bean("transactionManager")
    @Profile("wildfly")
    public PlatformTransactionManager wildflyTransactionManager() throws NamingException {
        InitialContext context = new InitialContext();
        UserTransaction userTransaction = (UserTransaction) context.lookup("java:comp/UserTransaction");
        TransactionManager transactionManager =
                (TransactionManager) context.lookup("java:jboss/TransactionManager");
        return new JtaTransactionManager(userTransaction, transactionManager);
    }

    @Bean("transactionManager")
    @Profile("!wildfly")
    public PlatformTransactionManager localTransactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return template;
    }

    @Bean
    @Qualifier("serializableTransactionTemplate")
    public TransactionTemplate serializableTransactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        template.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
        return template;
    }
}
