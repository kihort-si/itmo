// ============================================================
// File: com/vt/plugins/Routing.kt
// ============================================================
package com.vt.plugins

import com.vt.dao.PortfolioDao
import com.vt.dao.PositionDao
import com.vt.model.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        route("/api/v1/depository") {

            // Создать портфель
            post("/portfolios") {
                val request = call.receive<CreatePortfolioRequest>()
                val portfolio = PortfolioDao.create(request)
                call.respond(HttpStatusCode.Created, portfolio)
            }

            // Изменить имя портфеля
            patch("/portfolios/{portId}") {
                val portId = call.parameters["portId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid portId")
                val request = call.receive<UpdatePortfolioRequest>()
                val updated = PortfolioDao.updateName(portId, request.name)
                call.respond(updated)
            }

            // Провести операцию
            post("/portfolios/{portId}/operations") {
                val portId = call.parameters["portId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid portId")
                val request = call.receive<OperationRequest>()
                val result = PositionDao.executeOperation(portId, request)
                call.respond(HttpStatusCode.Created, result)
            }

            // Просмотр портфеля
            get("/portfolios/{portId}") {
                val portId = call.parameters["portId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid portId")
                val portfolio = PortfolioDao.getById(portId) ?: throw NoSuchElementException("Portfolio not found")
                call.respond(portfolio)
            }

            // Список портфелей клиента
            get("/clients/{clntId}/portfolios") {
                val clntId = call.parameters["clntId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid clientId")
                val portfolios = PortfolioDao.getByClientId(clntId)
                call.respond(portfolios)
            }

            post("/portfolios/{portId}/close") {
                val portId = call.parameters["portId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid portId")
                val closed = PortfolioDao.closePortfolio(portId)
                call.respond(closed)
            }
        }
    }
}