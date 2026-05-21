// ============================================================
// File: com/vt/model/Models.kt
// ============================================================
package com.vt.model

import java.time.Instant

data class CreatePortfolioRequest(
    val clntId: Int,
    val name: String
)

data class UpdatePortfolioRequest(
    val name: String
)

data class PortfolioResponse(
    val portId: Int,
    val clntId: Int,
    val name: String,
    val createdAt: Instant,
    val positions: List<PositionResponse>,
    val isClosed: Boolean
)

data class PositionResponse(
    val ticker: String,
    val amount: Int,
    val amountFrozen: Int
)

data class OperationRequest(
    val ticker: String,
    val operationType: String, // BUY, SELL, FREEZE, UNFREEZE
    val amount: Int
)

data class OperationResponse(
    val portId: Int,
    val ticker: String,
    val operationType: String,
    val amount: Int,
    val positionAfter: PositionResponse,
    val operationId: Int,
    val createdAt: Instant
)

data class ErrorResponse(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)