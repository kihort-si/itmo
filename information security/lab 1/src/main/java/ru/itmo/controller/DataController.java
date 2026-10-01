package ru.itmo.controller;

import io.javalin.http.Context;
import ru.itmo.dto.DataItem;
import ru.itmo.dto.DataResponse;
import ru.itmo.middleware.JwtAuthenticationMiddleware;
import ru.itmo.security.OutputEncoder;

import java.util.List;

public final class DataController {
    private static final List<DataItem> ITEMS = List.of(
            new DataItem(1, "Parameterized SQL queries"),
            new DataItem(2, "JWT authentication"),
            new DataItem(3, "Escaped API output")
    );

    public void getData(Context ctx) {
        String authenticatedUser = ctx.attribute(JwtAuthenticationMiddleware.AUTHENTICATED_USER);
        ctx.json(new DataResponse(OutputEncoder.html(authenticatedUser), ITEMS));
    }
}
