package com.vt.client

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.vt.consumer.ReportingGateway
import com.vt.model.RenderedMessage
import com.vt.model.TemplateInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.jackson.jackson
import org.slf4j.LoggerFactory
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * HTTP client for the Reporting / Template Service (REP).
 *
 * Template list is cached in memory after the first successful fetch.
 * The cache maps template code (e.g. "REGISTRATION_SUCCESS") → UUID.
 */
class ReportingClient(private val repUrl: String) : ReportingGateway {

    private val logger = LoggerFactory.getLogger(ReportingClient::class.java)

    private val http = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson {
                disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                registerModule(JavaTimeModule())
                registerModule(kotlinModule())
            }
        }
    }

    /** code → UUID cache; populated lazily on first call to [findTemplateId]. */
    private val codeToId = ConcurrentHashMap<String, UUID>()

    // ──────────────────────────────────────────────────────────────────────
    // Public API
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Find the UUID of a template by its [code].
     * Returns null if the template does not exist in REP.
     * Loads the full template list on the first call (or on cache miss).
     */
    override suspend fun findTemplateId(code: String): UUID? {
        codeToId[code]?.let { return it }
        refreshCache()
        return codeToId[code]
    }

    /**
     * Render template [tmplId] with the provided [variables].
     * Returns the rendered message (channel, subject, content).
     */
    override suspend fun render(tmplId: UUID, variables: Map<String, Any?>): RenderedMessage {
        return http.post("$repUrl/v1/templates/$tmplId/render") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("variables" to variables))
        }.body()
    }

    override fun close() = http.close()

    // ──────────────────────────────────────────────────────────────────────
    // Internal
    // ──────────────────────────────────────────────────────────────────────

    private suspend fun refreshCache() {
        try {
            val templates = http.get("$repUrl/v1/templates").body<List<TemplateInfo>>()
            templates.forEach { tmpl ->
                codeToId[tmpl.code] = UUID.fromString(tmpl.tmplId)
            }
            logger.info("Template cache refreshed: {} entries", codeToId.size)
        } catch (ex: Exception) {
            logger.error("Failed to fetch template list from REP at {}", repUrl, ex)
        }
    }
}
