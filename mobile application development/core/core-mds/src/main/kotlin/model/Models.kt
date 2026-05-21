package com.vt.model

import java.math.BigDecimal

// ──────────────────────────────────────────────
// Domain
// ──────────────────────────────────────────────

data class StockRow(
    val ticker: String,
    val name: String,
    val currency: String,
    val lastPrice: BigDecimal?,
    val dayChangePct: Double?
)

data class LinePoint(
    val ts: Long,           // Unix timestamp ms
    val close: BigDecimal
)

data class CandlePoint(
    val ts: Long,           // Unix timestamp ms
    val open: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val close: BigDecimal,
    val volume: Long
)

// ──────────────────────────────────────────────
// Responses
// ──────────────────────────────────────────────

data class StocksResponse(
    val stocks: List<StockRow>
)

data class LineChartResponse(
    val ticker: String,
    val chartType: String = "LINE",
    val timeframe: String,
    val period: String,
    val points: List<LinePoint>
)

data class CandleChartResponse(
    val ticker: String,
    val chartType: String = "CANDLE",
    val timeframe: String,
    val period: String,
    val candles: List<CandlePoint>
)

data class FxRateResponse(
    val base: String,
    val quote: String,
    val rate: BigDecimal
)

data class HealthResponse(
    val status: String
)

data class ErrorResponse(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)

// ──────────────────────────────────────────────
// Enums
// ──────────────────────────────────────────────

enum class SortBy { PRICE, NAME, DAY_CHANGE_PCT }

enum class SortOrder { ASC, DESC }

enum class ChartType { LINE, CANDLE }

enum class Timeframe {
    M1, M5, M30, H1, D1, W1;

    /** ClickHouse INTERVAL expression for toStartOfInterval() */
    fun toIntervalSql(): String = when (this) {
        M1  -> "INTERVAL 1 MINUTE"
        M5  -> "INTERVAL 5 MINUTE"
        M30 -> "INTERVAL 30 MINUTE"
        H1  -> "INTERVAL 1 HOUR"
        D1  -> "INTERVAL 1 DAY"
        W1  -> "INTERVAL 1 WEEK"
    }
}

enum class Period(
    /** Value used in the API query parameter */
    val apiName: String,
    /** ClickHouse interval used in WHERE clause; null means no filter (ALL) */
    val intervalSql: String?
) {
    P10M("10M",  "10 MINUTE"),
    P1H("1H",  "1 HOUR"),
    P6H("6H",  "6 HOUR"),
    P1D("1D",  "1 DAY"),
    P1W("1W",  "1 WEEK"),
    P10D("10D",  "10 DAY"),
    P1M("1M",  "1 MONTH"),
    P6M("6M",  "6 MONTH"),
    P1Y("1Y",  "1 YEAR"),
    ALL("ALL", null);

    companion object {
        fun fromApi(s: String): Period =
            values().find { it.apiName.equals(s, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown period '$s'. Allowed: 10M, 1H, 6H, 1D, 1W, 10D, 1M, 6M, 1Y, ALL")
    }
}
