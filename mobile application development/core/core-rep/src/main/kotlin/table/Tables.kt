package com.vt.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import java.util.UUID

object TemplateTable : Table("rep.template") {
    val tmplId    = uuid("tmpl_id").clientDefault { UUID.randomUUID() }
    val code      = varchar("code", 128).uniqueIndex()
    val name      = varchar("name", 512)
    val channel   = varchar("channel", 16)
    val subject   = varchar("subject", 512).nullable()
    val body      = text("body")
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Instant.now() }

    override val primaryKey = PrimaryKey(tmplId)
}
