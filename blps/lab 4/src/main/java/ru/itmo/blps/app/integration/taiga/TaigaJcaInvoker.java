package ru.itmo.blps.app.integration.taiga;

import java.lang.reflect.Method;
import javax.naming.InitialContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TaigaJcaInvoker {

    private static final Logger log = LoggerFactory.getLogger(TaigaJcaInvoker.class);

    private final String connectionFactoryJndi;

    public TaigaJcaInvoker(@Value("${app.taiga.jca.connection-factory-jndi:java:/eis/TaigaConnectionFactory}") String connectionFactoryJndi) {
        this.connectionFactoryJndi = connectionFactoryJndi;
    }

    public void syncOrderStatus(long orderId, String status, String eventId) throws Exception {
        Object cf = new InitialContext().lookup(connectionFactoryJndi);
        Method getConnection = cf.getClass().getMethod("getConnection");
        Object connection = getConnection.invoke(cf);
        try {
            Method sync = connection.getClass().getMethod("syncOrderStatus", long.class, String.class, String.class);
            sync.invoke(connection, orderId, status, eventId);
        } finally {
            Method close = connection.getClass().getMethod("close");
            close.invoke(connection);
        }
    }

    public void syncOrderStatusBestEffort(long orderId, String status, String eventId) {
        try {
            syncOrderStatus(orderId, status, eventId);
        } catch (Exception e) {
            log.warn("Taiga JCA sync skipped or failed: orderId={} status={} eventId={}", orderId, status, eventId, e);
        }
    }
}
