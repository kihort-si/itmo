package com.vt.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TemplateEngineTest {

    @Test
    fun `render substitutes variables`() {
        val rendered = TemplateEngine.render(
            "Hello, {{name}}!",
            mapOf("name" to "Timur")
        )

        assertEquals("Hello, Timur!", rendered)
    }

    @Test
    fun `render handles sections and inverted sections`() {
        val rendered = TemplateEngine.render(
            "{{#premium}}Premium{{/premium}}{{^premium}}Basic{{/premium}} user",
            mapOf("premium" to false)
        )

        assertTrue(rendered.contains("Basic user"))
    }
}
