package ru.itmo.is.utils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.platform.database.PostgreSQLPlatform;
import org.eclipse.persistence.sequencing.NativeSequence;
import org.eclipse.persistence.sessions.*;
import ru.itmo.is.descriptor.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@ApplicationScoped
public class DatabaseProducer {
    @Produces
    @ApplicationScoped
    public Session createSession() {
        try {
            DataSource ds = (DataSource) new InitialContext().lookup("java:/jdbc/MyDS");

            JNDIConnector connector = new JNDIConnector();
            connector.setDataSource(ds);

            DatabaseLogin login = new DatabaseLogin();
            login.setPlatform(new PostgreSQLPlatform());
            login.setDefaultSequence(new NativeSequence());
            login.shouldUseNativeSequencing();
            login.setConnector(connector);

            Project project = new Project();

            project.setDatasourceLogin(login);

            addDescriptors(project);

            DatabaseSession session = project.createDatabaseSession();
            session.getSessionLog().setLevel(SessionLog.FINE);

            session.login();

            session.executeNonSelectingSQL("SET search_path TO s366389");

            return session;
        } catch (NamingException e) {
            throw new RuntimeException("Failed to lookup DataSource", e);
        }

    }

    private void addDescriptors(Project project) {
        project.addDescriptor(new MovieDescriptorBuilder().buildDescriptor());
        project.addDescriptor(new PersonDescriptorBuilder().buildDescriptor());
        project.addDescriptor(new CoordinatesDescriptorBuilder().buildDescriptor());
        project.addDescriptor(new LocationDescriptorBuilder().buildDescriptor());
    }
}
