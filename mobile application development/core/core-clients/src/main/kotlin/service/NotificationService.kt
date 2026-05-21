package com.vt.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.rabbitmq.client.ConnectionFactory
import io.ktor.server.config.*
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.time.Instant

object NotificationService {
    private val logger = LoggerFactory.getLogger(NotificationService::class.java)
    private val mapper = ObjectMapper().apply {
        registerModule(JavaTimeModule())
        registerModule(kotlinModule())
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    private var connection: com.rabbitmq.client.Connection? = null
    private var exchangeName: String = ""
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun init(config: ApplicationConfig) {
        val enabled = config.propertyOrNull("notification.enabled")?.getString()?.toBoolean() ?: false
        if (!enabled) {
            logger.info("Notification service is disabled")
            return
        }

        exchangeName = config.property("notification.attribute_change_exchange").getString()
        val rabbitConfig = config.config("notification.rabbitmq")

        val factory = ConnectionFactory().apply {
            host = rabbitConfig.property("host").getString()
            port = rabbitConfig.property("port").getString().toInt()
            virtualHost = rabbitConfig.property("virtualHost").getString()
            username = rabbitConfig.property("username").getString()
            password = rabbitConfig.property("password").getString()
        }

        try {
            connection = factory.newConnection()
            logger.info("Connected to RabbitMQ, exchange: $exchangeName")
        } catch (e: Exception) {
            logger.error("Failed to connect to RabbitMQ", e)
            throw IllegalStateException("RabbitMQ connection failed", e)
        }
    }

    fun sendAttributeEvent(
        eventType: String,        // "created", "updated", "deleted"
        clientId: Int,
        attributeId: Int,
        value: String,
        startDate: Instant,
        endDate: Instant?
    ) {
        if (connection == null) {
            logger.warn("Notification service not initialized, skipping event")
            return
        }

        val exchange = exchangeName
        val routingKey = "clients.attribute.$eventType"

        val payload = mapOf(
            "eventType" to eventType,
            "clientId" to clientId,
            "attributeId" to attributeId,
            "value" to value,
            "startDate" to startDate.toString(),
            "endDate" to endDate?.toString(),
            "timestamp" to Instant.now().toString()
        )

        scope.launch {
            try {
                val json = mapper.writeValueAsBytes(payload)
                val channel = connection!!.createChannel()
                channel.use { ch ->
                    ch.basicPublish(exchange, routingKey, null, json)
                }
                logger.debug("Sent $eventType event for client $clientId, attribute $attributeId")
            } catch (e: Exception) {
                logger.error("Failed to send attribute event ($eventType) for client $clientId", e)
            }
        }
    }

    fun shutdown() {
        scope.cancel()
        connection?.close()
    }
}