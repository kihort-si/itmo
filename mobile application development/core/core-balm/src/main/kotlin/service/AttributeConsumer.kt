package com.vt.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.rabbitmq.client.*
import com.vt.model.AttributeEvent
import com.vt.table.ClientAttributeTable
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.time.Instant

object AttributeConsumer {
    private val logger = LoggerFactory.getLogger(AttributeConsumer::class.java)
    private val mapper = jacksonObjectMapper()
    private lateinit var connection: Connection
    private lateinit var channel: Channel
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun start(host: String, port: Int, vhost: String, user: String, password: String, queueName: String) {
        val factory = ConnectionFactory().apply {
            this.host = host
            this.port = port
            virtualHost = vhost
            username = user
            this.password = password
        }
        connection = factory.newConnection()
        channel = connection.createChannel()
        // Предполагаем, что очередь уже объявлена, просто подписываемся
        val consumer = object : DefaultConsumer(channel) {
            override fun handleDelivery(
                consumerTag: String?,
                envelope: Envelope?,
                properties: AMQP.BasicProperties?,
                body: ByteArray?
            ) {
                body?.let {
                    val json = String(it)
                    scope.launch { processMessage(json) }
                }
            }
        }
        channel.basicConsume(queueName, true, consumer)
        logger.info("Started listening to queue: $queueName")
    }

    private suspend fun processMessage(json: String) {
        try {
            val event = mapper.readValue<AttributeEvent>(json)
            when (event.eventType) {
                "created" -> createAttribute(event)
                "updated" -> updateAttribute(event)
                "deleted" -> deleteAttribute(event)
                else -> logger.warn("Unknown event type: ${event.eventType}")
            }
        } catch (e: Exception) {
            logger.error("Failed to process attribute event", e)
        }
    }

    private fun createAttribute(event: AttributeEvent) = transaction {
        ClientAttributeTable.insert {
            it[clntId] = event.clientId
            it[attributeRefsId] = event.attributeId
            it[startDate] = event.startDate?.let { Instant.parse(it) } ?: Instant.now()
            it[endDate] = event.endDate?.let { Instant.parse(it) }
            it[value] = event.value
        }
    }

    private fun updateAttribute(event: AttributeEvent) = transaction {
        ClientAttributeTable.update({
            (ClientAttributeTable.clntId eq event.clientId) and
                    (ClientAttributeTable.attributeRefsId eq event.attributeId)
        }) {
            it[startDate] = event.startDate?.let { Instant.parse(it) } ?: Instant.now()
            it[endDate] = event.endDate?.let { Instant.parse(it) }
            it[value] = event.value
        }
    }

    private fun deleteAttribute(event: AttributeEvent) = transaction {
        ClientAttributeTable.deleteWhere {
            (ClientAttributeTable.clntId eq event.clientId) and
                    (ClientAttributeTable.attributeRefsId eq event.attributeId)
        }
    }

    fun shutdown() {
        scope.cancel()
        channel.close()
        connection.close()
    }
}