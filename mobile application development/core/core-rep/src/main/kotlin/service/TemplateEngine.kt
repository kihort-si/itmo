package com.vt.service

import com.github.mustachejava.DefaultMustacheFactory
import java.io.StringReader
import java.io.StringWriter

/**
 * Stateless Mustache rendering helper.
 *
 * Each call compiles the template string on the fly.
 * For a production service with heavy load, compiled Mustache objects
 * should be cached; for a demo with ~5 templates this is sufficient.
 */
object TemplateEngine {

    private val factory = DefaultMustacheFactory()

    /**
     * Render [templateBody] by substituting [variables].
     * Mustache syntax: {{variable}}, {{#section}}...{{/section}}, {{^section}}...{{/section}}.
     */
    fun render(templateBody: String, variables: Map<String, Any?>): String {
        val mustache = factory.compile(StringReader(templateBody), "inline")
        val writer = StringWriter()
        mustache.execute(writer, variables)
        return writer.toString()
    }
}
