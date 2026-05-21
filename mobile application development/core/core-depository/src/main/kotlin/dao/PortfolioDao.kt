package com.vt.dao

import com.vt.model.*
import com.vt.table.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

object PortfolioDao {

    fun create(request: CreatePortfolioRequest): PortfolioResponse = transaction {
        val portId = PortfolioTable.insert {
            it[clntId] = request.clntId
            it[name] = request.name
        }[PortfolioTable.portId]

        getById(portId) ?: throw IllegalStateException("Failed to retrieve created portfolio")
    }

    fun updateName(portId: Int, newName: String): PortfolioResponse = transaction {
        val portfolio = PortfolioTable.select { PortfolioTable.portId eq portId }.singleOrNull()
            ?: throw NoSuchElementException("Portfolio not found")
        if (portfolio[PortfolioTable.isClosed]) {
            throw IllegalStateException("Cannot rename a closed portfolio")
        }

        PortfolioTable.update({ PortfolioTable.portId eq portId }) {
            it[name] = newName
        }

        getById(portId) ?: throw IllegalStateException("Failed to retrieve updated portfolio")
    }

    fun closePortfolio(portId: Int): PortfolioResponse = transaction {
        val portfolioRow = PortfolioTable.select { PortfolioTable.portId eq portId }.singleOrNull()
            ?: throw NoSuchElementException("Portfolio not found")
        if (portfolioRow[PortfolioTable.isClosed]) {
            throw IllegalStateException("Portfolio is already closed")
        }

        // Проверить, что все позиции нулевые
        val positions = PortfolioPositionTable.select { PortfolioPositionTable.portId eq portId }.toList()
        val nonZero = positions.any {
            it[PortfolioPositionTable.amount] != 0 || it[PortfolioPositionTable.amountFrozen] != 0
        }
        if (nonZero) {
            throw IllegalStateException("Cannot close portfolio: there are non‑zero positions")
        }

        PortfolioTable.update({ PortfolioTable.portId eq portId }) {
            it[isClosed] = true
        }

        getById(portId) ?: throw IllegalStateException("Failed to retrieve closed portfolio")
    }

    fun getById(portId: Int): PortfolioResponse? = transaction {
        val portfolioRow = PortfolioTable.select { PortfolioTable.portId eq portId }.singleOrNull() ?: return@transaction null

        val positions = PortfolioPositionTable.select { PortfolioPositionTable.portId eq portId }
            .map {
                PositionResponse(
                    ticker = it[PortfolioPositionTable.ticker],
                    amount = it[PortfolioPositionTable.amount],
                    amountFrozen = it[PortfolioPositionTable.amountFrozen]
                )
            }

        PortfolioResponse(
            portId = portfolioRow[PortfolioTable.portId],
            clntId = portfolioRow[PortfolioTable.clntId],
            name = portfolioRow[PortfolioTable.name],
            createdAt = portfolioRow[PortfolioTable.createdAt],
            positions = positions,
            isClosed = portfolioRow[PortfolioTable.isClosed]
        )
    }

    fun getByClientId(clntId: Int): List<PortfolioResponse> = transaction {
        PortfolioTable.select { PortfolioTable.clntId eq clntId }
            .map { row ->
                val portId = row[PortfolioTable.portId]
                val positions = PortfolioPositionTable.select { PortfolioPositionTable.portId eq portId }
                    .map {
                        PositionResponse(
                            ticker = it[PortfolioPositionTable.ticker],
                            amount = it[PortfolioPositionTable.amount],
                            amountFrozen = it[PortfolioPositionTable.amountFrozen]
                        )
                    }
                PortfolioResponse(
                    portId = portId,
                    clntId = row[PortfolioTable.clntId],
                    name = row[PortfolioTable.name],
                    createdAt = row[PortfolioTable.createdAt],
                    positions = positions,
                    isClosed = row[PortfolioTable.isClosed]
                )
            }
    }
}