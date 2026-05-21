package com.vt

import com.vt.plugins.*
import com.vt.service.AttributeConsumer
import com.vt.service.ScheduledReportService
import com.vt.service.ScriptEngine
import com.vt.service.SyncService
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val logger = LoggerFactory.getLogger(Application::class.java)

    configureDatabase()
    configureSerialization()
    configureStatusPages()

    val rabbitConfig = environment.config.config("rabbitmq")
    val queueName = environment.config.property("application.attribute_change_ntf_queue").getString()
    AttributeConsumer.start(
        host = rabbitConfig.property("host").getString(),
        port = rabbitConfig.property("port").getString().toInt(),
        vhost = rabbitConfig.property("virtualHost").getString(),
        user = rabbitConfig.property("username").getString(),
        password = rabbitConfig.property("password").getString(),
        queueName = queueName
    )

    val gatewayUrl = environment.config.property("application.api_gateway_url").getString()
    SyncService.init(gatewayUrl)

    val refsSyncRequired = environment.config.propertyOrNull("application.refs_sync_required")
        ?.getString()?.toBoolean() ?: false

    if (refsSyncRequired) {
        try {
            SyncService.syncCurrencyTable()
            SyncService.syncBillDetailsTable()
        } catch (e: Exception) {
            logger.error("REFS synchronization failed", e)
            AttributeConsumer.shutdown()
            throw RuntimeException("REFS synchronization failed")
        }
    } else {
        SyncService.startAsyncSync()
    }

    val reportExchange = environment.config.property("application.report_request_exchange").getString()
    ScheduledReportService.init(
        exchange = reportExchange,
        host = rabbitConfig.property("host").getString(),
        port = rabbitConfig.property("port").getString().toInt(),
        vhost = rabbitConfig.property("virtualHost").getString(),
        user = rabbitConfig.property("username").getString(),
        password = rabbitConfig.property("password").getString()
    )

    val reportSchedulingEnabled = environment.config.propertyOrNull("application.report_scheduling_enabled")
        ?.getString()?.toBoolean() ?: false

    if(reportSchedulingEnabled) ScheduledReportService.startScheduler()
    else logger.warn("Report scheduling disabled")

    ScriptEngine.loadAll()

    configureRouting()
}