package com.vt.service

import com.fasterxml.jackson.databind.JsonNode
import com.vt.model.SyncResult
import com.vt.table.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

object SyncService {
    private val logger = LoggerFactory.getLogger(SyncService::class.java)
    private val client = HttpClient(CIO)
    private var apiGatewayUrl: String = ""
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun init(gatewayUrl: String) {
        apiGatewayUrl = gatewayUrl
    }

    fun startAsyncSync() {
        scope.launch {
            try {
                syncCurrencies()
                syncBillDetails()
            } catch (e: Exception) {
                logger.error("Async sync failed at startup", e)
            }
        }
    }

    fun syncCurrencyTable(): SyncResult = runBlocking { syncCurrencies() }
    fun syncBillDetailsTable(): SyncResult = runBlocking { syncBillDetails() }

    private suspend fun syncCurrencies(): SyncResult {
        val response: JsonNode = client.get("$apiGatewayUrl/api/v1/refs/data/currency").bodyAsText().let {
            com.fasterxml.jackson.module.kotlin.jacksonObjectMapper().readTree(it)
        }
        val values = response["values"]
        if (values == null || !values.isArray || values.size() == 0) {
            throw IllegalStateException("Empty or invalid values array from refs")
        }

        var created = 0
        var updated = 0
        transaction {
            for (item in values) {
                val entityId = item["entityListId"].asInt()
                val code = item["data"]["code"].asText()

                val existing = CurrencyCodeTable.selectAll()
                    .where { CurrencyCodeTable.refsId eq entityId }
                    .singleOrNull()

                if (existing == null) {
                    CurrencyCodeTable.insert {
                        it[currId] = entityId
                        it[refsId] = entityId
                        it[CurrencyCodeTable.code] = code
                    }
                    created++
                } else if (existing[CurrencyCodeTable.code] != code) {
                    CurrencyCodeTable.update({ CurrencyCodeTable.refsId eq entityId }) {
                        it[CurrencyCodeTable.code] = code
                    }
                    updated++
                }
            }
        }
        logger.info("Currency sync: created $created, updated $updated")
        return SyncResult(created, updated)
    }

    private suspend fun syncBillDetails(): SyncResult {
        val response: JsonNode = client.get("$apiGatewayUrl/api/v1/refs/data/bill_details").bodyAsText().let {
            com.fasterxml.jackson.module.kotlin.jacksonObjectMapper().readTree(it)
        }
        val values = response["values"]
        if (values == null || !values.isArray || values.size() == 0) {
            throw IllegalStateException("Empty or invalid values array from refs")
        }

        var created = 0
        var updated = 0
        transaction {
            for (item in values) {
                val entityId = item["entityListId"].asInt()
                val code = item["data"]["code"].asText()

                val existing = BillDetailsTable.selectAll()
                    .where { BillDetailsTable.bdetRefsId eq entityId }
                    .singleOrNull()

                if (existing == null) {
                    BillDetailsTable.insert {
                        it[bdetId] = entityId
                        it[bdetRefsId] = entityId
                        it[BillDetailsTable.code] = code
                    }
                    created++
                } else if (existing[BillDetailsTable.code] != code) {
                    BillDetailsTable.update({ BillDetailsTable.bdetRefsId eq entityId }) {
                        it[BillDetailsTable.code] = code
                    }
                    updated++
                }
            }
        }
        logger.info("Bill details sync: created $created, updated $updated")
        return SyncResult(created, updated)
    }
}