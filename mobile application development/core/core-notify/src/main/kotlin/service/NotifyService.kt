package com.vt.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

/**
 * Orchestrates email and push delivery for the HTTP API endpoints.
 *
 * Each send call launches a fire-and-forget coroutine so the HTTP handler
 * can return 202 Accepted immediately. Delivery failures are logged but
 * do not surface to the caller.
 */
class NotifyService(
    private val emailService: EmailService
) {
    private val logger = LoggerFactory.getLogger(NotifyService::class.java)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun sendEmail(to: String, subject: String, html: String) {
        scope.launch {
            try {
                emailService.send(to, subject, html)
            } catch (ex: Exception) {
                logger.error("Async email delivery failed to={}", to, ex)
            }
        }
    }

    fun sendPush(deviceToken: String, title: String, message: String) {
        scope.launch {
            try {
                PushService.send(deviceToken, title, message)
            } catch (ex: Exception) {
                logger.error("Async push delivery failed token={}", deviceToken.take(20), ex)
            }
        }
    }
}
