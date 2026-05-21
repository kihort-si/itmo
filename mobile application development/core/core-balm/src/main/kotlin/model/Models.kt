package com.vt.model

import java.math.BigDecimal
import java.time.Instant

// ----- Account -----
data class AccountResponse(
    val accId: Int,
    val clntId: Int,
    val currency: CurrencyInfo,
    val status: String,
    val balance: BigDecimal
)

data class CurrencyInfo(
    val currId: Int,
    val code: String,
    val refsId: Int
)

data class CreateAccountRequest(
    val clntId: Int,
    val currId: Int? = null   // по умолчанию RUB
)

// ----- Charge -----
data class CreateChargeRequest(
    val chargeType: String,      // "debit", "credit", "freeze", "unfreeze"
    val amount: BigDecimal,
    val billDetail: Int? = null  // bdetId
)

data class ChargeResponse(
    val chargeId: Long,
    val accId: Int,
    val chargeType: String,
    val amount: BigDecimal,
    val billDetailId: Int?,
    val timestamp: Instant,
    val newBalance: BigDecimal
)

// ----- Sync -----
data class SyncResult(
    val rowsCreated: Int,
    val rowsUpdated: Int
)

// ----- Error -----
data class ErrorResponse(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)

// ----- Атрибут (для RabbitMQ) -----
data class AttributeEvent(
    val eventType: String,
    val clientId: Int,
    val attributeId: Int,
    val value: String,
    val startDate: String?,
    val endDate: String?,
    val timestamp: String
)