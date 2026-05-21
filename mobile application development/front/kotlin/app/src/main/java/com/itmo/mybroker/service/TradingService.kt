package com.itmo.mybroker.service

import com.itmo.mybroker.api.MockApi
import com.itmo.mybroker.api.PortfolioPosition
import com.itmo.mybroker.api.TradeRequest
import com.itmo.mybroker.api.TradeResponse
import com.itmo.mybroker.api.TradesApi
import com.itmo.mybroker.api.UserApi
import com.itmo.mybroker.api.isNetworkOrMockError
import com.itmo.mybroker.core.LiveQuote
import com.itmo.mybroker.core.PortfolioStore
import com.itmo.mybroker.core.QuotesHub
import com.itmo.mybroker.core.SessionStore
import com.itmo.mybroker.data.BrokerData
import com.itmo.mybroker.data.Ccy
import com.itmo.mybroker.data.MarketId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

data class PortfolioUiRow(
    val sym: String,
    val qty: Double,
    val avgCost: Double,
    val market: MarketId,
    val nameRu: String,
    val nameEn: String,
    val valueRub: Double,
    val pnlRub: Double,
    val pnlPct: Double,
)

object TradingService {
    private fun toRub(amount: Double, ccy: Ccy): Double =
        if (ccy == Ccy.USD) amount * BrokerData.repository.fx.USD_RUB else amount

    private fun getPortfolioPositions(): Pair<List<PortfolioPosition>, Double> {
        return try {
            val p = UserApi.getPortfolio()
            p.positions to (p.cashRub ?: SessionStore.balance.value)
        } catch (e: Exception) {
            if (!isNetworkOrMockError(e)) throw e
            val m = MockApi.getPortfolio()
            m.positions to (m.cashRub ?: BrokerData.repository.cashRub)
        }
    }

    private fun buildRows(positions: List<PortfolioPosition>): List<PortfolioUiRow> {
        return positions.mapNotNull { p ->
            val sym = (p.symbol.ifBlank { null } ?: p.instrumentId).uppercase()
            val tk = runCatching { BrokerData.repository.bySym(sym) }.getOrNull() ?: return@mapNotNull null
            val q = QuotesHub.quoteFor(tk.sym)
            val stats = BrokerData.repository.dayStats(tk)
            val price = q?.price ?: stats.price
            val qty = if (p.quantity > 0) p.quantity else PortfolioStore.getQuantity(sym)
            if (qty <= 0) return@mapNotNull null
            val avgCost = PortfolioStore.getCostBasis(sym)
                ?: BrokerData.repository.portfolioHoldings.find { it.sym == sym }?.avg?.toDouble()
                ?: price
            val valueRub = toRub(price * qty, tk.ccy)
            val investedRub = toRub(avgCost * qty, tk.ccy)
            val pnlRub = valueRub - investedRub
            val pnlPct = if (investedRub > 0) pnlRub / investedRub * 100 else 0.0
            PortfolioUiRow(
                sym = tk.sym,
                qty = qty,
                avgCost = avgCost,
                market = tk.market,
                nameRu = tk.nameRu,
                nameEn = tk.nameEn,
                valueRub = valueRub,
                pnlRub = pnlRub,
                pnlPct = pnlPct,
            )
        }
    }

    fun mergeWithLive(rows: List<PortfolioUiRow>, quotes: Map<String, LiveQuote>): List<PortfolioUiRow> {
        return rows.map { r ->
            val tk = BrokerData.repository.bySym(r.sym)
            val price = quotes[r.sym]?.price ?: BrokerData.repository.dayStats(tk).price
            val valueRub = toRub(price * r.qty, tk.ccy)
            val investedRub = toRub(r.avgCost * r.qty, tk.ccy)
            val pnlRub = valueRub - investedRub
            val pnlPct = if (investedRub > 0) pnlRub / investedRub * 100 else 0.0
            r.copy(valueRub = valueRub, pnlRub = pnlRub, pnlPct = pnlPct)
        }
    }

    suspend fun loadPortfolioRows(): List<PortfolioUiRow> = withContext(Dispatchers.IO) {
        PortfolioStore.seedIfEmpty()
        val (positions, _) = getPortfolioPositions()
        buildRows(positions)
    }

    suspend fun executeTrade(instrumentId: String, quantity: Double, side: String): TradeResponse =
        withContext(Dispatchers.IO) {
            val body = TradeRequest(instrumentId = instrumentId, quantity = quantity)
            val trade = try {
                if (side == "buy") TradesApi.buy(body) else TradesApi.sell(body)
            } catch (e: Exception) {
                if (!isNetworkOrMockError(e)) throw e
                MockApi.executeTrade(body, side)
            }
            PortfolioStore.recordTrade(trade)
            val tk = BrokerData.repository.bySym(trade.symbol)
            val totalRub = toRub(trade.price * trade.quantity, tk.ccy)
            val bal = SessionStore.balance.value
            val next = if (side == "buy") {
                max(0.0, bal - totalRub * 1.005)
            } else {
                bal + totalRub * 0.995
            }
            SessionStore.setBalance(next)
            trade
        }
}
