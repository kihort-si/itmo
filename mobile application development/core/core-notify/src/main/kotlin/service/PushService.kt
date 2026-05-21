package com.vt.service

import org.slf4j.LoggerFactory

/**
 * Push notification delivery.
 *
 * Currently implemented as a stub that logs the push payload.
 * To integrate with Firebase Cloud Messaging (FCM), replace the
 * log statement in [send] with an FCM HTTP v1 API call using
 * the device token from [deviceToken].
 *
 * FCM endpoint: POST https://fcm.googleapis.com/v1/projects/{project}/messages:send
 */
object PushService {

    private val logger = LoggerFactory.getLogger(PushService::class.java)

    fun send(deviceToken: String, title: String, message: String) {
        // TODO: replace with real FCM / APNs call when credentials are available
        logger.info(
            """
            [PUSH STUB]
              deviceToken : ${deviceToken.take(20)}...
              title       : $title
              message     : $message
            """.trimIndent()
        )
    }
}
