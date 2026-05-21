package com.vt.plugins

import com.vt.model.*
import com.vt.service.MdsService
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {

        // Health check
        get("/health") {
            call.respond(HealthResponse(status = "ok"))
        }

        // ──────────────────────────────────────────────────────────────────
        // GET /v1/stocks
        // Query params: search, sortBy, sortOrder, minPrice, maxPrice,
        //               minDayChangePct, maxDayChangePct
        // ──────────────────────────────────────────────────────────────────
        get("/v1/stocks") {
            val p = call.request.queryParameters

            val sortBy = p["sortBy"]
                ?.let { runCatching { SortBy.valueOf(it.uppercase()) }
                    .getOrElse { throw IllegalArgumentException("Unknown sortBy '$it'. Allowed: PRICE, NAME, DAY_CHANGE_PCT") }
                } ?: SortBy.NAME

            val sortOrder = p["sortOrder"]
                ?.let { runCatching { SortOrder.valueOf(it.uppercase()) }
                    .getOrElse { throw IllegalArgumentException("Unknown sortOrder '$it'. Allowed: ASC, DESC") }
                } ?: SortOrder.ASC

            val minPrice        = p["minPrice"]?.toBigDecimalOrNull()
            val maxPrice        = p["maxPrice"]?.toBigDecimalOrNull()
            val minDayChangePct = p["minDayChangePct"]?.toDoubleOrNull()
            val maxDayChangePct = p["maxDayChangePct"]?.toDoubleOrNull()

            val stocks = MdsService.getStocks(
                search          = p["search"],
                sortBy          = sortBy,
                sortOrder       = sortOrder,
                minPrice        = minPrice,
                maxPrice        = maxPrice,
                minDayChangePct = minDayChangePct,
                maxDayChangePct = maxDayChangePct
            )
            call.respond(StocksResponse(stocks))
        }

        // ──────────────────────────────────────────────────────────────────
        // GET /v1/stocks/{ticker}/chart
        // Query params: chartType (LINE|CANDLE), timeframe (M1|M5|M30|H1|D1|W1),
        //               period (1D|1W|1M|6M|1Y|ALL)
        // ──────────────────────────────────────────────────────────────────
        get("/v1/stocks/{ticker}/chart") {
            val ticker = call.parameters["ticker"]?.uppercase()
                ?: throw IllegalArgumentException("ticker path parameter is required")
            val p = call.request.queryParameters

            val chartType = p["chartType"]
                ?.let { runCatching { ChartType.valueOf(it.uppercase()) }
                    .getOrElse { throw IllegalArgumentException("Unknown chartType '$it'. Allowed: LINE, CANDLE") }
                } ?: ChartType.LINE

            val timeframe = p["timeframe"]
                ?.let { runCatching { Timeframe.valueOf(it.uppercase()) }
                    .getOrElse { throw IllegalArgumentException("Unknown timeframe '$it'. Allowed: M1, M5, M30, H1, D1, W1") }
                } ?: Timeframe.D1

            val period = p["period"]
                ?.let { Period.fromApi(it) }
                ?: Period.P1M

            when (chartType) {
                ChartType.LINE   -> call.respond(MdsService.getLineChart(ticker, timeframe, period))
                ChartType.CANDLE -> call.respond(MdsService.getCandleChart(ticker, timeframe, period))
            }
        }

        // ──────────────────────────────────────────────────────────────────
        // GET /v1/fx/rates
        // Query params: base (required), quote (required)
        // ──────────────────────────────────────────────────────────────────
        get("/v1/fx/rates") {
            val p    = call.request.queryParameters
            val base  = p["base"]  ?: throw IllegalArgumentException("Query parameter 'base' is required")
            val quote = p["quote"] ?: throw IllegalArgumentException("Query parameter 'quote' is required")

            call.respond(MdsService.getFxRate(base, quote))
        }
    }
}
