// ============================================================
// File: com/vt/table/Tables.kt
// ============================================================
package com.vt.table

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.timestamp

object PortfolioTable : Table("depository.portfolio") {
    val portId = integer("port_id").autoIncrement()
    val clntId = integer("clnt_id")
    val name = varchar("name", 512)
    val createdAt = timestamp("created_at").clientDefault { java.time.Instant.now() }
    val isClosed = bool("is_closed").default(false)

    override val primaryKey = PrimaryKey(portId)
}

object PortfolioPositionTable : Table("depository.portfolio_position") {
    val portPosId = integer("port_pos_id").autoIncrement()
    val portId = reference("port_id", PortfolioTable.portId, onDelete = ReferenceOption.CASCADE)
    val ticker = varchar("ticker", 512)
    val amount = integer("amount").default(0)
    val amountFrozen = integer("amount_frozen").default(0)

    override val primaryKey = PrimaryKey(portPosId)

    init {
        uniqueIndex(portId, ticker)
    }
}

object OperationsTypeTable : Table("depository.operations_type") {
    val portOperTypeId = integer("port_oper_type_id").autoIncrement()
    val code = varchar("code", 512).uniqueIndex()

    override val primaryKey = PrimaryKey(portOperTypeId)
}

object PortfolioOperationsHistoryTable : Table("depository.portfolio_operations_history") {
    val portOperId = integer("port_oper_id").autoIncrement()
    val portId = reference("port_id", PortfolioTable.portId, onDelete = ReferenceOption.CASCADE)
    val ticker = varchar("ticker", 512)
    val portOperTypeId = reference("port_oper_type_id", OperationsTypeTable.portOperTypeId)
    val amount = integer("amount")
    val createdAt = timestamp("created_at").clientDefault { java.time.Instant.now() }

    override val primaryKey = PrimaryKey(portOperId)
}