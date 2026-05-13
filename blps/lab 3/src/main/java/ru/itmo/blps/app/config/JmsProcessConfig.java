package ru.itmo.blps.app.config;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Queue;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;

@Configuration(proxyBeanMethods = false)
@Profile("wildfly")
public class JmsProcessConfig {

    @Bean
    public ConnectionFactory xaConnectionFactory(
            @Value("${app.process.jms.connection-factory-jndi}") String jndiName) throws NamingException {
        return (ConnectionFactory) new InitialContext().lookup(jndiName);
    }

    @Bean(name = "checkoutProcessQueue")
    public Queue checkoutProcessQueue(
            @Value("${app.process.jms.checkout-queue-jndi}") String jndiName) throws NamingException {
        return (Queue) new InitialContext().lookup(jndiName);
    }

    @Bean(name = "deliveryProcessQueue")
    public Queue deliveryProcessQueue(
            @Value("${app.process.jms.delivery-queue-jndi}") String jndiName) throws NamingException {
        return (Queue) new InitialContext().lookup(jndiName);
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory xaConnectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate(xaConnectionFactory);
        jmsTemplate.setSessionTransacted(true);
        return jmsTemplate;
    }
}
