package com.vt.dao

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import com.vt.model.*
import com.vt.validation.SchemaValidator

private val mapper = jacksonObjectMapper()

object LanguageDao {
    fun findAll(): List<Language> = transaction {
        LanguageTable.selectAll().map {
            Language(
                langId = it[LanguageTable.langId],
                code = it[LanguageTable.code],
                name = it[LanguageTable.name],
                isDefault = it[LanguageTable.isDefault]
            )
        }
    }

    fun findByCode(code: String): Language? = transaction {
        LanguageTable.selectAll().where { LanguageTable.code eq code }
            .map {
                Language(
                    it[LanguageTable.langId],
                    it[LanguageTable.code],
                    it[LanguageTable.name],
                    it[LanguageTable.isDefault]
                )
            }.singleOrNull()
    }

    fun findDefault(): Language? = transaction {
        LanguageTable.selectAll().where { LanguageTable.isDefault eq true }
            .map {
                Language(
                    it[LanguageTable.langId],
                    it[LanguageTable.code],
                    it[LanguageTable.name],
                    it[LanguageTable.isDefault]
                )
            }.singleOrNull()
    }

    fun existsByCode(code: String): Boolean = transaction {
        LanguageTable.selectAll().where { LanguageTable.code eq code }.any()
    }
}

object EntityListSchemaDao {
    fun findByCode(code: String): EntityListSchema? = transaction {
        EntityListSchemaTable.selectAll().where { EntityListSchemaTable.code eq code }
            .map {
                EntityListSchema(
                    it[EntityListSchemaTable.eschemaId],
                    it[EntityListSchemaTable.code],
                    it[EntityListSchemaTable.schema].toString()
                )
            }.singleOrNull()
    }

    fun existsByCode(code: String): Boolean = transaction {
        EntityListSchemaTable.selectAll().where { EntityListSchemaTable.code eq code }.any()
    }

    fun create(code: String, schema: String): EntityListSchema {
        require(SchemaValidator.validateSchema(schema)) { "Invalid schema structure" }

        val json: JsonNode = mapper.readTree(schema)

        val id = transaction {
            EntityListSchemaTable.insert {
                it[EntityListSchemaTable.code] = code
                it[EntityListSchemaTable.schema] = json
            } get EntityListSchemaTable.eschemaId
        }

        return EntityListSchema(id, code, schema)
    }

    fun update(code: String, newSchema: String): EntityListSchema {
        require(SchemaValidator.validateSchema(newSchema)) { "Invalid schema structure" }

        val hasLinkedData = transaction {
            EntityListTable.selectAll().where { EntityListTable.schemaCode eq code }.any()
        }
        if (hasLinkedData) throw IllegalStateException("Cannot update schema with existing data references")

        val json: JsonNode = mapper.readTree(newSchema)

        transaction {
            EntityListSchemaTable.update({ EntityListSchemaTable.code eq code }) {
                it[EntityListSchemaTable.schema] = json
            }
        }

        return findByCode(code) ?: throw NoSuchElementException("Schema not found after update")
    }

    fun deleteByCode(code: String): Boolean {
        val hasLinkedData = transaction {
            EntityListTable.selectAll().where { EntityListTable.schemaCode eq code }.any()
        }
        if (hasLinkedData) throw IllegalStateException("Cannot delete schema with existing data references")

        return transaction {
            EntityListSchemaTable.deleteWhere { EntityListSchemaTable.code eq code } > 0
        }
    }
}

object EntityListDao {
    fun findBySchemaAndLang(schemaCode: String, langId: Int): List<EntityList> = transaction {
        EntityListTable.selectAll()
            .where { (EntityListTable.schemaCode eq schemaCode) and (EntityListTable.langId eq langId) }
            .map { mapToEntityList(it) }
    }

    fun findBySchemaLangAndId(schemaCode: String, entityListId: Int, langId: Int): EntityList? = transaction {
        EntityListTable.selectAll().where {
            (EntityListTable.schemaCode eq schemaCode) and
                    (EntityListTable.entityListId eq entityListId) and
                    (EntityListTable.langId eq langId)
        }.map { mapToEntityList(it) }.singleOrNull()
    }

    fun exists(schemaCode: String, entityListId: Int, langId: Int): Boolean = transaction {
        EntityListTable.selectAll().where {
            (EntityListTable.schemaCode eq schemaCode) and
                    (EntityListTable.entityListId eq entityListId) and
                    (EntityListTable.langId eq langId)
        }.any()
    }

