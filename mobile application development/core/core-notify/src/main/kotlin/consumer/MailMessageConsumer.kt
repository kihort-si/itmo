package com.vt.consumer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import com.vt.model.MailMessage
import com.vt.model.RenderedMessage
import com.vt.model.RabbitConfig
import com.vt.service.PushService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.util.UUID

interface ReportingGateway {
    suspend fun findTemplateId(code: String): UUID?
    suspend fun render(tmplId: UUID, variables: Map<String, Any?>): RenderedMessage
    fun close()
}

interface EmailGateway {
    fun send(to: String, subject: String, html: String)
}

interface PushGateway {
    fun send(deviceToken: String, title: String, message: String)
}

private object DefaultPushGateway : PushGateway {
    override fun send(deviceToken: String, title: String, message: String) {
        PushService.send(deviceToken, title, message)
    }
}

/**
 * Consumes [MailMessage] events from the `reports.in` RabbitMQ queue (vhost /reports).
 *
 * Message flow:
 *   core-worker → worker.mails exchange (vhost /worker)
 *     → RabbitMQ shovel → reports.in queue (vhost /reports)
 *       → MailMessageConsumer → REP render → EmailService / PushService
 *
 * reportType → REP template code mapping:
 *   SUCCESSFULL_REGISTRATION_EMAIL → REGISTRATION_SUCCESS
 *   FAILED_REGISTRATION_EMAIL      → REGISTRATION_FAILURE
 *   TRADE_EXECUTED_EMAIL           → TRADE_EXECUTED
 *   TRADE_EXECUTED_PUSH            → TRADE_EXECUTED_PUSH
 *   EOD_REPORT                     → EOD_REPORT
 */
class MailMessageConsumer(
    private val cfg: RabbitConfig,
    private val reportingClient: ReportingGateway,
    private val emailService: EmailGateway,
    private val pushService: PushGateway = DefaultPushGateway
) {
    private val logger = LoggerFactory.getLogger(MailMessageConsumer::class.java)
    private val mapper = jacksonObjectMapper()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var connection: Connection
    private lateinit var channel: Channel

    fun start() {
        val factory = ConnectionFactory().apply {
            host = cfg.host
            port = cfg.port
            virtualHost = cfg.vhost
            username = cfg.user
            password = cfg.password
        }
        connection = factory.newConnection()
        channel = connection.createChannel()

        val consumer = object : DefaultConsumer(channel) {
            override fun handleDelivery(
                consumerTag: String?,
                envelope: Envelope?,
                properties: AMQP.BasicProperties?,
                body: ByteArray?
            ) {
                body?.let {
                    val json = String(it)
                    scope.launch { processRawMessage(json) }
                }
            }
        }

        channel.basicConsume(cfg.queue, true, consumer)
        logger.info("Listening on queue={} vhost={}", cfg.queue, cfg.vhost)
    }

    fun shutdown() {
        runCatching { channel.close() }
        runCatching { connection.close() }
        runCatching { reportingClient.close() }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Internal
    // ──────────────────────────────────────────────────────────────────────

    internal suspend fun processRawMessage(json: String) {
        try {
            val msg = mapper.readValue<MailMessage>(json)
            processMessage(msg)
        } catch (ex: Exception) {
            logger.error("Failed to process mail message", ex)
        }
    }

    internal suspend fun processMessage(msg: MailMessage) {
        try {
            logger.debug("Received reportType={}", msg.reportType)

            val templateCode = mapReportType(msg.reportType) ?: run {
                logger.warn("No template mapping for reportType={}", msg.reportType)
                return
            }

            val tmplId = reportingClient.findTemplateId(templateCode) ?: run {
                logger.warn("Template not found in REP: code={}", templateCode)
                return
            }

            val rendered = reportingClient.render(tmplId, msg.data)

            when (rendered.channel.uppercase()) {
                "EMAIL" -> {
                    val to = msg.data["email"]?.toString() ?: run {
                        logger.warn("Missing 'email' field in data for reportType={}", msg.reportType)
                        return
                    }
                    emailService.send(to, rendered.subject ?: "(no subject)", rendered.content)
                }
                "PUSH" -> {
                    val token = msg.data["deviceToken"]?.toString() ?: run {
                        logger.warn("Missing 'deviceToken' field in data for reportType={}", msg.reportType)
                        return
                    }
                    pushService.send(token, rendered.subject ?: "", rendered.content)
                }
                else -> logger.warn("Unknown channel={} for reportType={}", rendered.channel, msg.reportType)
            }
        } catch (ex: Exception) {
            logger.error("Failed to process mail message", ex)
        }
    }

    internal fun mapReportType(reportType: String): String? = when (reportType) {
        "SUCCESSFULL_REGISTRATION_EMAIL" -> "REGISTRATION_SUCCESS"
        "FAILED_REGISTRATION_EMAIL"      -> "REGISTRATION_FAILURE"
        "TRADE_EXECUTED_EMAIL"           -> "TRADE_EXECUTED"
        "TRADE_EXECUTED_PUSH"            -> "TRADE_EXECUTED_PUSH"
        "EOD_REPORT"                     -> "EOD_REPORT"
        else                             -> null
    }
}
