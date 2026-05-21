package com.vt.dao

import com.vt.model.*
import com.vt.table.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

object AccountDao {

    fun create(request: CreateAccountRequest): AccountResponse = transaction {
        val currencyId = request.currId ?: getRubCurrencyId()
        val activeStateId = getActiveStateId()

        val accId = AccountTable.insert {
            it[clntId] = request.clntId
            it[currId] = currencyId
            it[accstId] = activeStateId
        }[AccountTable.accId]

        BalanceCacheTable.insert {
            it[BalanceCacheTable.accId] = accId
            it[balance] = BigDecimal.ZERO
        }

        getById(accId) ?: throw IllegalStateException("Failed to retrieve created account")
    }

    fun getById(accId: Int): AccountResponse? = transaction {
        AccountTable
            .innerJoin(CurrencyCodeTable, { AccountTable.currId }, { CurrencyCodeTable.currId })
            .innerJoin(AccountStateTable, { AccountTable.accstId }, { AccountStateTable.accstId })
            .leftJoin(BalanceCacheTable, { AccountTable.accId }, { BalanceCacheTable.accId })
            .select { AccountTable.accId eq accId }
            .singleOrNull()
            ?.let { row ->
                AccountResponse(
                    accId = row[AccountTable.accId],
                    clntId = row[AccountTable.clntId],
                    currency = CurrencyInfo(
                        currId = row[CurrencyCodeTable.currId],
                        code = row[CurrencyCodeTable.code],
                        refsId = row[CurrencyCodeTable.refsId]
                    ),
                    status = row[AccountStateTable.name],
                    balance = row[BalanceCacheTable.balance] ?: BigDecimal.ZERO
                )
            }
    }

    fun getByClientId(clntId: Int): List<AccountResponse> = transaction {
        AccountTable
            .innerJoin(CurrencyCodeTable, { AccountTable.currId }, { CurrencyCodeTable.currId })
            .innerJoin(AccountStateTable, { AccountTable.accstId }, { AccountStateTable.accstId })
            .leftJoin(BalanceCacheTable, { AccountTable.accId }, { BalanceCacheTable.accId })
            .select { AccountTable.clntId eq clntId }
            .map { row ->
                AccountResponse(
                    accId = row[AccountTable.accId],
                    clntId = row[AccountTable.clntId],
                    currency = CurrencyInfo(
                        currId = row[CurrencyCodeTable.currId],
                        code = row[CurrencyCodeTable.code],
                        refsId = row[CurrencyCodeTable.refsId]
                    ),
                    status = row[AccountStateTable.name],
                    balance = row[BalanceCacheTable.balance] ?: BigDecimal.ZERO
                )
            }
    }

    fun closeAccount(accId: Int): AccountResponse = transaction {
        val account = getById(accId) ?: throw NoSuchElementException("Account not found")
        if (account.status == "closed") {
            throw IllegalStateException("Account is already closed")
        }

        // Проверка баланса (должен быть нулевым)
        val balance = BalanceCacheTable.select { BalanceCacheTable.accId eq accId }
            .singleOrNull()?.get(BalanceCacheTable.balance) ?: BigDecimal.ZERO
        if (balance.compareTo(BigDecimal.ZERO) != 0) {
            throw IllegalStateException("nonzerobalance")
        }

        // Проверка незавершённых заявок
//        val unfinished = PurchaseOrderTable
//            .innerJoin(PurchaseOrderStatusTable)
//            .select {
//                (PurchaseOrderTable.accId eq accId) and
//                        (PurchaseOrderStatusTable.code neq "finished")
//            }
//            .count()
//        if (unfinished > 0) {
//            throw IllegalStateException("unfinishedorders")
//        }
        // Управление заявками планируется перенести в другой сервис

        val closedStateId = AccountStateTable
            .select { AccountStateTable.name eq "closed" }
            .singleOrNull()?.get(AccountStateTable.accstId)
            ?: throw IllegalStateException("Closed state not found")

        AccountTable.update({ AccountTable.accId eq accId }) {
            it[accstId] = closedStateId
        }

        getById(accId) ?: throw IllegalStateException("Failed to retrieve closed account")
    }

    private fun getRubCurrencyId(): Int = transaction {
        CurrencyCodeTable.select { CurrencyCodeTable.code eq "RUB" }
            .singleOrNull()?.get(CurrencyCodeTable.currId)
            ?: throw IllegalStateException("RUB currency not found")
    }

    private fun getActiveStateId(): Int = transaction {
        AccountStateTable.select { AccountStateTable.name eq "active" }
            .singleOrNull()?.get(AccountStateTable.accstId)
            ?: throw IllegalStateException("Active state not found")
    }

    fun getBalance(accId: Int): BigDecimal = transaction {
        BalanceCacheTable.select { BalanceCacheTable.accId eq accId }
            .singleOrNull()?.get(BalanceCacheTable.balance) ?: BigDecimal.ZERO
    }


}