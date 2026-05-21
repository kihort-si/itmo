package com.vt.service

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class PasswordServiceTest {

    @Test
    fun `hash and verify round trip succeeds`() {
        val encoded = PasswordService.hash("strong-pass")

        assertTrue(PasswordService.verify("strong-pass", encoded))
        assertFalse(PasswordService.verify("wrong-pass", encoded))
    }

    @Test
    fun `hash rejects short password`() {
        assertFailsWith<IllegalArgumentException> {
            PasswordService.hash("short")
        }
    }

    @Test
    fun `verify returns false for malformed hash`() {
        assertFalse(PasswordService.verify("password", "not-a-valid-hash"))
    }
}
