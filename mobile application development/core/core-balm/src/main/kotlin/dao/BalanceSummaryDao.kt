package com.vt.dao

import com.vt.table.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.Instant

object BalanceSummaryDao {

    data class SummaryResponse(
        val currentBalance: BigDecimal,
        val balanceBeforePeriod: BigDecimal,
        val summaryByBillDetail: List<BillDetailSummary>
    )

    data class BillDetailSummary(
        val billDetailId: Int,
        val billDetailCode: String,
        val billDetailSum: BigDecimal
    )

    fun getSummary(accId: Int, startDate: Instant, endDate: Instant): SummaryResponse = transaction {
        // 1. Текущий баланс
        val currentBalance = BalanceCacheTable
            .select { BalanceCacheTable.accId eq accId }
            .singleOrNull()?.get(BalanceCacheTable.balance) ?: BigDecimal.ZERO

        // 2. Выбираем все начисления за период вместе с типом и биллинг-деталью
        val rawRows = (ChargeTable
                innerJoin ChargeTypesTable)
            .leftJoin(BillDetailsTable, { ChargeTable.bdetId }, { BillDetailsTable.bdetId })
            .select {
                (ChargeTable.accId eq accId) and
                        (ChargeTable.timestamp greaterEq startDate) and
                        (ChargeTable.timestamp less endDate)
            }
            .mapNotNull { row ->
                val bdetRefsId = row[BillDetailsTable.bdetRefsId]
                val bdetCode = row[BillDetailsTable.code]
                if (bdetRefsId == null || bdetCode == null) null
                else Triple(
                    row[ChargeTable.amount],
                    row[ChargeTypesTable.code],
                    bdetRefsId to bdetCode
                )
            }

        // 3. Считаем общее изменение за период для баланса до периода
        val totalChange = rawRows.fold(BigDecimal.ZERO) { acc, (amount, code, _) ->
            when (code) {
                "debit", "freeze" -> acc - amount
                else -> acc + amount   // credit, unfreeze
            }
        }

        val balanceBeforePeriod = currentBalance - totalChange

        // 4. Группируем по биллинг-деталям и суммируем с учётом знака
        val billDetailMap = rawRows
            .groupBy { it.third }  // ключ – пара (bdetRefsId, code)
            .map { (key, rows) ->
                val sum = rows.fold(BigDecimal.ZERO) { acc, (amount, code, _) ->
                    when (code) {
                        "debit", "freeze" -> acc - amount
                        else -> acc + amount
                    }
                }
                BillDetailSummary(
                    billDetailId = key.first,
                    billDetailCode = key.second,
                    billDetailSum = sum
                )
            }

        SummaryResponse(
            currentBalance = currentBalance,
            balanceBeforePeriod = balanceBeforePeriod,
            summaryByBillDetail = billDetailMap
        )
    }
}