    fun create(schemaCode: String, entityListId: Int?, langId: Int, data: String): EntityList {
        val finalId = entityListId ?: nextEntityListId(schemaCode, langId)

        if (exists(schemaCode, finalId, langId)) throw IllegalStateException("Entity already exists")

        val schema = EntityListSchemaDao.findByCode(schemaCode)
            ?: throw NoSuchElementException("Schema not found")

        SchemaValidator.validateDataAgainstSchema(data, schema.schema)

        val json = mapper.readTree(data)

        val id = transaction {
            EntityListTable.insert {
                it[EntityListTable.entityListId] = finalId
                it[EntityListTable.schemaCode] = schemaCode
                it[EntityListTable.langId] = langId
                it[EntityListTable.data] = json
            } get EntityListTable.entlId
        }

        return EntityList(id, finalId, schemaCode, langId, data)
    }

    fun update(schemaCode: String, entityListId: Int, langId: Int, newData: String): EntityList {
        val existing = findBySchemaLangAndId(schemaCode, entityListId, langId)
            ?: throw NoSuchElementException("Entity not found")

        val schema = EntityListSchemaDao.findByCode(schemaCode)
            ?: throw NoSuchElementException("Schema not found")

        SchemaValidator.validateDataAgainstSchema(newData, schema.schema)

        val json = mapper.readTree(newData)

        transaction {
            EntityListTable.update({
                (EntityListTable.schemaCode eq schemaCode) and
                        (EntityListTable.entityListId eq entityListId) and
                        (EntityListTable.langId eq langId)
            }) {
                it[EntityListTable.data] = json
            }
        }

        return existing.copy(data = newData)
    }

    fun delete(schemaCode: String, entityListId: Int, langId: Int): Boolean = transaction {
        EntityListTable.deleteWhere {
            (EntityListTable.schemaCode eq schemaCode) and
                    (EntityListTable.entityListId eq entityListId) and
                    (EntityListTable.langId eq langId)
        } > 0
    }

    private fun nextEntityListId(schemaCode: String, langId: Int): Int {
        return transaction {
            (EntityListTable.select(EntityListTable.entityListId.max())
                .where { (EntityListTable.schemaCode eq schemaCode) and (EntityListTable.langId eq langId) }
                .firstOrNull()?.get(EntityListTable.entityListId.max()) ?: 0) + 1
        }
    }

    private fun mapToEntityList(row: ResultRow): EntityList = EntityList(
        entlId = row[EntityListTable.entlId],
        entityListId = row[EntityListTable.entityListId],
        schemaCode = row[EntityListTable.schemaCode],
        langId = row[EntityListTable.langId],
        data = row[EntityListTable.data].toString()
    )
}

object EntitySingleDao {
    fun findByCodeAndLang(code: String, langId: Int): EntitySingle? = transaction {
        EntitySingleTable.selectAll()
            .where { (EntitySingleTable.code eq code) and (EntitySingleTable.langId eq langId) }
            .map {
                EntitySingle(
                    it[EntitySingleTable.entsId],
                    it[EntitySingleTable.code],
                    it[EntitySingleTable.langId],
                    it[EntitySingleTable.data].toString()
                )
            }.singleOrNull()
    }

    fun exists(code: String, langId: Int): Boolean = transaction {
        EntitySingleTable.selectAll()
            .where { (EntitySingleTable.code eq code) and (EntitySingleTable.langId eq langId) }
            .any()
    }

    fun create(code: String, langId: Int, data: String): EntitySingle {
        if (exists(code, langId)) throw IllegalStateException("Entity single already exists")

        val json = mapper.readTree(data)

        val id = transaction {
            EntitySingleTable.insert {
                it[EntitySingleTable.code] = code
                it[EntitySingleTable.langId] = langId
                it[EntitySingleTable.data] = json
            } get EntitySingleTable.entsId
        }

        return EntitySingle(id, code, langId, data)
    }

    fun update(code: String, langId: Int, newData: String): EntitySingle {
        val existing = findByCodeAndLang(code, langId)
            ?: throw NoSuchElementException("Entity single not found")

        val json = mapper.readTree(newData)

        transaction {
            EntitySingleTable.update({
                (EntitySingleTable.code eq code) and (EntitySingleTable.langId eq langId)
            }) {
                it[EntitySingleTable.data] = json
            }
        }

        return existing.copy(data = newData)
    }

    fun delete(code: String, langId: Int): Boolean = transaction {
        EntitySingleTable.deleteWhere {
            (EntitySingleTable.code eq code) and (EntitySingleTable.langId eq langId)
        } > 0
    }
}