package ru.itmo.handler;

import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.itmo.dto.ErrorResponse;
import ru.itmo.exception.ApiException;
import ru.itmo.exception.DataAccessException;
import ru.itmo.security.OutputEncoder;

public final class ApiExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    public void register(RoutesConfig routes) {
        routes.exception(ApiException.class, (exception, ctx) ->
                respond(ctx, exception.status(), exception.getMessage()));
        routes.exception(DataAccessException.class, (exception, ctx) -> {
            LOGGER.error("Database operation failed", exception);
            respond(ctx, 500, "Internal server error");
        });
        routes.exception(Exception.class, (exception, ctx) -> {
            LOGGER.error("Unhandled request error", exception);
            respond(ctx, 500, "Internal server error");
        });
    }

    private static void respond(Context ctx, int status, String message) {
        ctx.status(status).json(new ErrorResponse(OutputEncoder.html(message)));
    }
}
