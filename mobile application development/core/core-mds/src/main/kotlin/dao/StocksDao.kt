package com.vt.dao

import com.vt.clickhouse.ClickHouseHolder
import com.vt.model.StockRow
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Reads stock snapshot data from ClickHouse.
 *
 * broker.broker_stocks FINAL  – deduplicated list of all known tickers
 * broker.stocks_trades         – tick-level trade history
 *
 * last_price  = price of the trade with the highest eventId (latest matched trade)
 * day_open    = price of the first trade of the current calendar day
 * dayChangePct = (last - open) / open * 100
 *
 * Stocks with no trades at all are still returned; lastPrice and dayChangePct
 * will be null so the caller / BFF can decide how to display them.
 */
object StocksDao {

    fun getAllStocks(): List<StockRow> = ClickHouseHolder.withConnection { conn ->
        // Two-CTE approach: compute last price and today's opening price separately,
        // then LEFT JOIN so tickers with no trades still appear.
        val sql = """
            WITH
                last_prices AS (
                    SELECT
                        ticker,
                        argMax(price, eventId) AS last_price
                    FROM broker.stocks_trades
                    GROUP BY ticker
                ),
                today_open AS (
                    SELECT
                        ticker,
                        argMin(price, ingestedAt) AS open_price
                    FROM broker.stocks_trades
                    WHERE ingestedAt >= toStartOfDay(now())
                    GROUP BY ticker
                )
            SELECT
                broker.broker_stocks.ticker AS ticker,
                broker.broker_stocks.name AS name,
                broker.broker_stocks.currency AS currency,
                lp.last_price AS last_price,
                top.open_price AS open_price
            FROM broker.broker_stocks FINAL
            LEFT JOIN last_prices lp ON broker.broker_stocks.ticker = lp.ticker
            LEFT JOIN today_open top ON broker.broker_stocks.ticker = top.ticker
        """.trimIndent()

        conn.createStatement().use { st ->
            st.executeQuery(sql).use { rs ->
                buildList {
                    while (rs.next()) {
                        val lastPriceRaw = rs.getBigDecimal("last_price")
                        val openPriceRaw = rs.getBigDecimal("open_price")

                        // ClickHouse LEFT JOIN returns 0 (not NULL) for non-Nullable Decimal columns
                        // when there is no matching row in the right-side CTE.
                        val lastPrice = lastPriceRaw?.takeIf { it > BigDecimal.ZERO }
                        val openPrice = openPriceRaw?.takeIf { it > BigDecimal.ZERO }

                        val dayChangePct: Double? =
                            if (lastPrice != null && openPrice != null) {
                                lastPrice
                                    .subtract(openPrice)
                                    .divide(openPrice, 6, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal(100))
                                    .toDouble()
                            } else null

                        add(
                            StockRow(
                                ticker       = rs.getString("ticker"),
                                name         = rs.getString("name"),
                                currency     = rs.getString("currency"),
                                lastPrice    = lastPrice,
                                dayChangePct = dayChangePct
                            )
                        )
                    }
                }
            }
        }
    }
}
