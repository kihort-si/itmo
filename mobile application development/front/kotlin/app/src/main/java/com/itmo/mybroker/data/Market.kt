package com.itmo.mybroker.data

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object Market {

    private fun mulberry32(seedIn: Int): () -> Double {
        var a = seedIn
        return {
            a += 0x6D2B79F5
            var t = a
            t = (t xor (t ushr 15)) * (t or 1)
            t = t xor (t + (t xor (t ushr 7)) * (t or 61))

            ((t xor (t ushr 14)).toLong() and 0xFFFFFFFFL).toDouble() / 4294967296.0
        }
    }

    private fun strSeed(s: String): Int {

        var h = 0x811C9DC5L.toInt()
        for (ch in s) {
            h = (h xor ch.code) * 16777619
        }
        return h
    }

    val TICKERS: List<Ticker> = listOf(
        Ticker("SBER",  MarketId.MOEX,   "Сбербанк",     "Sberbank",            "Банки",         "Banks",       312.45,  0.018, 0.0006,  Ccy.RUB),
        Ticker("GAZP",  MarketId.MOEX,   "Газпром",      "Gazprom",             "Энергетика",    "Energy",      138.62,  0.022, -0.0002, Ccy.RUB),
        Ticker("LKOH",  MarketId.MOEX,   "Лукойл",       "Lukoil",              "Энергетика",    "Energy",      7642.5,  0.014, 0.0003,  Ccy.RUB),
        Ticker("YDEX",  MarketId.MOEX,   "Яндекс",       "Yandex",              "Технологии",    "Tech",        4378.0,  0.026, 0.0010,  Ccy.RUB),
        Ticker("T",     MarketId.MOEX,   "Т-Технологии", "T-Technologies",      "Финансы",       "Finance",     3215.6,  0.024, 0.0007,  Ccy.RUB),
        Ticker("GMKN",  MarketId.MOEX,   "Норникель",    "Nornickel",           "Металлургия",   "Metals",      132.84,  0.020, -0.0001, Ccy.RUB),
        Ticker("ROSN",  MarketId.MOEX,   "Роснефть",     "Rosneft",             "Энергетика",    "Energy",      588.30,  0.017, 0.0001,  Ccy.RUB),
        Ticker("NVTK",  MarketId.MOEX,   "Новатэк",      "Novatek",             "Энергетика",    "Energy",      1234.5,  0.019, -0.0003, Ccy.RUB),
        Ticker("AAPL",  MarketId.NASDAQ, "Apple",        "Apple Inc.",          "Технологии",    "Tech",        218.74,  0.014, 0.0004,  Ccy.USD),
        Ticker("MSFT",  MarketId.NASDAQ, "Microsoft",    "Microsoft Corp.",     "Технологии",    "Tech",        462.18,  0.013, 0.0005,  Ccy.USD),
        Ticker("NVDA",  MarketId.NASDAQ, "NVIDIA",       "NVIDIA Corp.",        "Полупроводники","Semis",       138.92,  0.030, 0.0014,  Ccy.USD),
        Ticker("TSLA",  MarketId.NASDAQ, "Tesla",        "Tesla Inc.",          "Авто",          "Auto",        248.50,  0.034, -0.0003, Ccy.USD),
        Ticker("GOOGL", MarketId.NASDAQ, "Alphabet",     "Alphabet Inc.",       "Технологии",    "Tech",        178.20,  0.016, 0.0004,  Ccy.USD),
        Ticker("AMZN",  MarketId.NASDAQ, "Amazon",       "Amazon.com",          "E-commerce",    "E-commerce",  196.85,  0.018, 0.0005,  Ccy.USD),
        Ticker("META",  MarketId.NASDAQ, "Meta",         "Meta Platforms",      "Технологии",    "Tech",        528.60,  0.020, 0.0006,  Ccy.USD),
        Ticker("AMD",   MarketId.NASDAQ, "AMD",          "Advanced Micro Devices", "Полупроводники", "Semis",    156.30,  0.028, 0.0008,  Ccy.USD),
    )

    object FX {
        const val RUB_USD = 0.0103
        const val RUB_EUR = 0.0096
        const val USD_RUB = 97.05
        const val EUR_RUB = 104.8
        const val USD_EUR = 0.93
        const val EUR_USD = 1.075
    }

    private fun tfMs(tf: String): Long = when (tf) {
        "5m" -> 5L * 60 * 1000
        "30m" -> 30L * 60 * 1000
        "1h" -> 60L * 60 * 1000
        "1d" -> 24L * 60 * 60 * 1000
        "1w" -> 7L * 24 * 60 * 60 * 1000
        else -> 24L * 60 * 60 * 1000
    }

    private val candleCache = HashMap<String, List<Candle>>()

    fun genCandles(t: Ticker, tf: String = "1d", n: Int = 240): List<Candle> {
        val key = "${t.sym}:$tf:$n"
        candleCache[key]?.let { return it }

        val ms = tfMs(tf)
        val rand = mulberry32(strSeed("${t.sym}:$tf"))
        val out = ArrayList<Candle>(n)
        var price = t.base * (0.85 + rand() * 0.3)
        val volBase = (t.base * 0.001) * (if (t.sym == "GAZP") 5e6 else 2e5)

        val now = utc(2026, 5, 8, 9, 29)
        val start = now - n * ms
        for (i in 0 until n) {
            val time = start + i * ms

            var g = 0.0
            repeat(6) { g += rand() }
            g = (g - 3.0) / 1.732
            val ret = t.drift + t.vol * g
            val open = price
            val close = open * (1 + ret)
            val hRet = abs(t.vol * (rand() * 1.5 + 0.2))
            val lRet = abs(t.vol * (rand() * 1.5 + 0.2))
            val high = max(open, close) * (1 + hRet)
            val low  = min(open, close) * (1 - lRet)
            val v = volBase * (0.4 + rand() * 1.8) * (1 + abs(g) * 0.6)
            out.add(Candle(time, open, high, low, close, v))
            price = close
        }

        val last = out.last()
        val adjust = t.base / last.c
        if (abs(1 - adjust) < 0.4) {
            val k = min(6, out.size)
            for (i in (out.size - k) until out.size) {
                val w = (i - (out.size - k) + 1).toDouble() / k
                val m = 1 + (adjust - 1) * w
                val b = out[i]
                out[i] = b.copy(o = b.o * m, c = b.c * m, h = b.h * m, l = b.l * m)
            }
        }
        candleCache[key] = out
        return out
    }

    fun dayStats(t: Ticker): DayStats {
        val c = genCandles(t, "1d", 60)
        val last = c[c.size - 1]
        val prev = c[c.size - 2]
        val change = last.c - prev.c
        val changePct = change / prev.c * 100
        val spark = c.subList(c.size - 30, c.size).map { it.c }
        return DayStats(price = last.c, change = change, changePct = changePct, spark = spark, candles = c)
    }

    fun genOrderbook(t: Ticker, levels: Int = 12): Orderbook {
        val stats = dayStats(t)
        val mid = stats.price
        val tick = when {
            mid > 1000 -> 0.5
            mid > 100 -> 0.05
            else -> 0.01
        }
        val rand = mulberry32(strSeed("${t.sym}:ob"))
        val bids = ArrayList<BookLevel>(levels)
        val asks = ArrayList<BookLevel>(levels)
        for (i in 0 until levels) {
            val bp = mid - tick * (i + 1) - rand() * tick * 0.4
            val ap = mid + tick * (i + 1) + rand() * tick * 0.4
            val baseSize = (50 + rand() * 950).toInt()
            val depthFactor = 1 + i * 0.15 + rand() * 0.4
            bids.add(BookLevel(bp, (baseSize * depthFactor).toInt()))
            asks.add(BookLevel(ap, (baseSize * depthFactor * (0.7 + rand() * 0.6)).toInt()))
        }
        return Orderbook(bids = bids, asks = asks, mid = mid, tick = tick)
    }

    fun bySym(sym: String): Ticker = TICKERS.firstOrNull { it.sym == sym } ?: TICKERS.first()

    val PORTFOLIO_HOLDINGS: List<Holding> = listOf(
        Holding("SBER", 240, 285.40),
        Holding("YDEX", 8,   4012.00),
        Holding("LKOH", 3,   7320.00),
        Holding("NVDA", 14,  121.20),
        Holding("AAPL", 22,  198.40),
        Holding("T",    18,  3105.00),
    )

    const val CASH_RUB: Double = 184_320.0

    val HISTORY: List<Operation> = listOf(
        Operation("op-2024", utc(2026, 5, 8, 8, 12),  OpType.Buy,    "NVDA", 4,   138.92,  OpStatus.Filled,    Ccy.USD),
        Operation("op-2023", utc(2026, 5, 7, 16, 45), OpType.Sell,   "GAZP", 100, 138.62,  OpStatus.Filled,    Ccy.RUB),
        Operation("op-2022", utc(2026, 5, 7, 12, 30), OpType.Buy,    "YDEX", 2,   4368.50, OpStatus.Partial,   Ccy.RUB, filledQty = 1),
        Operation("op-2021", utc(2026, 5, 6, 14, 15), OpType.Buy,    "SBER", 60,  309.80,  OpStatus.Filled,    Ccy.RUB),
        Operation("op-2020", utc(2026, 5, 6, 10, 2),  OpType.Cancel, "TSLA", 5,   250.00,  OpStatus.Cancelled, Ccy.USD),
        Operation("op-2019", utc(2026, 5, 5, 18, 22), OpType.Sell,   "LKOH", 1,   7615.00, OpStatus.Filled,    Ccy.RUB),
        Operation("op-2018", utc(2026, 5, 5, 11, 50), OpType.Buy,    "AAPL", 6,   215.30,  OpStatus.Filled,    Ccy.USD),
        Operation("op-2017", utc(2026, 5, 4, 15, 10), OpType.Buy,    "T",    5,   3201.00, OpStatus.Filled,    Ccy.RUB),
        Operation("op-2016", utc(2026, 5, 3, 17, 35), OpType.Reject, "NVDA", 50,  138.92,  OpStatus.Rejected,  Ccy.USD, reason = "Недостаточно средств"),
    )

    val ACTIVE_ORDERS: List<ActiveOrder> = listOf(
        ActiveOrder("lo-501", "SBER", TradeSide.Buy,  100, 308.00, 0, utc(2026, 5, 8, 9, 0)),
        ActiveOrder("lo-502", "GMKN", TradeSide.Sell, 50,  135.00, 0, utc(2026, 5, 7, 14, 30)),
    )

    val USER = UserProfile(
        initials = "МА",
        nameRu = "Михаил Ахметов",
        nameEn = "Mikhail Akhmetov",
        email = "m.akhmetov@itmo.ru",
        memberSinceRu = "12 окт 2024",
        memberSinceEn = "Oct 12, 2024",
        ordersCount = "183",
        streak = "14d",
    )

    val INDICES: List<MarketIndex> = listOf(
        MarketIndex("MOEX", "3 248,17"),
        MarketIndex("NASDAQ", "21 184,3"),
    )

    private fun utc(year: Int, month1: Int, day: Int, hour: Int, minute: Int): Long {
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        cal.clear()
        cal.set(year, month1 - 1, day, hour, minute, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
