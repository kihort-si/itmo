package com.vt.service

import com.vt.dao.TemplateDao
import com.vt.model.EodReportRequest
import com.vt.model.RenderedMessage
import com.vt.model.Template
import com.vt.model.TradeReportRequest

/**
 * Business logic for building and rendering reports.
 *
 * Both report builders follow the same pattern:
 *  1. Look up the appropriate template by well-known code.
 *  2. Build the variable map from the request.
 *  3. Render body (and subject if EMAIL) via Mustache.
 *  4. Return [RenderedMessage] — caller can forward it to the Notification Service.
 */
object ReportingService {

    // ──────────────────────────────────────────────────────────────────────
    // Report: end-of-day portfolio summary
    // ──────────────────────────────────────────────────────────────────────

    fun buildEodReport(request: EodReportRequest): RenderedMessage {
        val template = requireTemplate("EOD_REPORT")

        val variables: Map<String, Any?> = mapOf(
            "userId" to request.userId,
            "date"   to request.date
        )

        return render(template, variables)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Report: single trade execution notification
    // ──────────────────────────────────────────────────────────────────────

    fun buildTradeReport(request: TradeReportRequest): RenderedMessage {
        val template = requireTemplate("TRADE_EXECUTED")

        val variables: Map<String, Any?> = mapOf(
            "tradeId"    to request.tradeId,
            "userId"     to request.userId,
            "ticker"     to request.ticker,
            "side"       to request.side,
            "quantity"   to request.quantity.toPlainString(),
            "price"      to request.price.toPlainString(),
            "executedAt" to request.executedAt
        )

        return render(template, variables)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Generic: render any template by UUID with caller-supplied variables
    // ──────────────────────────────────────────────────────────────────────

    fun renderTemplate(tmplId: java.util.UUID, variables: Map<String, Any?>): RenderedMessage {
        val template = TemplateDao.findById(tmplId)
            ?: throw NoSuchElementException("Template not found: $tmplId")
        return render(template, variables)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Internal helpers
    // ──────────────────────────────────────────────────────────────────────

    private fun requireTemplate(code: String): Template =
        TemplateDao.findByCode(code)
            ?: throw NoSuchElementException("Required template '$code' not found in database")

    private fun render(template: Template, variables: Map<String, Any?>): RenderedMessage {
        val renderedBody    = TemplateEngine.render(template.body, variables)
        val renderedSubject = template.subject?.let { TemplateEngine.render(it, variables) }

        return RenderedMessage(
            channel = template.channel.name,
            subject = renderedSubject,
            content = renderedBody
        )
    }
}
