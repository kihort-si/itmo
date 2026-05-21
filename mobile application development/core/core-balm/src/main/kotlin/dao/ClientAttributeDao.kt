package com.vt.dao

import com.vt.table.ClientAttributeTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object ClientAttributeDao {
    data class ClientAttribute(
        val clntAttrId: Int,
        val clntId: Int,
        val attributeId: Int,
        val value: String,
        val startDate: java.time.Instant,
        val endDate: java.time.Instant?
    )

    fun findByClntId(clntId: Int): List<ClientAttribute> = transaction {
        ClientAttributeTable.selectAll()
            .where { ClientAttributeTable.clntId eq clntId }
            .map { row ->
                ClientAttribute(
                    clntAttrId = row[ClientAttributeTable.clntAttrId],
                    clntId = row[ClientAttributeTable.clntId],
                    attributeId = row[ClientAttributeTable.attributeRefsId],
                    value = row[ClientAttributeTable.value],
                    startDate = row[ClientAttributeTable.startDate],
                    endDate = row[ClientAttributeTable.endDate]
                )
            }
    }

    fun hasActiveAttribute(clntId: Int, attributeRefsId: Int): Boolean = transaction {
        val now = java.time.Instant.now()
        ClientAttributeTable.selectAll()
            .where {
                (ClientAttributeTable.clntId eq clntId) and
                        (ClientAttributeTable.attributeRefsId eq attributeRefsId) and
                        (ClientAttributeTable.startDate lessEq now) and
                        ((ClientAttributeTable.endDate.isNull()) or (ClientAttributeTable.endDate greaterEq now))
            }
            .limit(1)
            .count() > 0
    }
}