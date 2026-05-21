package com.vt.dao

import com.vt.clickhouse.ClickHouseHolder
import com.vt.model.CandlePoint
import com.vt.model.LinePoint
import com.vt.model.Period
import com.vt.model.Timeframe

/**
 * Builds LINE and CANDLE chart data from broker.stocks_trades.
 *
 * Timestamps are returned as Unix ms (Int64) so the frontend can use them
 * directly without parsing.
 *
 * toStartOfInterval(ts, INTERVAL N UNIT) groups trades into buckets.
 * argMin/argMax by ingestedAt give the open/close within each bucket.
 */
object ChartDao {

    fun getLineChart(ticker: String, timeframe: Timeframe, period: Period): List<LinePoint> {
        val sql = buildLineSql(timeframe, period)
        return ClickHouseHolder.withConnection { conn ->
            conn.prepareStatement(sql).use { ps ->
                ps.setString(1, ticker)
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(LinePoint(ts = rs.getLong("ts"), close = rs.getBigDecimal("close")))
                        }
                    }
                }
            }
        }
    }

    fun getCandleChart(ticker: String, timeframe: Timeframe, period: Period): List<CandlePoint> {
        val sql = buildCandleSql(timeframe, period)
        return ClickHouseHolder.withConnection { conn ->
            conn.prepareStatement(sql).use { ps ->
                ps.setString(1, ticker)
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(
                                CandlePoint(
                                    ts     = rs.getLong("ts"),
                                    open   = rs.getBigDecimal("open"),
                                    high   = rs.getBigDecimal("high"),
                                    low    = rs.getBigDecimal("low"),
                                    close  = rs.getBigDecimal("close"),
                                    volume = rs.getLong("volume")
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // SQL builders – only interval/period strings are interpolated (both
    // come from validated enums, so there is no injection risk).
    // The ticker is always passed as a JDBC parameter (?).
    // ─────────────────────────────────────────────────────────────────────

    private fun buildLineSql(tf: Timeframe, period: Period): String {
        val periodFilter = periodFilter(period)
        return """
            SELECT
                toInt64(toUnixTimestamp(toStartOfInterval(ingestedAt, ${tf.toIntervalSql()}))) * 1000 AS ts,
                argMax(price, ingestedAt) AS close
            FROM broker.stocks_trades
            WHERE ticker = ? $periodFilter
            GROUP BY ts
            ORDER BY ts ASC
        """.trimIndent()
    }

    private fun buildCandleSql(tf: Timeframe, period: Period): String {
        val periodFilter = periodFilter(period)
        return """
            SELECT
                toInt64(toUnixTimestamp(toStartOfInterval(ingestedAt, ${tf.toIntervalSql()}))) * 1000 AS ts,
                argMin(price, ingestedAt)  AS open,
                max(price)                 AS high,
                min(price)                 AS low,
                argMax(price, ingestedAt)  AS close,
                toInt64(sum(volume))       AS volume
            FROM broker.stocks_trades
            WHERE ticker = ? $periodFilter
            GROUP BY ts
            ORDER BY ts ASC
        """.trimIndent()
    }

    private fun periodFilter(period: Period): String =
        if (period.intervalSql != null)
            "AND ingestedAt >= now() - INTERVAL ${period.intervalSql}"
        else
            "" // Period.ALL – no time filter
}
