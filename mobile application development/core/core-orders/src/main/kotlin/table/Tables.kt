package com.vt.table

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.javatime.timestamp

private val mapper = jacksonObjectMapper()

object OrderStatusTable : Table("orders.order_status") {
    val ostId = integer("ost_id").autoIncrement()
    val code = varchar("code", 255).uniqueIndex()

    override val primaryKey = PrimaryKey(ostId)
}

object OrderTable : Table("orders.order") {
    val ordId = integer("ord_id").autoIncrement()
    val clntId = integer("clnt_id")
    val orderType = varchar("order_type", 512)
    val ostId = reference("ost_id", OrderStatusTable.ostId)
    val dateStart = timestamp("date_start").clientDefault { java.time.Instant.now() }
    val dateEnd = timestamp("date_end").nullable()
    val orderData = jsonb<JsonNode>(
        "order_data",
        serialize = { mapper.writeValueAsString(it) },
        deserialize = { mapper.readTree(it) }
    )

    override val primaryKey = PrimaryKey(ordId)
}