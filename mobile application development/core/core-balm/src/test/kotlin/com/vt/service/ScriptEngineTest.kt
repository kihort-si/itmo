package com.vt.service

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ScriptEngineTest {

    @AfterTest
    fun clearCache() {
        ScriptEngine.clearCacheForTests()
    }

    @Test
    fun `execute calculation uses binding variables`() {
        ScriptEngine.registerScriptForTests(
            "CALC",
            """
            def total = new java.math.BigDecimal(parameters.amount.toString()) +
                new java.math.BigDecimal(parameters.fee.toString())
            return [clientId: clientId, result: total.toString()]
            """.trimIndent()
        )

        val result = ScriptEngine.executeCalculation("CALC", 77, mapOf("amount" to "10.5", "fee" to 2))

        assertEquals(77, result["clientId"])
        assertEquals("12.5", result["result"])
    }

    @Test
    fun `execute commission requires result fee`() {
        ScriptEngine.registerScriptForTests(
            "COMM",
            """return [fee: "10"]"""
        )

        assertFailsWith<IllegalStateException> {
            ScriptEngine.executeCommission("COMM", 7, emptyMap())
        }
    }
}
