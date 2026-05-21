package com.vt.dao

import com.vt.model.*
import com.vt.service.NotificationService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

object ClientAttributeDao {
    fun findByClntId(clntId: Int): List<AttributeResponse> = transaction {
        ClientAttributeTable.selectAll()
            .where { ClientAttributeTable.clntId eq clntId }
            .map { row ->
                AttributeResponse(
                    attributeClientId = row[ClientAttributeTable.clntAttrId],
                    attributeId = row[ClientAttributeTable.attributeRefsId],
                    startDate = row[ClientAttributeTable.startDate].atZone(java.time.ZoneId.of("UTC")).toInstant(),
                    endDate = row[ClientAttributeTable.endDate]?.atZone(java.time.ZoneId.of("UTC"))?.toInstant(),
                    value = row[ClientAttributeTable.value]
                )
            }
    }

    fun findByAttributeRefsId(clntId: Int, attributeRefsId: Int): AttributeResponse? = transaction {
        ClientAttributeTable.selectAll()
            .where { (ClientAttributeTable.clntId eq clntId) and (ClientAttributeTable.attributeRefsId eq attributeRefsId) }
            .map { row ->
                AttributeResponse(
                    attributeClientId = row[ClientAttributeTable.clntAttrId],
                    attributeId = row[ClientAttributeTable.attributeRefsId],
                    startDate = row[ClientAttributeTable.startDate].atZone(java.time.ZoneId.of("UTC")).toInstant(),
                    endDate = row[ClientAttributeTable.endDate]?.atZone(java.time.ZoneId.of("UTC"))?.toInstant(),
                    value = row[ClientAttributeTable.value]
                )
            }.singleOrNull()
    }

    fun create(clntId: Int, request: CreateAttributeRequest): AttributeResponse = transaction {
        val existing = ClientAttributeTable.selectAll()
            .where {
                (ClientAttributeTable.clntId eq clntId) and
                        (ClientAttributeTable.attributeRefsId eq request.attributeId)
            }
            .firstOrNull()

        if (existing != null) {
            throw IllegalStateException(
                "Attribute with attribute_refs_id ${request.attributeId} already exists for client $clntId"
            )
        }

        ClientAttributeTable.insert {
            it[ClientAttributeTable.clntId] = clntId
            it[attributeRefsId] = request.attributeId
            it[value] = request.value
            request.endDate?.let { end ->
                it[ClientAttributeTable.endDate] = end
            }
        }

        val created = findByAttributeRefsId(clntId, request.attributeId)
            ?: throw IllegalStateException("Failed to retrieve created attribute")

        // Отправляем уведомление создании
        notifyAttributeChange("created", clntId, created)
        created
    }

    fun updateEndDate(clntId: Int, attributeRefsId: Int, endDate: Instant): AttributeResponse = transaction {
        val attribute = ClientAttributeTable.selectAll()
            .where {
                (ClientAttributeTable.clntId eq clntId) and
                        (ClientAttributeTable.attributeRefsId eq attributeRefsId)
            }
            .firstOrNull() ?: throw NoSuchElementException("Attribute not found")

        val startDate = attribute[ClientAttributeTable.startDate]
        if (endDate < startDate) {
            throw IllegalArgumentException("end_date cannot be before start_date")
        }

        ClientAttributeTable.update({
            (ClientAttributeTable.clntId eq clntId) and
                    (ClientAttributeTable.attributeRefsId eq attributeRefsId)
        }) {
            it[ClientAttributeTable.endDate] = endDate
        }

        val updated = findByAttributeRefsId(clntId, attributeRefsId)
            ?: throw IllegalStateException("Failed to retrieve updated attribute")

        // Отправляем уведомление обновлении
        notifyAttributeChange("updated", clntId, updated)
        updated
    }

    fun delete(clntId: Int, attributeRefsId: Int): Boolean {
        // Получаем данные атрибута до удаления
        val existing = transaction {
            findByAttributeRefsId(clntId, attributeRefsId)
        } ?: throw NoSuchElementException("Attribute not found")

        val deleted = transaction {
            ClientAttributeTable.deleteWhere {
                (ClientAttributeTable.clntId eq clntId) and
                        (ClientAttributeTable.attributeRefsId eq attributeRefsId)
            } > 0
        }

        if (deleted) {
            // Отправляем уведомление удалении, используя сохранённые данные
            notifyAttributeChange("deleted", clntId, existing)
        }

        return deleted
    }
    private fun notifyAttributeChange(eventType: String, clntId: Int, attribute: AttributeResponse) {
        NotificationService.sendAttributeEvent(
            eventType = eventType,
            clientId = clntId,
            attributeId = attribute.attributeId,
            value = attribute.value,
            startDate = attribute.startDate,
            endDate = attribute.endDate
        )
    }
}