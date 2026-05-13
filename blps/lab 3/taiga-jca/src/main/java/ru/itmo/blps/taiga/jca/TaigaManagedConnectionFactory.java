package ru.itmo.blps.taiga.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import javax.security.auth.Subject;

public class TaigaManagedConnectionFactory implements ManagedConnectionFactory, Serializable {

    private static final long serialVersionUID = 1L;

    private String baseUrl;
    private String bearerToken;
    private String userStoryId;

    public TaigaManagedConnectionFactory() {
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    public String getUserStoryId() {
        return userStoryId;
    }

    public void setUserStoryId(String userStoryId) {
        this.userStoryId = userStoryId;
    }

    @Override
    public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
        return new TaigaEisConnectionFactoryImpl(this, cxManager);
    }

    @Override
    public Object createConnectionFactory() throws ResourceException {
        throw new ResourceException("Container-managed ConnectionManager is required");
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo)
            throws ResourceException {
        return new TaigaManagedConnection(this);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxRequestInfo)
            throws ResourceException {
        if (connectionSet == null || connectionSet.isEmpty()) {
            return null;
        }
        return (ManagedConnection) connectionSet.iterator().next();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TaigaManagedConnectionFactory that)) {
            return false;
        }
        return Objects.equals(baseUrl, that.baseUrl)
                && Objects.equals(bearerToken, that.bearerToken)
                && Objects.equals(userStoryId, that.userStoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseUrl, bearerToken, userStoryId);
    }

    @Override
    public String toString() {
        return "TaigaManagedConnectionFactory";
    }
}
