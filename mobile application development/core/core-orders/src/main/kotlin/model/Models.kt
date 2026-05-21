package com.vt.model

import com.fasterxml.jackson.databind.JsonNode
import java.time.Instant

data class CreateOrderRequest(
    val clntId: Int,
    val orderType: String,
    val orderData: JsonNode?
)

data class UpdateOrderStatusRequest(
    val statusCode: String
)

data class UpdateOrderDataRequest(
    val path: String,
    val value: JsonNode
)

data class OrderResponse(
    val ordId: Int,
    val clntId: Int,
    val orderType: String,
    val status: String,
    val dateStart: Instant,
    val dateEnd: Instant?,
    val orderData: JsonNode?
)

data class PaginatedOrdersResponse(
    val orders: List<OrderResponse>,
    val total: Long,
    val page: Int,
    val size: Int,
    val totalPages: Int
)

data class ErrorResponse(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)