package com.vt

import com.vt.client.ReportingClient
import com.vt.consumer.MailMessageConsumer
import com.vt.model.RabbitConfig
import com.vt.model.SmtpConfig
import com.vt.plugins.configureRouting
import com.vt.plugins.configureSerialization
import com.vt.plugins.configureStatusPages
import com.vt.service.EmailService
import com.vt.service.NotifyService
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val smtpCfg = environment.config.config("ktor.smtp").let { c ->
        SmtpConfig(
            host     = c.property("host").getString(),
            port     = c.property("port").getString().toInt(),
            user     = c.propertyOrNull("user")?.getString() ?: "",
            password = c.propertyOrNull("password")?.getString() ?: "",
            from     = c.property("from").getString(),
            tls      = c.propertyOrNull("tls")?.getString()?.toBoolean() ?: false
        )
    }

    val rabbitCfg = environment.config.config("ktor.rabbitmq").let { c ->
        RabbitConfig(
            host     = c.property("host").getString(),
            port     = c.property("port").getString().toInt(),
            vhost    = c.property("vhost").getString(),
            user     = c.property("user").getString(),
            password = c.property("password").getString(),
            queue    = c.property("queue").getString()
        )
    }

    val repUrl = environment.config.property("ktor.rep.url").getString()

    val emailService     = EmailService(smtpCfg)
    val notifyService    = NotifyService(emailService)
    val reportingClient  = ReportingClient(repUrl)
    val mailConsumer     = MailMessageConsumer(rabbitCfg, reportingClient, emailService)

    mailConsumer.start()

    monitor.subscribe(ApplicationStopping) {
        mailConsumer.shutdown()
    }

    configureSerialization()
    configureStatusPages()
    configureRouting(notifyService)
}
