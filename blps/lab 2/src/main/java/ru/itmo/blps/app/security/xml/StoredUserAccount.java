package ru.itmo.blps.app.security.xml;

import ru.itmo.blps.app.models.enums.UserRole;

public record StoredUserAccount(
        String username,
        String passwordHash,
        UserRole role,
        Long participantId
) {
}
