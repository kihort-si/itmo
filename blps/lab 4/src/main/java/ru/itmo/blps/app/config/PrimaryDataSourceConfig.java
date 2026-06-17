package ru.itmo.blps.app.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Configuration(proxyBeanMethods = false)
public class PrimaryDataSourceConfig {

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource,
            @Value("${spring.jpa.hibernate.ddl-auto:create-drop}") String ddlAuto,
            @Value("${spring.jpa.database-platform:org.hibernate.dialect.PostgreSQLDialect}") String dialect,
            @Value("${spring.jpa.properties.hibernate.format_sql:false}") String formatSql) {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setShowSql(true);
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("ru.itmo.blps.app.models");
        em.setJpaVendorAdapter(adapter);
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", ddlAuto);
        props.put("hibernate.dialect", dialect);
        props.put("hibernate.format_sql", formatSql);
        props.put("hibernate.implicit_naming_strategy",
                "org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy");
        props.put("hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        props.put("hibernate.hbm2ddl.import_files", "");
        em.setJpaPropertyMap(props);
        // Prevent WildFly JPA subsystem from wrapping with org.hibernate.SessionFactory
        em.setEntityManagerFactoryInterface(jakarta.persistence.EntityManagerFactory.class);
        return em;
    }

    @Bean
    public static BeanDefinitionRegistryPostProcessor dataSourceInitDependsOnEntityManagerFactory() {
        return new BeanDefinitionRegistryPostProcessor() {
            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
                String initializerBean = "dataSourceScriptDatabaseInitializer";
                if (registry.containsBeanDefinition(initializerBean)) {
                    BeanDefinition bd = registry.getBeanDefinition(initializerBean);
                    String[] existing = bd.getDependsOn();
                    if (existing == null) {
                        bd.setDependsOn("entityManagerFactory");
                    } else {
                        String[] updated = Arrays.copyOf(existing, existing.length + 1);
                        updated[existing.length] = "entityManagerFactory";
                        bd.setDependsOn(updated);
                    }
                }
            }
            

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            }
        };
    }
}
