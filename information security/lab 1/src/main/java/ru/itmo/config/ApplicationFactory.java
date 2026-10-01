package ru.itmo.config;

import io.javalin.Javalin;
import ru.itmo.controller.AuthController;
import ru.itmo.controller.DataController;
import ru.itmo.handler.ApiExceptionHandler;
import ru.itmo.middleware.JwtAuthenticationMiddleware;
import ru.itmo.repository.SqliteUserRepository;
import ru.itmo.repository.UserRepository;
import ru.itmo.routing.ApiRouter;
import ru.itmo.security.JwtService;
import ru.itmo.service.AuthService;

public final class ApplicationFactory {
    private ApplicationFactory() {
    }

    public static Javalin create(ApplicationSettings settings) {
        Database database = new Database(settings.databasePath());
        database.initialize();

        UserRepository userRepository = new SqliteUserRepository(database);
        JwtService jwtService = new JwtService(settings.jwtSecret());
        AuthService authService = new AuthService(userRepository, jwtService);

        ApiRouter router = new ApiRouter(
                new AuthController(authService),
                new DataController(),
                new JwtAuthenticationMiddleware(jwtService),
                new ApiExceptionHandler()
        );
        return Javalin.create(router::configure);
    }
}
