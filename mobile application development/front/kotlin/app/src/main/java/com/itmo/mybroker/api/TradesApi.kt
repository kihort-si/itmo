package com.itmo.mybroker.api

object TradesApi {
    private const val PREFIX = "/api/broker-app/v1"

    fun buy(body: TradeRequest): TradeResponse =
        ApiClient.request("$PREFIX/trades/buy", method = "POST", body = body)

    fun sell(body: TradeRequest): TradeResponse =
        ApiClient.request("$PREFIX/trades/sell", method = "POST", body = body)
}
