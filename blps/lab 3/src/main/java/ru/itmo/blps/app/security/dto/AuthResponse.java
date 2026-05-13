package ru.itmo.blps.app.security.dto;

import java.util.List;
import ru.itmo.blps.app.models.enums.UserRole;

public record AuthResponse(
        String username,
        UserRole role,
        Long participantId,
        List<String> privileges,
        String authenticationScheme
) {
}
