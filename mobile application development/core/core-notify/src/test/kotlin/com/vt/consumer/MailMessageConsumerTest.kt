package com.vt.consumer

import com.vt.model.MailMessage
import com.vt.model.RabbitConfig
import com.vt.model.RenderedMessage
import kotlinx.coroutines.test.runTest
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MailMessageConsumerTest {

    @Test
    fun `map report type returns template code`() {
        val consumer = consumer()

        assertEquals("REGISTRATION_SUCCESS", consumer.mapReportType("SUCCESSFULL_REGISTRATION_EMAIL"))
        assertNull(consumer.mapReportType("UNKNOWN"))
    }

    @Test
    fun `process message sends email when rendered channel is email`() = runTest {
        val reporting = FakeReportingGateway()
        val email = FakeEmailGateway()
        val consumer = consumer(reporting = reporting, email = email)

        consumer.processMessage(
            MailMessage(
                type = "report",
                reportType = "SUCCESSFULL_REGISTRATION_EMAIL",
                data = mapOf("email" to "user@example.com", "name" to "Test")
            )
        )

        assertEquals("REGISTRATION_SUCCESS", reporting.lastLookupCode)
        assertEquals("user@example.com", email.to)
        assertEquals("Rendered subject", email.subject)
        assertEquals("<b>Hello</b>", email.html)
    }

    @Test
    fun `process message skips email send when address is missing`() = runTest {
        val email = FakeEmailGateway()
        val consumer = consumer(email = email)

        consumer.processMessage(
            MailMessage(
                type = "report",
                reportType = "SUCCESSFULL_REGISTRATION_EMAIL",
                data = mapOf("name" to "Test")
            )
        )

        assertNull(email.to)
    }

    private fun consumer(
        reporting: FakeReportingGateway = FakeReportingGateway(),
        email: FakeEmailGateway = FakeEmailGateway()
    ) = MailMessageConsumer(
        cfg = RabbitConfig("localhost", 5672, "/", "guest", "guest", "reports.in"),
        reportingClient = reporting,
        emailService = email,
        pushService = FakePushGateway()
    )
}

private class FakeReportingGateway : ReportingGateway {
    var lastLookupCode: String? = null
    private val templateId = UUID.randomUUID()

    override suspend fun findTemplateId(code: String): UUID? {
        lastLookupCode = code
        return templateId
    }

    override suspend fun render(tmplId: UUID, variables: Map<String, Any?>): RenderedMessage {
        return RenderedMessage(channel = "EMAIL", subject = "Rendered subject", content = "<b>Hello</b>")
    }

    override fun close() = Unit
}

private class FakeEmailGateway : EmailGateway {
    var to: String? = null
    var subject: String? = null
    var html: String? = null

    override fun send(to: String, subject: String, html: String) {
        this.to = to
        this.subject = subject
        this.html = html
    }
}

private class FakePushGateway : PushGateway {
    override fun send(deviceToken: String, title: String, message: String) = Unit
}
