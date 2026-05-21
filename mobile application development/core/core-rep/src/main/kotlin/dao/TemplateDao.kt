package com.vt.dao

import com.vt.model.Channel
import com.vt.model.Template
import com.vt.model.TemplateResponse
import com.vt.table.TemplateTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.UUID

object TemplateDao {

    fun findById(tmplId: UUID): Template? = transaction {
        TemplateTable.selectAll()
            .where { TemplateTable.tmplId eq tmplId }
            .singleOrNull()
            ?.toTemplate()
    }

    fun findByCode(code: String): Template? = transaction {
        TemplateTable.selectAll()
            .where { TemplateTable.code eq code }
            .singleOrNull()
            ?.toTemplate()
    }

    fun listAll(): List<TemplateResponse> = transaction {
        TemplateTable.selectAll()
            .orderBy(TemplateTable.code)
            .map { it.toResponse() }
    }

    fun create(
        code: String,
        name: String,
        channel: Channel,
        subject: String?,
        body: String
    ): Template = transaction {
        val id = UUID.randomUUID()
        TemplateTable.insert {
            it[tmplId]    = id
            it[TemplateTable.code]    = code
            it[TemplateTable.name]    = name
            it[TemplateTable.channel] = channel.name
            it[TemplateTable.subject] = subject
            it[TemplateTable.body]    = body
        }
        findById(id) ?: error("Failed to load created template")
    }

    fun update(
        tmplId: UUID,
        name: String?,
        subject: String?,
        body: String?
    ): Template = transaction {
        TemplateTable.update({ TemplateTable.tmplId eq tmplId }) { row ->
            name?.let    { row[TemplateTable.name]    = it }
            subject?.let { row[TemplateTable.subject] = it }
            body?.let    { row[TemplateTable.body]    = it }
            row[updatedAt] = Instant.now()
        }
        findById(tmplId) ?: throw NoSuchElementException("Template not found: $tmplId")
    }

    // ─── Mapping ─────────────────────────────────────────────────────────

    private fun ResultRow.toTemplate() = Template(
        tmplId    = this[TemplateTable.tmplId],
        code      = this[TemplateTable.code],
        name      = this[TemplateTable.name],
        channel   = Channel.valueOf(this[TemplateTable.channel]),
        subject   = this[TemplateTable.subject],
        body      = this[TemplateTable.body],
        createdAt = this[TemplateTable.createdAt],
        updatedAt = this[TemplateTable.updatedAt]
    )

    private fun ResultRow.toResponse() = TemplateResponse(
        tmplId    = this[TemplateTable.tmplId],
        code      = this[TemplateTable.code],
        name      = this[TemplateTable.name],
        channel   = this[TemplateTable.channel],
        subject   = this[TemplateTable.subject],
        createdAt = this[TemplateTable.createdAt],
        updatedAt = this[TemplateTable.updatedAt]
    )
}
