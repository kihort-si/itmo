package com.vt.dao

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.vt.model.*
import com.vt.table.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

object OrderDao {
    private val mapper = ObjectMapper()

    fun create(request: CreateOrderRequest): OrderResponse = transaction {
        val statusCreated = OrderStatusTable.select { OrderStatusTable.code eq "CREATED" }
            .singleOrNull()?.get(OrderStatusTable.ostId)
            ?: throw IllegalStateException("Initial status CREATED not found in DB")

        val ordId = OrderTable.insert {
            it[clntId] = request.clntId
            it[orderType] = request.orderType
            it[ostId] = statusCreated
            it[orderData] = request.orderData ?: mapper.createObjectNode()
        }[OrderTable.ordId]

        getById(ordId) ?: throw IllegalStateException("Failed to create order")
    }

    fun getById(ordId: Int): OrderResponse? = transaction {
        (OrderTable innerJoin OrderStatusTable)
            .select { OrderTable.ordId eq ordId }
            .singleOrNull()
            ?.let { row ->
                OrderResponse(
                    ordId = row[OrderTable.ordId],
                    clntId = row[OrderTable.clntId],
                    orderType = row[OrderTable.orderType],
                    status = row[OrderStatusTable.code],
                    dateStart = row[OrderTable.dateStart],
                    dateEnd = row[OrderTable.dateEnd],
                    orderData = row[OrderTable.orderData]
                )
            }
    }

    fun updateStatus(ordId: Int, statusCode: String): OrderResponse = transaction {
        val newStatus = OrderStatusTable.select { OrderStatusTable.code eq statusCode }
            .singleOrNull()?.get(OrderStatusTable.ostId)
            ?: throw IllegalArgumentException("Invalid status code: $statusCode")

        val updated = OrderTable.update({ OrderTable.ordId eq ordId }) {
            it[ostId] = newStatus
            if (statusCode in listOf("COMPLETED", "FAILED", "CANCELED")) {
                it[dateEnd] = Instant.now()
            }
        }
        if (updated == 0) throw NoSuchElementException("Order not found")

        getById(ordId) ?: throw IllegalStateException("Failed to retrieve updated order")
    }

    fun updateOrderData(ordId: Int, path: String, value: JsonNode): OrderResponse = transaction {
        val order = getById(ordId) ?: throw NoSuchElementException("Order not found")

        val root = order.orderData ?: mapper.createObjectNode()
        applyPath(root, path.split("."), value)

        OrderTable.update({ OrderTable.ordId eq ordId }) {
            it[orderData] = root
        }

        getById(ordId) ?: throw IllegalStateException("Failed to retrieve updated order")
    }

    fun getAllByClient(
        clntId: Int,
        page: Int,
        size: Int,
        statuses: List<String>? = null
    ): PaginatedOrdersResponse = transaction {
        val join = (OrderTable innerJoin OrderStatusTable)
            .selectAll()
            .where { OrderTable.clntId eq clntId }

        val filtered = if (!statuses.isNullOrEmpty()) {
            join.andWhere { OrderStatusTable.code inList statuses }
        } else join

        val total = filtered.count()
        val totalPages = if (total == 0L) 0 else ((total - 1) / size + 1).toInt()

        val resultRows = filtered
            .orderBy(OrderTable.dateStart to SortOrder.DESC)
            .limit(n = size, offset = ((page - 1) * size).toLong())
            .toList()

        val orders = resultRows.map { row: ResultRow ->
            OrderResponse(
                ordId = row[OrderTable.ordId],
                clntId = row[OrderTable.clntId],
                orderType = row[OrderTable.orderType],
                status = row[OrderStatusTable.code],
                dateStart = row[OrderTable.dateStart],
                dateEnd = row[OrderTable.dateEnd],
                orderData = row[OrderTable.orderData]
            )
        }

        PaginatedOrdersResponse(
            orders = orders,
            total = total,
            page = page,
            size = size,
            totalPages = totalPages
        )
    }

    private fun applyPath(current: JsonNode, segments: List<String>, value: JsonNode): JsonNode {
        if (segments.isEmpty()) return value

        val field = segments.first()
        val rest = segments.drop(1)
        val nextNode = if (current.has(field) && !current.get(field).isMissingNode) {
            current.get(field)
        } else {
            mapper.createObjectNode()
        }

        val child = applyPath(nextNode, rest, value)
        (current as ObjectNode).set<JsonNode>(field, child)
        return current
    }
}