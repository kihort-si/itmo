package ru.itmo.blps.app.security.jaas;

import java.util.List;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import ru.itmo.blps.app.security.AuthPrincipal;

@Service
public class XmlJaasAuthenticationService {
    private final Configuration configuration;

    public XmlJaasAuthenticationService(Configuration configuration) {
        this.configuration = configuration;
    }

    public AuthPrincipal authenticate(String username, String password) {
        try {
            Subject subject = new Subject();
            LoginContext loginContext = new LoginContext(
                    JaasLoginConfiguration.APPLICATION_NAME,
                    subject,
                    new JaasUsernamePasswordCallbackHandler(username, password),
                    configuration
            );
            loginContext.login();

            UserIdentityPrincipal identityPrincipal = subject.getPrincipals(UserIdentityPrincipal.class)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new BadCredentialsException("Не удалось получить principal пользователя"));

            List<String> authorities = subject.getPrincipals().stream()
                    .map(java.security.Principal::getName)
                    .distinct()
                    .toList();

            return new AuthPrincipal(
                    identityPrincipal.getName(),
                    identityPrincipal.getRole(),
                    identityPrincipal.getParticipantId(),
                    authorities
            );
        } catch (LoginException exception) {
            throw new BadCredentialsException("Неверный логин или пароль", exception);
        }
    }
}
