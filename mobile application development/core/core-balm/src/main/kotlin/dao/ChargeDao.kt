package com.vt.dao

import com.vt.model.*
import com.vt.table.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

object ChargeDao {

    fun createCharge(accId: Int, request: CreateChargeRequest): ChargeResponse = transaction {
        // Проверка, что счёт существует и не закрыт
        val status = AccountTable
            .innerJoin(AccountStateTable, { AccountTable.accstId }, { AccountStateTable.accstId })
            .select { AccountTable.accId eq accId }
            .singleOrNull()?.get(AccountStateTable.name)
            ?: throw NoSuchElementException("Account not found")

        if (status == "closed") {
            throw IllegalStateException("Account is closed")
        }

        // Определяем тип операции
        val chargeType = ChargeTypesTable
            .select { ChargeTypesTable.code eq request.chargeType }
            .singleOrNull()
            ?: error("Charge type not found")

        val bdetId = request.billDetail ?: chargeType[ChargeTypesTable.defaultBdetId]

        // Вызов хранимой функции с обработкой ошибки недостатка средств
        val (chtgId, timestamp) = try {
            TransactionManager.current().exec(
                """
                SELECT * FROM balm.process_charge(
                    $accId,
                    ${chargeType[ChargeTypesTable.chtpId]},
                    ${request.amount},
                    $bdetId
                )
                """
            ) { rs ->
                if (!rs.next()) error("No rows")
                Pair(
                    rs.getLong("chtg_id"),
                    rs.getTimestamp("timestamp").toInstant()
                )
            }!!
        } catch (e: ExposedSQLException) {
            if (e.message?.contains("Insufficient balance") == true) {
                throw IllegalStateException("Insufficient balance")
            } else {
                throw e
            }
        }

        // Получаем обновлённый баланс
        val newBalance = BalanceCacheTable
            .select { BalanceCacheTable.accId eq accId }
            .single()[BalanceCacheTable.balance]

        ChargeResponse(
            chargeId = chtgId,
            accId = accId,
            chargeType = request.chargeType,
            amount = request.amount,
            billDetailId = bdetId,
            timestamp = timestamp,
            newBalance = newBalance
        )
    }

    fun recalculateBalance(accId: Int): BigDecimal = transaction {
        val result = TransactionManager.current().exec(
            "SELECT balm.recalculate_balance($accId)"
        ) { rs ->
            if (rs.next()) rs.getBigDecimal(1) else null
        }

        result ?: BigDecimal.ZERO
    }
}