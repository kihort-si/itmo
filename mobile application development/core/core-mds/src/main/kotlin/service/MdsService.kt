package com.vt.service

import com.vt.dao.ChartDao
import com.vt.dao.FxRatesDao
import com.vt.dao.StocksDao
import com.vt.model.*
import java.math.BigDecimal

object MdsService {
    internal var stocksLoader: () -> List<StockRow> = { StocksDao.getAllStocks() }
    internal var lineChartLoader: (String, Timeframe, Period) -> List<LinePoint> =
        { ticker, timeframe, period -> ChartDao.getLineChart(ticker, timeframe, period) }
    internal var candleChartLoader: (String, Timeframe, Period) -> List<CandlePoint> =
        { ticker, timeframe, period -> ChartDao.getCandleChart(ticker, timeframe, period) }
    internal var fxRateLoader: (String, String) -> BigDecimal? =
        { base, quote -> FxRatesDao.getRate(base, quote) }

    // ──────────────────────────────────────────────
    // Stocks list  (GET /v1/stocks)
    // ──────────────────────────────────────────────

    fun getStocks(
        search: String?,
        sortBy: SortBy,
        sortOrder: SortOrder,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        minDayChangePct: Double?,
        maxDayChangePct: Double?
    ): List<StockRow> {
        var stocks = stocksLoader()

        // Full-text search on ticker and name
        if (!search.isNullOrBlank()) {
            val q = search.trim().lowercase()
            stocks = stocks.filter {
                it.ticker.lowercase().contains(q) || it.name.lowercase().contains(q)
            }
        }

        // Price range filters
        if (minPrice != null) stocks = stocks.filter { it.lastPrice != null && it.lastPrice >= minPrice }
        if (maxPrice != null) stocks = stocks.filter { it.lastPrice != null && it.lastPrice <= maxPrice }

        // Day-change-pct range filters
        if (minDayChangePct != null) stocks = stocks.filter { it.dayChangePct != null && it.dayChangePct >= minDayChangePct }
        if (maxDayChangePct != null) stocks = stocks.filter { it.dayChangePct != null && it.dayChangePct <= maxDayChangePct }

        // Sorting (nulls last for numeric columns)
        val comparator: Comparator<StockRow> = when (sortBy) {
            SortBy.PRICE        -> compareBy(nullsLast(naturalOrder())) { it.lastPrice }
            SortBy.NAME         -> compareBy { it.name.lowercase() }
            SortBy.DAY_CHANGE_PCT -> compareBy(nullsLast(naturalOrder())) { it.dayChangePct }
        }

        return if (sortOrder == SortOrder.DESC) {
            stocks.sortedWith(comparator.reversed())
        } else {
            stocks.sortedWith(comparator)
        }
    }

    // ──────────────────────────────────────────────
    // Chart  (GET /v1/stocks/{ticker}/chart)
    // ──────────────────────────────────────────────

    fun getLineChart(ticker: String, timeframe: Timeframe, period: Period): LineChartResponse =
        LineChartResponse(
            ticker    = ticker,
            timeframe = timeframe.name,
            period    = period.apiName,
            points    = lineChartLoader(ticker, timeframe, period)
        )

    fun getCandleChart(ticker: String, timeframe: Timeframe, period: Period): CandleChartResponse =
        CandleChartResponse(
            ticker    = ticker,
            timeframe = timeframe.name,
            period    = period.apiName,
            candles   = candleChartLoader(ticker, timeframe, period)
        )

    // ──────────────────────────────────────────────
    // FX rates  (GET /v1/fx/rates)
    // ──────────────────────────────────────────────

    fun getFxRate(base: String, quote: String): FxRateResponse {
        val b = base.uppercase()
        val q = quote.uppercase()

        val rate = if (b == q) {
            BigDecimal.ONE
        } else {
            fxRateLoader(b, q)
                ?: throw NoSuchElementException("FX rate not found for $b/$q")
        }

        return FxRateResponse(base = b, quote = q, rate = rate)
    }
}
