package com.vt.validation

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

object SchemaValidator {
    private val mapper = jacksonObjectMapper()
    private val allowedPrimitives = setOf("string", "integer", "boolean", "float")
    private val arrayPattern = Regex("^array\\[(string|integer|boolean|float)]$")

    fun validateSchema(schemaJson: String): Boolean {
        return try {
            val schema = mapper.readValue<Map<String, Any>>(schemaJson)
            validateSchemaNode(schema)
        } catch (e: Exception) {
            false
        }
    }

    private fun validateSchemaNode(node: Any?): Boolean {
        return when (node) {
            is Map<*, *> -> {
                node.all { (key, value) ->
                    key is String && validateSchemaNode(value)
                }
            }
            is String -> {
                allowedPrimitives.contains(node) || arrayPattern.matches(node)
            }
            else -> false
        }
    }

    fun validateDataAgainstSchema(dataJson: String, schemaJson: String) {
        val data = mapper.readValue<Map<String, Any>>(dataJson)
        val schema = mapper.readValue<Map<String, Any>>(schemaJson)
        validateDataNode(data, schema)
    }

    private fun validateDataNode(data: Any?, schema: Any?, path: String = "") {
        when (schema) {
            is Map<*, *> -> {
                if (data !is Map<*, *>) throw ValidationException("Expected object at $path")

                val schemaKeys = schema.keys.map { it.toString() }.toSet()
                val dataKeys = data.keys.map { it.toString() }.toSet()

                val missing = schemaKeys - dataKeys
                if (missing.isNotEmpty()) {
                    throw ValidationException("Missing required fields at $path: ${missing.joinToString()}")
                }

                val extra = dataKeys - schemaKeys
                if (extra.isNotEmpty()) {
                    throw ValidationException("Extra fields not allowed at $path: ${extra.joinToString()}")
                }

                for ((key, schemaValue) in schema) {
                    val dataValue = data[key]
                    validateDataNode(dataValue, schemaValue, "$path/$key")
                }
            }

            is String -> {
                validatePrimitiveOrArray(data, schema, path)
            }

            else -> throw ValidationException("Invalid schema node at $path")
        }
    }

    private fun validatePrimitiveOrArray(data: Any?, schemaType: String, path: String) {
        when {
            allowedPrimitives.contains(schemaType) -> {
                when (schemaType) {
                    "string" -> if (data !is String) throw ValidationException("Expected string at $path")
                    "integer" -> if (data !is Number || data.toDouble() != data.toInt().toDouble())
                        throw ValidationException("Expected integer at $path")
                    "boolean" -> if (data !is Boolean) throw ValidationException("Expected boolean at $path")
                    "float" -> if (data !is Number) throw ValidationException("Expected number at $path")
                }
            }

            arrayPattern.matches(schemaType) -> {
                val innerType = arrayPattern.find(schemaType)?.groupValues?.get(1)
                    ?: throw ValidationException("Invalid array type at $path")

                if (data !is List<*>) throw ValidationException("Expected array at $path")

                data.forEachIndexed { idx, item ->
                    validatePrimitiveOrArray(item, innerType, "$path[$idx]")
                }
            }

            else -> throw ValidationException("Unknown schema type $schemaType at $path")
        }
    }

    class ValidationException(message: String) : Exception(message)
}