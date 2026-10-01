package ru.itmo.middleware;

import io.javalin.http.Context;

public final class SecurityHeadersMiddleware {
    private SecurityHeadersMiddleware() {
    }

    public static void addHeaders(Context ctx) {
        ctx.header("X-Content-Type-Options", "nosniff");
        ctx.header("Cache-Control", "no-store");
        ctx.header("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'");
    }
}
