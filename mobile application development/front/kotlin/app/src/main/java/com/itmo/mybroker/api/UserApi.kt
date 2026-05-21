package com.itmo.mybroker.api

object UserApi {
    private const val PREFIX = "/api/broker-app/v1"

    fun getMe(): UserMe = ApiClient.request("$PREFIX/users/me")

    fun updateBalance(balance: Double): UserMe =
        ApiClient.request("$PREFIX/users/me/balance", method = "PUT", body = mapOf("balance" to balance))

    fun getPortfolio(): PortfolioResponse = ApiClient.request("$PREFIX/users/me/portfolio")
}
