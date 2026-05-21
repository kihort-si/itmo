package com.vt.dao

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant as JavaInstant

private val mapper = jacksonObjectMapper()

object ClientStatusTable : Table("clients.client_status") {
    val clstId = integer("clst_id").autoIncrement()
    val def = varchar("def", 50).uniqueIndex()

    override val primaryKey = PrimaryKey(clstId)
}

object ClientTable : Table("clients.client") {
    val clntId = integer("clnt_id").autoIncrement()
    val username = varchar("username", 255).uniqueIndex()
    val regionRefsIdentifier = integer("region_refs_identifier")
    val createdAt = timestamp("created_at").clientDefault { JavaInstant.now() }
    val languageCode = varchar("language_code", 10)
    val statusId = reference("status_id", ClientStatusTable.clstId)  // <-- новая колонка

    override val primaryKey = PrimaryKey(clntId)
}

object ClientDetailsTable : Table("clients.client_details") {
    val clntDetailsId = integer("clnt_details_id").autoIncrement()
    val clntId = reference("clnt_id", ClientTable.clntId, onDelete = ReferenceOption.CASCADE)
    val fullName = varchar("full_name", 512)
    val email = varchar("email", 255).uniqueIndex() // <-- UNIQUE
    val phoneNumber = varchar("phone_number", 20).nullable()
    val additionalInfo = text("additional_info").nullable()
    val profileExtension = jsonb<JsonNode>("profile_extension",
        serialize = { mapper.writeValueAsString(it) },
        deserialize = { mapper.readTree(it) }
    ).nullable()

    override val primaryKey = PrimaryKey(clntDetailsId)
}

object ClientAccountTable : Table("clients.client_account") {
    val clntAccId = integer("clnt_acc_id").autoIncrement()
    val clntId = reference("clnt_id", ClientTable.clntId, onDelete = ReferenceOption.CASCADE)
    val accountId = integer("account_id").uniqueIndex()

    override val primaryKey = PrimaryKey(clntAccId)
}

object ClientAttributeTable : Table("clients.client_attribute") {
    val clntAttrId = integer("clnt_attr_id").autoIncrement()
    val clntId = reference("clnt_id", ClientTable.clntId, onDelete = ReferenceOption.CASCADE)
    val attributeRefsId = integer("attribute_refs_id")
    val startDate = timestamp("start_date").clientDefault { JavaInstant.now() }
    val endDate = timestamp("end_date").nullable()
    val value = varchar("value", 512)

    override val primaryKey = PrimaryKey(clntAttrId)

    init {
        uniqueIndex(clntId, attributeRefsId)
    }
}

