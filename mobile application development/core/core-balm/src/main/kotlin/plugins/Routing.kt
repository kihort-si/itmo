package com.vt.plugins

import com.vt.dao.*
import com.vt.model.*
import com.vt.service.ScheduledReportService
import com.vt.service.ScriptEngine
import com.vt.service.SyncService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        route("/api/v1/balm") {

            // ---- Sync ----
            post("/sync/currency") {
                val result = SyncService.syncCurrencyTable()
                call.respond(HttpStatusCode.Created, result)
            }
            post("/sync/billdetails") {
                val result = SyncService.syncBillDetailsTable()
                call.respond(HttpStatusCode.Created, result)
            }

            // ---- Accounts ----
            post("/accounts") {
                val request = call.receive<CreateAccountRequest>()
                val account = AccountDao.create(request)
                call.respond(HttpStatusCode.Created, account)
            }

            get("/accounts/{accId}") {
                val accId = call.parameters["accId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid account id")
                val account = AccountDao.getById(accId) ?: throw NoSuchElementException("Account not found")
                call.respond(account)
            }

            post("/accounts/{accId}/close") {
                val accId = call.parameters["accId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid account id")
                try {
                    val closed = AccountDao.closeAccount(accId)
                    call.respond(closed)
                } catch (e: IllegalStateException) {
                    // Преобразуем reason в сообщение для ErrorResponse
                    throw when (e.message) {
                        "nonzerobalance" -> IllegalStateException("Account balance must be zero to close, reason=nonzerobalance")
                        "unfinishedorders" -> IllegalStateException("Account has unfinished purchase orders, reason=unfinishedorders")
                        else -> e
                    }
                }
            }

            get("/clients/{clntId}/accounts") {
                val clntId = call.parameters["clntId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid client id")
                val accounts = AccountDao.getByClientId(clntId)
                call.respond(accounts)
            }

            // ---- Charges ----
            post("/accounts/{accId}/charge") {
                val accId = call.parameters["accId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid account id")
                val request = call.receive<CreateChargeRequest>()
                val response = ChargeDao.createCharge(accId, request)
                call.respond(HttpStatusCode.Created, response)
            }

            // ---- Balance recalc ----
            post("/accounts/{accId}/balance/recalculate") {
                val accId = call.parameters["accId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid account id")
                val newBalance = ChargeDao.recalculateBalance(accId)
                call.respond(mapOf("accId" to accId, "balance" to newBalance))
            }

            post("/reports/send") {
                val hour = call.request.queryParameters["hour"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Parameter 'hour' must be an integer")
                ScheduledReportService.manualSend(hour)
                call.respond(HttpStatusCode.OK, mapOf("status" to "sent"))
            }

            // Сводка по балансу счета за период
            get("/accounts/{accId}/summary") {
                val accId = call.parameters["accId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid account id")
                val startDateStr = call.request.queryParameters["startDate"]
                    ?: throw IllegalArgumentException("startDate required")
                val endDateStr = call.request.queryParameters["endDate"]
                    ?: throw IllegalArgumentException("endDate required")

                val startDate = java.time.OffsetDateTime.parse(startDateStr).toInstant()
                val endDate = java.time.OffsetDateTime.parse(endDateStr).toInstant()

                val summary = BalanceSummaryDao.getSummary(accId, startDate, endDate)
                call.respond(summary)
            }

            // Расчёт по схеме
            post("/calculate") {
                val request = call.receive<Map<String, Any>>()
                val schemeCode = request["calcSchemeCode"] as? String
                    ?: throw IllegalArgumentException("calcSchemeCode is required")
                val clientId = (request["clientId"] as? Number)?.toInt()
                    ?: throw IllegalArgumentException("clientId must be integer")
                val regionId = (request["regionId"] as? Number)?.toInt()
                    ?: throw IllegalArgumentException("regionId must be integer")
                val parameters = request["parameters"] as? Map<String, Any>
                    ?: throw IllegalArgumentException("parameters map is required")

                // Добавляем regionId и clientId в параметры (скрипты могут их использовать)
                val fullParameters = parameters.toMutableMap()
                fullParameters["regionId"] = regionId
                fullParameters["clientId"] = clientId

                val result = ScriptEngine.executeCalculation(schemeCode, clientId, fullParameters)
                call.respond(mapOf(
                    "calcSchemeCode" to schemeCode,
                    "clientId" to clientId,
                    "regionId" to regionId,
                    "output" to result
                ))
            }

            // Горячая перезагрузка скриптов
            post("/scripts/reload") {
                ScriptEngine.reload()
                call.respond(mapOf("status" to "reloaded"))
            }
        }
    }
}