package com.vt.plugins

import com.vt.model.ChangeStatusRequest
import com.vt.model.HealthResponse
import com.vt.model.LoginRequest
import com.vt.model.RefreshRequest
import com.vt.model.RegisterRequest
import com.vt.service.AuthService
import com.vt.service.JwtService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        get("/health") {
            call.respond(HealthResponse(status = "ok"))
        }

        route("/api/v1/auth") {
            post("/register") {
                val request = call.receive<RegisterRequest>()
                call.respond(HttpStatusCode.Created, AuthService.register(request))
            }

            post("/login") {
                val request = call.receive<LoginRequest>()
                call.respond(
                    AuthService.login(
                        request = request,
                        userAgent = call.request.headers["User-Agent"],
                        ip = call.request.headers["X-Forwarded-For"] ?: call.request.local.remoteHost
                    )
                )
            }

            post("/refresh") {
                val request = call.receive<RefreshRequest>()
                call.respond(AuthService.refresh(request))
            }

            post("/logout") {
                val principal = JwtService.requirePrincipal(call.request.headers["Authorization"])
                AuthService.logout(principal)
                call.respond(HttpStatusCode.NoContent)
            }

            get("/me") {
                val principal = JwtService.requirePrincipal(call.request.headers["Authorization"])
                call.respond(AuthService.me(principal))
            }

            post("/changeStatus") {
                val request = call.receive<ChangeStatusRequest>()
                call.respond(AuthService.changeUserStatus(request))
            }

            get("/byUserId/{userId}") {
                val userId = call.parameters["userId"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid userId")
                call.respond(AuthService.getUserById(userId))
            }

            get("/byClntId/{clntId}") {
                val clntId = call.parameters["clntId"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid clntId")
                call.respond(AuthService.getUserByClntId(clntId))
            }

            delete("/byClntId/{clntId}") {
                val clntId = call.parameters["clntId"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid clntId")
                AuthService.deleteUserByClntId(clntId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}