package ru.itmo.blps.taiga.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import javax.naming.Reference;

public class TaigaEisConnectionFactoryImpl implements TaigaEisConnectionFactory {

    private static final long serialVersionUID = 1L;

    private final TaigaManagedConnectionFactory mcf;
    private final ConnectionManager connectionManager;
    private Reference reference;

    public TaigaEisConnectionFactoryImpl(TaigaManagedConnectionFactory mcf, ConnectionManager connectionManager) {
        this.mcf = mcf;
        this.connectionManager = connectionManager;
    }

    @Override
    public TaigaPhysicalConnection getConnection() throws ResourceException {
        return (TaigaPhysicalConnection) connectionManager.allocateConnection(mcf, null);
    }

    @Override
    public void setReference(Reference reference) {
        this.reference = reference;
    }

    @Override
    public Reference getReference() {
        return reference;
    }
}
