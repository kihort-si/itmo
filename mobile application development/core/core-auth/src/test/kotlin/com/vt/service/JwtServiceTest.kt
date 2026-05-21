package com.vt.service

import com.typesafe.config.ConfigFactory
import com.vt.model.AuthenticationException
import io.ktor.server.config.HoconApplicationConfig
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import java.util.UUID

class JwtServiceTest {

    @BeforeTest
    fun initService() {
        JwtService.init(
            HoconApplicationConfig(
                ConfigFactory.parseString(
                    """
                    ktor.jwt {
                      issuer = "test-issuer"
                      audience = "test-audience"
                      privateKeyPath = "classpath:keys/dev-private.pem"
                      publicKeyPath = "classpath:keys/dev-public.pem"
                      accessTtlMinutes = 15
                      refreshTtlDays = 30
                    }
                    """.trimIndent()
                )
            )
        )
    }

    @Test
    fun `create access token and extract principal`() {
        val sessionId = UUID.randomUUID()
        val token = JwtService.createAccessToken(
            userId = 42,
            email = "user@example.com",
            username = "tester",
            clntId = 7,
            roles = listOf("USER", "ADMIN"),
            sessionId = sessionId
        )

        val principal = JwtService.requirePrincipal("Bearer $token")

        assertEquals(42, principal.userId)
        assertEquals("user@example.com", principal.email)
        assertEquals("tester", principal.username)
        assertEquals(7, principal.clntId)
        assertEquals(sessionId, principal.sessionId)
        assertTrue("ADMIN" in principal.roles)
    }

    @Test
    fun `require principal rejects missing bearer token`() {
        assertFailsWith<AuthenticationException> {
            JwtService.requirePrincipal(null)
        }
    }
}
