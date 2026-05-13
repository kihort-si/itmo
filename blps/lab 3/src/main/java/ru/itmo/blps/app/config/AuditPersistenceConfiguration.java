package ru.itmo.blps.app.config;

import jakarta.persistence.EntityManagerFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.itmo.blps.audit.OrderStatusAuditRepository;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "app.messaging.order-status", name = "enabled", havingValue = "true")
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackageClasses = OrderStatusAuditRepository.class,
        entityManagerFactoryRef = "auditEntityManagerFactory",
        transactionManagerRef = "auditTransactionManager"
)
public class AuditPersistenceConfiguration {

    @Bean
    public LocalContainerEntityManagerFactoryBean auditEntityManagerFactory(DataSource dataSource) {
        createAuditSchema(dataSource);

        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setGenerateDdl(true);
        adapter.setShowSql(false);
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("ru.itmo.blps.audit");
        em.setPersistenceUnitName("audit");
        em.setJpaVendorAdapter(adapter);
        em.getJpaPropertyMap().put("hibernate.hbm2ddl.auto", "update");
        em.getJpaPropertyMap().put("hibernate.default_schema", "audit");
        return em;
    }

    @Bean
    public PlatformTransactionManager auditTransactionManager(
            @Qualifier("auditEntityManagerFactory") EntityManagerFactory auditEntityManagerFactory) {
        return new JpaTransactionManager(auditEntityManagerFactory);
    }

    private static void createAuditSchema(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE SCHEMA IF NOT EXISTS audit");
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create audit schema", e);
        }
    }
}
