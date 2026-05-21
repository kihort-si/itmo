package com.itmo.mybroker.data

enum class Ccy { RUB, USD, EUR }

enum class MarketId { MOEX, NASDAQ }

data class Ticker(
    val sym: String,
    val market: MarketId,
    val nameRu: String,
    val nameEn: String,
    val sectorRu: String,
    val sectorEn: String,
    val base: Double,
    val vol: Double,
    val drift: Double,
    val ccy: Ccy,
)

data class Candle(
    val t: Long,
    val o: Double,
    val h: Double,
    val l: Double,
    val c: Double,
    val v: Double,
)

data class DayStats(
    val price: Double,
    val change: Double,
    val changePct: Double,
    val spark: List<Double>,
    val candles: List<Candle>,
)

data class BookLevel(val price: Double, val size: Int)

data class Orderbook(
    val bids: List<BookLevel>,
    val asks: List<BookLevel>,
    val mid: Double,
    val tick: Double,
)

data class Holding(val sym: String, val qty: Int, val avg: Double)

enum class OpType { Buy, Sell, Cancel, Reject }
enum class OpStatus { Filled, Partial, Cancelled, Rejected, Pending }

data class Operation(
    val id: String,
    val t: Long,
    val type: OpType,
    val sym: String,
    val qty: Int,
    val price: Double,
    val status: OpStatus,
    val currency: Ccy,
    val filledQty: Int? = null,
    val reason: String? = null,
)

data class ActiveOrder(
    val id: String,
    val sym: String,
    val side: TradeSide,
    val qty: Int,
    val limit: Double,
    val filled: Int,
    val placedAt: Long,
)

enum class TradeSide { Buy, Sell }

data class UserProfile(
    val initials: String,
    val nameRu: String,
    val nameEn: String,
    val email: String,
    val memberSinceRu: String,
    val memberSinceEn: String,
    val ordersCount: String,
    val streak: String,
)

data class MarketIndex(val label: String, val value: String)
