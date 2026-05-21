package com.itmo.mybroker.core

import com.itmo.mybroker.api.TradeResponse
import com.itmo.mybroker.data.BrokerData
import com.itmo.mybroker.data.Ccy

data class ExecutedTrade(
    val id: String,
    val instrumentId: String,
    val symbol: String,
    val side: String,
    val quantity: Double,
    val price: Double,
    val currency: Ccy,
    val executedAt: Long,
)

object PortfolioStore {
    private val trades = mutableListOf<ExecutedTrade>()

    fun seedIfEmpty() {
        if (trades.isNotEmpty()) return
        BrokerData.repository.portfolioHoldings.forEachIndexed { i, h ->
            val tk = BrokerData.repository.bySym(h.sym)
            trades.add(
                ExecutedTrade(
                    id = "seed-${h.sym}",
                    instrumentId = h.sym,
                    symbol = h.sym,
                    side = "buy",
                    quantity = h.qty.toDouble(),
                    price = h.avg,
                    currency = tk.ccy,
                    executedAt = java.util.Calendar.getInstance().apply { set(2026, 4, 1) }.timeInMillis + i * 86400000L,
                ),
            )
        }
    }

    fun recordTrade(t: TradeResponse) {
        val ccy = runCatching { Ccy.valueOf(t.currency) }.getOrDefault(Ccy.RUB)
        trades.add(
            ExecutedTrade(
                id = t.id,
                instrumentId = t.instrumentId,
                symbol = t.symbol,
                side = t.side,
                quantity = t.quantity,
                price = t.price,
                currency = ccy,
                executedAt = t.executedAt,
            ),
        )
    }

    fun getQuantity(sym: String): Double {
        val s = sym.uppercase()
        return trades.filter { it.symbol.uppercase() == s }
            .sumOf { if (it.side == "buy") it.quantity else -it.quantity }
    }

    fun getCostBasis(sym: String): Double? {
        val s = sym.uppercase()
        var qty = 0.0
        var cost = 0.0
        val sorted = trades.filter { it.symbol.uppercase() == s }.sortedBy { it.executedAt }
        for (t in sorted) {
            if (t.side == "buy") {
                cost += t.price * t.quantity
                qty += t.quantity
            } else {
                if (qty <= 0) continue
                val avg = cost / qty
                val sellQty = minOf(t.quantity, qty)
                cost -= avg * sellQty
                qty -= sellQty
            }
        }
        if (qty <= 0) return null
        return cost / qty
    }
}
