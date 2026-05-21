package com.vt.service

import org.slf4j.LoggerFactory

object ProvisioningStubService {
    private val logger = LoggerFactory.getLogger(ProvisioningStubService::class.java)

    fun onUserRegistered(userId: Int, email: String) {
        logger.info("Provisioning stub invoked for userId={}, email={}", userId, email)
    }
}
