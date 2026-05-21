package com.vt.plugins

import com.vt.model.AcceptedResponse
import com.vt.model.EmailRequest
import com.vt.model.HealthResponse
import com.vt.model.PushRequest
import com.vt.service.NotifyService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

fun Application.configureRouting(notifyService: NotifyService) {
    routing {
        get("/health") {
            call.respond(HealthResponse("UP"))
        }

        post("/v1/notifications/email") {
            val req = call.receive<EmailRequest>()
            notifyService.sendEmail(req.to, req.subject, req.html)
            call.respond(HttpStatusCode.Accepted, AcceptedResponse())
        }

        post("/v1/notifications/push") {
            val req = call.receive<PushRequest>()
            notifyService.sendPush(req.deviceToken, req.title, req.message)
            call.respond(HttpStatusCode.Accepted, AcceptedResponse())
        }
    }
}
