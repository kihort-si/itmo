package com.vt.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import com.vt.model.ErrorResponse
import com.vt.validation.SchemaValidator.ValidationException
import io.ktor.server.request.path
import java.time.Instant

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            val (status, message) = when (cause) {
                is NoSuchElementException -> HttpStatusCode.NotFound to cause.message
                is IllegalStateException -> HttpStatusCode.Conflict to cause.message
                is ValidationException -> HttpStatusCode.BadRequest to cause.message
                is IllegalArgumentException -> HttpStatusCode.BadRequest to cause.message
                else -> {
                    cause.printStackTrace()
                    HttpStatusCode.InternalServerError to "Internal server error"
                }
            }
            val errorResponse = ErrorResponse(
                timestamp = Instant.now().toString(),
                status = status.value,
                error = status.description,
                message = message ?: "Unknown error",
                path = call.request.path()
            )
            call.respond(status, errorResponse)
        }
    }
}