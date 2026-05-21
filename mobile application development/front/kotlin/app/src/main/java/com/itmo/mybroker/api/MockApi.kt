package com.itmo.mybroker.api

import com.itmo.mybroker.data.BrokerData

private const val MOCK_JWT = "mock-jwt"

object MockApi {
    fun register(body: RegisterRequest): AuthResponse {
        return AuthResponse(
            token = MOCK_JWT,
            email = body.email,
            username = body.username,
            name = body.name,
            clntId = 1,
            roles = listOf("USER"),
            balance = BrokerData.repository.cashRub,
        )
    }

    fun login(body: LoginRequest): AuthResponse =
        AuthResponse(
            token = MOCK_JWT,
            email = body.email,
            username = "mock_trader",
            name = BrokerData.repository.user.nameRu,
            clntId = 1,
            roles = listOf("USER"),
            balance = BrokerData.repository.cashRub,
        )

    fun getMe(email: String, username: String?, name: String?, balance: Double): UserMe =
        UserMe(
            id = "mock-user",
            userId = 1,
            clntId = 1,
            email = email,
            username = username ?: "mock_trader",
            name = name ?: BrokerData.repository.user.nameRu,
            roles = listOf("USER"),
            status = "ACTIVE",
            balance = balance,
        )

    fun getPortfolio(): PortfolioResponse =
        PortfolioResponse(
            positions = BrokerData.repository.portfolioHoldings.map {
                PortfolioPosition(instrumentId = it.sym, symbol = it.sym, quantity = it.qty.toDouble())
            },
            cashRub = BrokerData.repository.cashRub,
        )

    fun executeTrade(body: TradeRequest, side: String): TradeResponse {
        val tk = BrokerData.repository.bySym(body.instrumentId)
        val stats = BrokerData.repository.dayStats(tk)
        return TradeResponse(
            id = "mock-${System.currentTimeMillis()}",
            instrumentId = body.instrumentId,
            symbol = tk.sym,
            side = side,
            quantity = body.quantity,
            price = stats.price,
            currency = tk.ccy.name,
            executedAt = System.currentTimeMillis(),
        )
    }
}
