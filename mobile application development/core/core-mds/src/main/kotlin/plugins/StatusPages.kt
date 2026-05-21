package com.vt.plugins

import com.vt.model.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.statuspages.exception
import io.ktor.server.request.path
import io.ktor.server.response.respond
import java.time.Instant

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            val (status, message) = when (cause) {
                is NoSuchElementException  -> HttpStatusCode.NotFound        to cause.message
                is IllegalArgumentException -> HttpStatusCode.BadRequest     to cause.message
                is IllegalStateException   -> HttpStatusCode.Conflict        to cause.message
                else -> {
                    cause.printStackTrace()
                    HttpStatusCode.InternalServerError to "Internal server error"
                }
            }

            call.respond(
                status,
                ErrorResponse(
                    timestamp = Instant.now().toString(),
                    status    = status.value,
                    error     = status.description,
                    message   = message ?: "Unknown error",
                    path      = call.request.path()
                )
            )
        }
    }
}
