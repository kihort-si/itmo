package com.vt.model

// ──────────────────────────────────────────────
// HTTP API request / response bodies
// ──────────────────────────────────────────────

data class EmailRequest(
    val to: String,
    val subject: String,
    val html: String
)

data class PushRequest(
    val deviceToken: String,
    val title: String,
    val message: String
)

data class AcceptedResponse(
    val status: String = "QUEUED"
)

data class HealthResponse(val status: String)

data class ErrorResponse(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)

// ──────────────────────────────────────────────
// RabbitMQ message (published by core-worker)
// ──────────────────────────────────────────────

/**
 * Message format published by core-worker to the worker.mails exchange
 * and shovelled to reports.in queue (vhost /reports).
 *
 * Example:
 * {
 *   "type": "report",
 *   "reportType": "SUCCESSFULL_REGISTRATION_EMAIL",
 *   "data": { "name": "...", "username": "...", "email": "..." }
 * }
 */
data class MailMessage(
    val type: String,
    val reportType: String,
    val data: Map<String, Any?>
)

// ──────────────────────────────────────────────
// REP service response (rendered template)
// ──────────────────────────────────────────────

data class RenderedMessage(
    val channel: String,
    val subject: String?,
    val content: String
)

data class TemplateInfo(
    val tmplId: String,
    val code: String,
    val name: String,
    val channel: String,
    val subject: String?
)

// ──────────────────────────────────────────────
// Config
// ──────────────────────────────────────────────

data class SmtpConfig(
    val host: String,
    val port: Int,
    val user: String,
    val password: String,
    val from: String,
    val tls: Boolean
) {
    val isLogOnly: Boolean get() = host.isBlank()
}

data class RabbitConfig(
    val host: String,
    val port: Int,
    val vhost: String,
    val user: String,
    val password: String,
    val queue: String
)
