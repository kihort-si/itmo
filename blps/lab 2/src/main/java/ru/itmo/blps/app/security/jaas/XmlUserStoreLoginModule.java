package ru.itmo.blps.app.security.jaas;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.itmo.blps.app.security.access.RolePrivileges;
import ru.itmo.blps.app.security.xml.StoredUserAccount;
import ru.itmo.blps.app.security.xml.XmlUserStore;

public class XmlUserStoreLoginModule implements LoginModule {
    private static XmlUserStore xmlUserStore;
    private static PasswordEncoder passwordEncoder;

    private Subject subject;
    private CallbackHandler callbackHandler;
    private StoredUserAccount authenticatedUser;
    private final Set<java.security.Principal> principals = new HashSet<>();

    public static void configure(XmlUserStore userStore, PasswordEncoder encoder) {
        xmlUserStore = userStore;
        passwordEncoder = encoder;
    }

    @Override
    public void initialize(Subject subject,
                           CallbackHandler callbackHandler,
                           Map<String, ?> sharedState,
                           Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        if (xmlUserStore == null || passwordEncoder == null) {
            throw new LoginException("JAAS login module не инициализирован");
        }

        NameCallback nameCallback = new NameCallback("username");
        PasswordCallback passwordCallback = new PasswordCallback("password", false);
        Callback[] callbacks = new Callback[]{nameCallback, passwordCallback};

        try {
            callbackHandler.handle(callbacks);
        } catch (UnsupportedCallbackException | java.io.IOException exception) {
            throw new LoginException("Не удалось получить учетные данные: " + exception.getMessage());
        }

        String username = nameCallback.getName();
        String password = new String(passwordCallback.getPassword() == null ? new char[0] : passwordCallback.getPassword());
        authenticatedUser = xmlUserStore.findByUsername(username)
                .orElseThrow(() -> new FailedLoginException("Неверный логин или пароль"));

        if (!passwordEncoder.matches(password, authenticatedUser.passwordHash())) {
            throw new FailedLoginException("Неверный логин или пароль");
        }

        principals.add(new UserIdentityPrincipal(
                authenticatedUser.username(),
                authenticatedUser.role(),
                authenticatedUser.participantId()
        ));
        principals.add(new RolePrincipal("ROLE_" + authenticatedUser.role().name()));
        RolePrivileges.get(authenticatedUser.role())
                .forEach(privilege -> principals.add(new PrivilegePrincipal(privilege.name())));
        return true;
    }

    @Override
    public boolean commit() {
        subject.getPrincipals().addAll(principals);
        return true;
    }

    @Override
    public boolean abort() {
        principals.clear();
        authenticatedUser = null;
        return true;
    }

    @Override
    public boolean logout() {
        subject.getPrincipals().removeAll(principals);
        principals.clear();
        authenticatedUser = null;
        return true;
    }
}
