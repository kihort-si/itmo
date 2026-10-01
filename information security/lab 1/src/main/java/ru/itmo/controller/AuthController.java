package ru.itmo.controller;

import io.javalin.http.Context;
import ru.itmo.dto.CredentialsRequest;
import ru.itmo.dto.RegisterResponse;
import ru.itmo.dto.TokenResponse;
import ru.itmo.exception.ApiException;
import ru.itmo.security.JwtService;
import ru.itmo.security.OutputEncoder;
import ru.itmo.service.AuthService;

public final class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public void register(Context ctx) {
        CredentialsRequest request = readCredentials(ctx);
        authService.register(request.login(), request.password());
        ctx.status(201).json(new RegisterResponse(
                "User registered",
                OutputEncoder.html(request.login())
        ));
    }

    public void login(Context ctx) {
        CredentialsRequest request = readCredentials(ctx);
        String token = authService.login(request.login(), request.password());
        ctx.json(new TokenResponse(token, "Bearer", JwtService.TOKEN_LIFETIME_SECONDS));
    }

    private static CredentialsRequest readCredentials(Context ctx) {
        try {
            return ctx.bodyAsClass(CredentialsRequest.class);
        } catch (RuntimeException exception) {
            throw new ApiException(400, "A JSON body with login and password is required");
        }
    }
}
