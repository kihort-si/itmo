package ru.itmo.blps.taiga.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterInternalException;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

public class TaigaResourceAdapter implements ResourceAdapter {

    @Override
    public boolean equals(Object o) {
        return o instanceof TaigaResourceAdapter;
    }

    @Override
    public int hashCode() {
        return TaigaResourceAdapter.class.hashCode();
    }

    @Override
    public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
    }

    @Override
    public void stop() {
    }

    @Override
    public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec)
            throws ResourceException {
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
    }

    @Override
    public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {
        return null;
    }
}
