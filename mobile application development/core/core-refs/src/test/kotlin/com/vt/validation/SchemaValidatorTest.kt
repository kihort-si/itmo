package com.vt.validation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class SchemaValidatorTest {

    @Test
    fun `validate schema accepts nested objects and arrays`() {
        val valid = """
            {
              "name": "string",
              "flags": "array[boolean]",
              "meta": {
                "count": "integer",
                "ratio": "float"
              }
            }
        """.trimIndent()

        assertTrue(SchemaValidator.validateSchema(valid))
    }

    @Test
    fun `validate schema rejects unsupported primitive`() {
        val invalid = """{"createdAt":"date"}"""

        assertFalse(SchemaValidator.validateSchema(invalid))
    }

    @Test
    fun `validate data against schema accepts matching payload`() {
        SchemaValidator.validateDataAgainstSchema(
            """{"name":"bond","flags":[true,false],"meta":{"count":3,"ratio":1.5}}""",
            """{"name":"string","flags":"array[boolean]","meta":{"count":"integer","ratio":"float"}}"""
        )
    }

    @Test
    fun `validate data against schema rejects extra field`() {
        val error = assertFailsWith<SchemaValidator.ValidationException> {
            SchemaValidator.validateDataAgainstSchema(
                """{"name":"bond","extra":"boom"}""",
                """{"name":"string"}"""
            )
        }

        assertTrue(error.message!!.contains("Extra fields not allowed"))
    }
}
