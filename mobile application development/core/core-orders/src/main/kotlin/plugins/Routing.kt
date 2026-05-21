package com.vt.plugins

import com.vt.dao.OrderDao
import com.vt.model.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        route("/api/v1/orders") {

            // Создать заявку
            post {
                val request = call.receive<CreateOrderRequest>()
                val order = OrderDao.create(request)
                call.respond(HttpStatusCode.Created, order)
            }

            // Получить заявку по ID
            get("/{ordId}") {
                val ordId = call.parameters["ordId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid order ID")
                val order = OrderDao.getById(ordId) ?: throw NoSuchElementException("Order not found")
                call.respond(order)
            }

            // Изменить статус
            patch("/{ordId}/status") {
                val ordId = call.parameters["ordId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid order ID")
                val request = call.receive<UpdateOrderStatusRequest>()
                val updated = OrderDao.updateStatus(ordId, request.statusCode)
                call.respond(updated)
            }

            // Изменить значение в order_data по пути
            patch("/{ordId}/order-data") {
                val ordId = call.parameters["ordId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid order ID")
                val request = call.receive<UpdateOrderDataRequest>()
                val updated = OrderDao.updateOrderData(ordId, request.path, request.value)
                call.respond(updated)
            }

            // Список заявок клиента с пагинацией и фильтрацией
            get("/clients/{clntId}") {
                val clntId = call.parameters["clntId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid client ID")
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
                val statusesParam = call.request.queryParameters["statuses"]
                val statuses = statusesParam?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }

                val result = OrderDao.getAllByClient(clntId, page, size, statuses)
                call.respond(result)
            }
        }
    }
}