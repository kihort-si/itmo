package com.itmo.mybroker.data

interface BrokerRepository {
    val tickers: List<Ticker>
    val portfolioHoldings: List<Holding>
    val cashRub: Double
    val history: List<Operation>
    val activeOrders: List<ActiveOrder>
    val user: UserProfile
    val indices: List<MarketIndex>
    val fx: Market.FX

    fun bySym(sym: String): Ticker
    fun genCandles(t: Ticker, tf: String = "1d", n: Int = 240): List<Candle>
    fun dayStats(t: Ticker): DayStats
    fun genOrderbook(t: Ticker, levels: Int = 12): Orderbook
}

object MockBrokerRepository : BrokerRepository {
    override val tickers get() = Market.TICKERS
    override val portfolioHoldings get() = Market.PORTFOLIO_HOLDINGS
    override val cashRub get() = Market.CASH_RUB
    override val history get() = Market.HISTORY
    override val activeOrders get() = Market.ACTIVE_ORDERS
    override val user get() = Market.USER
    override val indices get() = Market.INDICES
    override val fx get() = Market.FX

    override fun bySym(sym: String) = Market.bySym(sym)
    override fun genCandles(t: Ticker, tf: String, n: Int) = Market.genCandles(t, tf, n)
    override fun dayStats(t: Ticker) = Market.dayStats(t)
    override fun genOrderbook(t: Ticker, levels: Int) = Market.genOrderbook(t, levels)
}

object BrokerData {
    val repository: BrokerRepository = MockBrokerRepository
}
