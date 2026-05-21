package com.vt.service

import com.vt.dao.ClientAttributeDao
import com.vt.dao.AccountDao
import com.vt.table.FeesTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

interface BalmContext {
    fun clientAttributes(clientId: Int): List<Map<String, Any>>
    fun fee(code: String, regionId: Int): BigDecimal?
    fun accountBalance(accId: Int): BigDecimal
    fun hasActiveAttribute(clientId: Int, attributeRefsId: Int): Boolean
}

class BalmContextImpl : BalmContext {
    override fun clientAttributes(clientId: Int): List<Map<String, Any>> {
        return ClientAttributeDao.findByClntId(clientId).map {
            mapOf<String, Any>(
                "attributeId" to it.attributeId,
                "value" to it.value,
                "startDate" to it.startDate.toString(),
                "endDate" to (it.endDate?.toString() ?: "")
            )
        }
    }

    override fun fee(code: String, regionId: Int): BigDecimal? {
        return transaction {
            FeesTable.selectAll()
                .where { (FeesTable.code eq code) and (FeesTable.regionId eq regionId) }
                .singleOrNull()
                ?.get(FeesTable.amount)
        }
    }

    override fun accountBalance(accId: Int): BigDecimal {
        return AccountDao.getBalance(accId)
    }

    override fun hasActiveAttribute(clientId: Int, attributeRefsId: Int): Boolean {
        return ClientAttributeDao.hasActiveAttribute(clientId, attributeRefsId)
    }
}