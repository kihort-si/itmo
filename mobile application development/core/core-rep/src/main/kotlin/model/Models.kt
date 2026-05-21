package com.vt.model

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

// ──────────────────────────────────────────────
// Domain
// ──────────────────────────────────────────────

enum class Channel { EMAIL, PUSH }

data class Template(
    val tmplId: UUID,
    val code: String,
    val name: String,
    val channel: Channel,
    val subject: String?,   // null for PUSH
    val body: String,
    val createdAt: Instant,
    val updatedAt: Instant
)

// ──────────────────────────────────────────────
// Request bodies
// ──────────────────────────────────────────────

data class EodReportRequest(
    val userId: String,     // UUID as string (per OpenAPI)
    val date: String        // ISO date: yyyy-MM-dd
)

data class TradeReportRequest(
    val tradeId: String,
    val userId: String,
    val ticker: String,
    val side: String,       // BUY | SELL
    val quantity: BigDecimal,
    val price: BigDecimal,
    val executedAt: String  // ISO date-time
)

data class RenderRequest(
    val variables: Map<String, Any?> = emptyMap()
)

// ──────────────────────────────────────────────
// Responses
// ──────────────────────────────────────────────

/**
 * Rendered message ready to pass to the Notification Service.
 * [subject] is populated for EMAIL channel templates.
 * [content] is the rendered body (HTML for EMAIL, text for PUSH).
 */
data class RenderedMessage(
    val channel: String,
    val subject: String?,
    val content: String
)

data class TemplateResponse(
    val tmplId: UUID,
    val code: String,
    val name: String,
    val channel: String,
    val subject: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class CreateTemplateRequest(
    val code: String,
    val name: String,
    val channel: String,    // EMAIL | PUSH
    val subject: String?,
    val body: String
)

data class HealthResponse(val status: String)

data class ErrorResponse(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)
