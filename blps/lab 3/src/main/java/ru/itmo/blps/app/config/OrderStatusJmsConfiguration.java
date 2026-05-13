package ru.itmo.blps.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.BytesMessage;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;
import ru.itmo.blps.app.messaging.OrderStatusEventMessage;
import ru.itmo.blps.app.messaging.OrderStatusMessagingProperties;

@Configuration(proxyBeanMethods = false)
@EnableJms
@ConditionalOnProperty(prefix = "app.messaging.order-status", name = "enabled", havingValue = "true")
public class OrderStatusJmsConfiguration {

    @Bean
    public ConnectionFactory orderStatusActiveMqConnectionFactory(OrderStatusMessagingProperties properties) {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(properties.getBrokerUrl());
        factory.setTrustAllPackages(true);
        return factory;
    }

    @Bean
    public MessageConverter orderStatusMessageConverter(ObjectMapper objectMapper) {
        return new MessageConverter() {
            @Override
            public jakarta.jms.Message toMessage(Object object, Session session) throws JMSException {
                throw new UnsupportedOperationException("Send not supported by this converter");
            }

            @Override
            public Object fromMessage(jakarta.jms.Message message) throws JMSException {
                String json;
                if (message instanceof TextMessage tm) {
                    json = tm.getText();
                } else if (message instanceof BytesMessage bm) {
                    byte[] bytes = new byte[(int) bm.getBodyLength()];
                    bm.readBytes(bytes);
                    json = new String(bytes, StandardCharsets.UTF_8);
                } else {
                    throw new JMSException("Unsupported message type: " + message.getClass().getSimpleName());
                }
                try {
                    return objectMapper.readValue(json, OrderStatusEventMessage.class);
                } catch (IOException e) {
                    throw new MessageConversionException("Failed to deserialize order status event", e);
                }
            }
        };
    }

    @Bean
    public JmsListenerContainerFactory<?> orderStatusTopicListenerContainerFactory(
            ConnectionFactory orderStatusActiveMqConnectionFactory,
            MessageConverter orderStatusMessageConverter) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(orderStatusActiveMqConnectionFactory);
        factory.setPubSubDomain(true);
        factory.setSessionTransacted(true);
        factory.setConcurrency("1");
        factory.setMessageConverter(orderStatusMessageConverter);
        return factory;
    }
}
