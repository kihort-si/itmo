package com.vt.plugins

import com.vt.dao.TemplateDao
import com.vt.model.*
import com.vt.service.ReportingService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import java.util.UUID

fun Application.configureRouting() {
    routing {

        get("/health") {
            call.respond(HealthResponse(status = "ok"))
        }

        // ────────────────────────────────────────────────────────────────
        // Reports
        // ────────────────────────────────────────────────────────────────

        route("/v1/reports") {

            /**
             * POST /v1/reports/end-of-day/build
             * Build an end-of-day portfolio report for a user.
             * Returns a rendered RenderedMessage (EMAIL channel).
             */
            post("/end-of-day/build") {
                val request = call.receive<EodReportRequest>()
                require(request.userId.isNotBlank()) { "userId is required" }
                require(request.date.isNotBlank())   { "date is required" }

                val rendered = ReportingService.buildEodReport(request)
                call.respond(rendered)
            }

            /**
             * POST /v1/reports/trade/build
             * Build a trade-execution report.
             * Returns a rendered RenderedMessage (EMAIL channel).
             */
            post("/trade/build") {
                val request = call.receive<TradeReportRequest>()
                require(request.tradeId.isNotBlank())  { "tradeId is required" }
                require(request.userId.isNotBlank())   { "userId is required" }
                require(request.ticker.isNotBlank())   { "ticker is required" }
                require(request.side.isNotBlank())     { "side is required" }
                require(request.executedAt.isNotBlank()) { "executedAt is required" }

                val rendered = ReportingService.buildTradeReport(request)
                call.respond(rendered)
            }
        }

        // ────────────────────────────────────────────────────────────────
        // Templates
        // ────────────────────────────────────────────────────────────────

        route("/v1/templates") {

            /**
             * GET /v1/templates
             * List all templates (metadata only, no body).
             */
            get {
                call.respond(TemplateDao.listAll())
            }

            /**
             * GET /v1/templates/{templateId}
             * Get template metadata by UUID.
             */
            get("/{templateId}") {
                val id = parseTemplateId(call.parameters["templateId"])
                val tmpl = TemplateDao.findById(id)
                    ?: throw NoSuchElementException("Template not found: $id")
                call.respond(
                    TemplateResponse(
                        tmplId    = tmpl.tmplId,
                        code      = tmpl.code,
                        name      = tmpl.name,
                        channel   = tmpl.channel.name,
                        subject   = tmpl.subject,
                        createdAt = tmpl.createdAt,
                        updatedAt = tmpl.updatedAt
                    )
                )
            }

            /**
             * POST /v1/templates/{templateId}/render
             * Render a template with caller-supplied variables.
             * Returns RenderedMessage with substituted content.
             */
            post("/{templateId}/render") {
                val id      = parseTemplateId(call.parameters["templateId"])
                val request = call.receive<RenderRequest>()

                val rendered = ReportingService.renderTemplate(id, request.variables)
                call.respond(rendered)
            }

            /**
             * POST /v1/templates
             * Create a new template.
             */
            post {
                val request = call.receive<CreateTemplateRequest>()
                require(request.code.isNotBlank())    { "code is required" }
                require(request.name.isNotBlank())    { "name is required" }
                require(request.body.isNotBlank())    { "body is required" }

                val channel = runCatching { Channel.valueOf(request.channel.uppercase()) }
                    .getOrElse { throw IllegalArgumentException("Invalid channel '${request.channel}'. Allowed: EMAIL, PUSH") }

                val tmpl = TemplateDao.create(
                    code    = request.code.trim(),
                    name    = request.name.trim(),
                    channel = channel,
                    subject = request.subject?.trim(),
                    body    = request.body
                )
                call.respond(
                    HttpStatusCode.Created,
                    TemplateResponse(
                        tmplId    = tmpl.tmplId,
                        code      = tmpl.code,
                        name      = tmpl.name,
                        channel   = tmpl.channel.name,
                        subject   = tmpl.subject,
                        createdAt = tmpl.createdAt,
                        updatedAt = tmpl.updatedAt
                    )
                )
            }
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun parseTemplateId(raw: String?): UUID =
    raw?.let { runCatching { UUID.fromString(it) }.getOrNull() }
        ?: throw IllegalArgumentException("Invalid templateId: must be a UUID")
