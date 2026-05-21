package com.itmo.mybroker.api

data class AuthResponse(
    val token: String,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val email: String,
    val username: String? = null,
    val name: String? = null,
    val clntId: Int? = null,
    val roles: List<String> = emptyList(),
    val balance: Double,
    val user: UserMe? = null,
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val username: String,
)

data class LoginRequest(
    val email: String,
    val password: String,
)

data class UserMe(
    val id: String,
    val userId: Int? = null,
    val clntId: Int? = null,
    val email: String,
    val username: String? = null,
    val name: String? = null,
    val roles: List<String> = emptyList(),
    val status: String? = null,
    val balance: Double,
)

data class AvailabilityResponse(
    val inUse: Boolean,
)

data class PortfolioPosition(
    val instrumentId: String,
    val symbol: String,
    val quantity: Double,
)

data class PortfolioResponse(
    val positions: List<PortfolioPosition>,
    val cashRub: Double? = null,
)

data class TradeRequest(
    val instrumentId: String,
    val quantity: Double,
)

data class TradeResponse(
    val id: String,
    val instrumentId: String,
    val symbol: String,
    val side: String,
    val quantity: Double,
    val price: Double,
    val currency: String,
    val executedAt: Long,
)
