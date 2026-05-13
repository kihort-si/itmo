package ru.itmo.blps.taiga.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionEvent;
import jakarta.resource.spi.ConnectionEventListener;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.LocalTransaction;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionMetaData;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

public class TaigaManagedConnection implements ManagedConnection {

    private final TaigaManagedConnectionFactory mcf;
    private final List<ConnectionEventListener> listeners = new CopyOnWriteArrayList<>();
    private PrintWriter logWriter;
    private volatile TaigaPhysicalConnection handle;

    public TaigaManagedConnection(TaigaManagedConnectionFactory mcf) {
        this.mcf = mcf;
    }

    void notifyClosed(TaigaPhysicalConnection physicalConnection) {
        if (handle == physicalConnection) {
            handle = null;
        }
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
        event.setConnectionHandle(physicalConnection);
        for (ConnectionEventListener listener : listeners) {
            listener.connectionClosed(event);
        }
    }

    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        handle = new TaigaPhysicalConnection(this, mcf);
        return handle;
    }

    @Override
    public void destroy() throws ResourceException {
        if (handle != null) {
            handle.close();
            handle = null;
        }
        listeners.clear();
    }

    @Override
    public void cleanup() throws ResourceException {
        if (handle != null) {
            handle.close();
            handle = null;
        }
    }

    @Override
    public void associateConnection(Object connection) throws ResourceException {
        if (connection instanceof TaigaPhysicalConnection p) {
            this.handle = p;
        }
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public XAResource getXAResource() throws ResourceException {
        return null;
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        return null;
    }

    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        return new TaigaManagedConnectionMetaData();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        this.logWriter = out;
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return logWriter;
    }
}
