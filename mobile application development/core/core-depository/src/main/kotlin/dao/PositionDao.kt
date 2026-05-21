// ============================================================
// File: com/vt/dao/PositionDao.kt (операции)
// ============================================================
package com.vt.dao

import com.vt.model.*
import com.vt.table.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

object PositionDao {

    fun executeOperation(portId: Int, request: OperationRequest): OperationResponse = transaction {
        val portfolioRow = PortfolioTable.select { PortfolioTable.portId eq portId }.singleOrNull()
            ?: throw NoSuchElementException("Portfolio not found")
        if (portfolioRow[PortfolioTable.isClosed]) {
            throw IllegalStateException("Cannot perform operations on a closed portfolio")
        }
        // 1. Получить код типа операции
        val opType = OperationsTypeTable.select { OperationsTypeTable.code eq request.operationType }
            .singleOrNull() ?: throw IllegalArgumentException("Invalid operation type: ${request.operationType}")

        // 2. Найти позицию или создать для BUY
        val existingPosition = PortfolioPositionTable.select {
            (PortfolioPositionTable.portId eq portId) and (PortfolioPositionTable.ticker eq request.ticker)
        }.singleOrNull()

        val (currentAmount, currentFrozen) = when {
            existingPosition != null -> {
                existingPosition[PortfolioPositionTable.amount] to existingPosition[PortfolioPositionTable.amountFrozen]
            }
            request.operationType == "BUY" -> {
                // Создаём новую позицию
                PortfolioPositionTable.insert {
                    it[PortfolioPositionTable.portId] = portId
                    it[ticker] = request.ticker
                    it[amount] = 0
                    it[amountFrozen] = 0
                }
                0 to 0
            }
            else -> throw NoSuchElementException("Position not found for ticker ${request.ticker}")
        }

        // 3. Проверить достаточность и вычислить новые значения
        val newAmount: Int
        val newFrozen: Int
        when (request.operationType) {
            "BUY" -> {
                newAmount = currentAmount + request.amount
                newFrozen = currentFrozen
            }
            "SELL" -> {
                if (currentAmount < request.amount) throw IllegalStateException("Insufficient amount to sell")
                newAmount = currentAmount - request.amount
                newFrozen = currentFrozen
            }
            "FREEZE" -> {
                if (currentAmount < request.amount) throw IllegalStateException("Insufficient amount to freeze")
                newAmount = currentAmount - request.amount
                newFrozen = currentFrozen + request.amount
            }
            "UNFREEZE" -> {
                if (currentFrozen < request.amount) throw IllegalStateException("Insufficient frozen amount to unfreeze")
                newAmount = currentAmount + request.amount
                newFrozen = currentFrozen - request.amount
            }
            else -> throw IllegalArgumentException("Unsupported operation type")
        }

        // 4. Обновить позицию
        PortfolioPositionTable.update({
            (PortfolioPositionTable.portId eq portId) and (PortfolioPositionTable.ticker eq request.ticker)
        }) {
            it[amount] = newAmount
            it[amountFrozen] = newFrozen
        }

        // 5. Записать в историю
        val now = Instant.now()
        val historyId = PortfolioOperationsHistoryTable.insert {
            it[PortfolioOperationsHistoryTable.portId] = portId
            it[ticker] = request.ticker
            it[portOperTypeId] = opType[OperationsTypeTable.portOperTypeId]
            it[amount] = request.amount
            it[createdAt] = now
        }[PortfolioOperationsHistoryTable.portOperId]

        // 6. Вернуть ответ
        OperationResponse(
            portId = portId,
            ticker = request.ticker,
            operationType = request.operationType,
            amount = request.amount,
            positionAfter = PositionResponse(
                ticker = request.ticker,
                amount = newAmount,
                amountFrozen = newFrozen
            ),
            operationId = historyId,
            createdAt = now
        )
    }
}