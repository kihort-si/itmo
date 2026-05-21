package com.vt.dao

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.jsonb
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper


private val mapper = jacksonObjectMapper()

object LanguageTable : Table("refs.language") {
    val langId = integer("lang_id").autoIncrement()
    val code = varchar("code", 255).uniqueIndex()
    val name = varchar("name", 255).uniqueIndex()
    val isDefault = bool("is_default").default(false)

    override val primaryKey = PrimaryKey(langId)
}

object EntityListSchemaTable : Table("refs.entity_list_schema") {
    val eschemaId = integer("eschema_id").autoIncrement()
    val code = varchar("code", 255).uniqueIndex()
    val schema = jsonb<JsonNode>("schema",
        serialize = { mapper.writeValueAsString(it) },
        deserialize = { mapper.readTree(it) }
    )

    override val primaryKey = PrimaryKey(eschemaId)
}

object EntityListTable : Table("refs.entity_list") {
    val entlId = integer("entl_id").autoIncrement()
    val entityListId = integer("entity_list_id")
    val schemaCode = varchar("schema_code", 255).references(EntityListSchemaTable.code, onDelete = ReferenceOption.CASCADE)
    val langId = integer("lang_id").references(LanguageTable.langId, onDelete = ReferenceOption.CASCADE)
    val data = jsonb<JsonNode>("data",
        serialize = { mapper.writeValueAsString(it) },
        deserialize = { mapper.readTree(it) }
    )

    override val primaryKey = PrimaryKey(entlId)

    init {
        uniqueIndex(entityListId, schemaCode, langId)
    }
}

object EntitySingleTable : Table("refs.entity_single") {
    val entsId = integer("ents_id").autoIncrement()
    val code = varchar("code", 255)
    val langId = integer("lang_id").references(LanguageTable.langId, onDelete = ReferenceOption.CASCADE)
    val data = jsonb<JsonNode>("data",
        serialize = { mapper.writeValueAsString(it) },
        deserialize = { mapper.readTree(it) }
    )

    override val primaryKey = PrimaryKey(entsId)

    init {
        uniqueIndex(code, langId)
    }
}