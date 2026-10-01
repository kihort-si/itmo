package ru.itmo.routing;

import io.javalin.config.JavalinConfig;
import ru.itmo.controller.AuthController;
import ru.itmo.controller.DataController;
import ru.itmo.handler.ApiExceptionHandler;
import ru.itmo.middleware.JwtAuthenticationMiddleware;
import ru.itmo.middleware.SecurityHeadersMiddleware;

public final class ApiRouter {
    private final AuthController authController;
    private final DataController dataController;
    private final JwtAuthenticationMiddleware authenticationMiddleware;
    private final ApiExceptionHandler exceptionHandler;

    public ApiRouter(
            AuthController authController,
            DataController dataController,
            JwtAuthenticationMiddleware authenticationMiddleware,
            ApiExceptionHandler exceptionHandler
    ) {
        this.authController = authController;
        this.dataController = dataController;
        this.authenticationMiddleware = authenticationMiddleware;
        this.exceptionHandler = exceptionHandler;
    }

    public void configure(JavalinConfig config) {
        config.startup.showJavalinBanner = false;
        config.http.defaultContentType = "application/json";
        config.http.maxRequestSize = 16_384;

        config.routes.before(SecurityHeadersMiddleware::addHeaders);
        config.routes.before("/api/*", authenticationMiddleware::authenticate);
        config.routes.post("/auth/register", authController::register);
        config.routes.post("/auth/login", authController::login);
        config.routes.get("/api/data", dataController::getData);
        exceptionHandler.register(config.routes);
    }
}
