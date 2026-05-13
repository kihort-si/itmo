package ru.itmo.blps.app.security.jaas;

import java.security.Principal;
import ru.itmo.blps.app.models.enums.UserRole;

public class UserIdentityPrincipal implements Principal {
    private final String username;
    private final UserRole role;
    private final Long participantId;

    public UserIdentityPrincipal(String username, UserRole role, Long participantId) {
        this.username = username;
        this.role = role;
        this.participantId = participantId;
    }

    @Override
    public String getName() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }

    public Long getParticipantId() {
        return participantId;
    }
}
