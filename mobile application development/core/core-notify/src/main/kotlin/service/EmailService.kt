package com.vt.service

import com.vt.consumer.EmailGateway
import com.vt.model.SmtpConfig
import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import org.slf4j.LoggerFactory
import java.util.Date
import java.util.Properties

/**
 * Email delivery via Jakarta Mail (SMTP).
 *
 * If [SmtpConfig.isLogOnly] is true (SMTP_HOST is blank), the service
 * operates in log-only mode: emails are printed to the log and not sent.
 * This allows running the stack locally without an SMTP server.
 */
class EmailService(private val cfg: SmtpConfig) : EmailGateway {

    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    private val session: Session? by lazy {
        if (cfg.isLogOnly) {
            logger.warn("SMTP host is not configured — running in log-only mode")
            null
        } else {
            buildSession()
        }
    }

    override fun send(to: String, subject: String, html: String) {
        if (cfg.isLogOnly || session == null) {
            logger.info(
                """
                [EMAIL LOG-ONLY]
                  To      : $to
                  Subject : $subject
                  Body    : ${html.take(300)}${if (html.length > 300) "..." else ""}
                """.trimIndent()
            )
            return
        }

        try {
            val msg = MimeMessage(session).apply {
                setFrom(InternetAddress(cfg.from))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                this.subject = subject
                sentDate = Date()

                val htmlPart = MimeBodyPart().apply { setContent(html, "text/html; charset=UTF-8") }
                setContent(MimeMultipart("alternative").apply { addBodyPart(htmlPart) })
            }
            Transport.send(msg)
            logger.info("Email sent to={} subject={}", to, subject)
        } catch (ex: Exception) {
            logger.error("Failed to send email to={} subject={}", to, subject, ex)
            // Do not rethrow — caller already returned 202; log and continue
        }
    }

    // ─── Internal ─────────────────────────────────────────────────────────

    private fun buildSession(): Session {
        val props = Properties().apply {
            put("mail.smtp.host", cfg.host)
            put("mail.smtp.port", cfg.port.toString())
            put("mail.smtp.auth", (cfg.user.isNotBlank()).toString())
            if (cfg.tls) {
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.starttls.required", "true")
            }
            put("mail.smtp.connectiontimeout", "10000")
            put("mail.smtp.timeout", "10000")
        }

        return if (cfg.user.isNotBlank()) {
            Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication() =
                    PasswordAuthentication(cfg.user, cfg.password)
            })
        } else {
            Session.getInstance(props)
        }
    }
}
