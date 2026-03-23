package ru.itmo.blps.app.security.dto;

import ru.itmo.blps.app.models.enums.UserRole;

public record AuthResponse(
        String token,
        Long userId,
        UserRole role,
        Long participantId
) {
}
