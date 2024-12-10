package web.weblab4.utils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.SecurityContext;
import web.weblab4.database.UserDB;
import web.weblab4.models.User;

import java.util.Optional;

@ApplicationScoped
public class SecurityManager {
    @Inject
    private UserDB userDB;
    @Inject
    private SecurityContext securityContext;

    public User getCurrentUser() {
        String username = securityContext.getUserPrincipal().getName();
        Optional<User> user = userDB.getUserByUsername(username);
        return user.orElse(null);
    }
}
