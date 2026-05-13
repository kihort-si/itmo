package ru.itmo.blps.taiga.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.Referenceable;
import java.io.Serializable;

public interface TaigaEisConnectionFactory extends Serializable, Referenceable {

    TaigaPhysicalConnection getConnection() throws ResourceException;
}

