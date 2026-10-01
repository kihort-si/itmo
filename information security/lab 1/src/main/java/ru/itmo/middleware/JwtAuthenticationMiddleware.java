package ru.itmo.middleware;

import io.javalin.http.Context;
import ru.itmo.exception.ApiException;
import ru.itmo.security.JwtService;

import java.util.Optional;

public final class JwtAuthenticationMiddleware {
    public static final String AUTHENTICATED_USER = "authenticatedUser";

    private final JwtService jwtService;

    public JwtAuthenticationMiddleware(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public void authenticate(Context ctx) {
        String authorization = ctx.header("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ApiException(401, "Bearer token is required");
        }

        Optional<String> subject = jwtService.verify(authorization.substring(7));
        if (subject.isEmpty()) {
            throw new ApiException(401, "Token is invalid or expired");
        }
        ctx.attribute(AUTHENTICATED_USER, subject.get());
    }
}
