package com.vt.service

import groovy.lang.Binding
import groovy.lang.GroovyShell
import groovy.lang.Script
import org.codehaus.groovy.control.CompilerConfiguration
import com.vt.table.SchemeScriptTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.math.BigDecimal

object ScriptEngine {
    private val logger = LoggerFactory.getLogger(ScriptEngine::class.java)
    private val scriptCache = ConcurrentHashMap<String, Class<*>>()
    private val config = CompilerConfiguration() // без SecureASTCustomizer для простоты

    fun loadAll() {
        val scripts = transaction {
            SchemeScriptTable.selectAll()
                .where { SchemeScriptTable.enabled eq true }
                .map { it[SchemeScriptTable.code] to it[SchemeScriptTable.script] }
        }
        scripts.forEach { (code, src) ->
            compileAndCache(code, src)
        }
        logger.info("Loaded {} scripts into cache", scriptCache.size)
    }

    fun reload() {
        scriptCache.clear()
        loadAll()
    }

    internal fun registerScriptForTests(code: String, scriptText: String) {
        compileAndCache(code, scriptText)
    }

    internal fun clearCacheForTests() {
        scriptCache.clear()
    }

    fun executeCalculation(schemeCode: String, clientId: Int, parameters: Map<String, Any>): Map<String, Any> {
        val scriptClass = scriptCache[schemeCode]
            ?: throw NoSuchElementException("Calculation scheme not found: $schemeCode")

        val binding = Binding().apply {
            setVariable("clientId", clientId)
            setVariable("parameters", parameters)
            setVariable("commissionService", CommissionServiceProxy)
            setVariable("ctx", BalmContextImpl())
            setVariable("bigDecimal", { value: Any -> BigDecimal(value.toString()) })
        }

        val script = scriptClass.getDeclaredConstructor().newInstance() as Script
        script.binding = binding
        val result = script.run()
        return result as Map<String, Any>
    }

    fun executeCommission(commissionCode: String, clientId: Int, parameters: Map<String, Any>): Map<String, Any> {
        val scriptClass = scriptCache[commissionCode]
            ?: throw NoSuchElementException("Commission scheme not found: $commissionCode")

        val binding = Binding().apply {
            setVariable("clientId", clientId)
            setVariable("parameters", parameters)
            setVariable("ctx", BalmContextImpl())
            setVariable("bigDecimal", { value: Any -> BigDecimal(value.toString()) })
        }

        val script = scriptClass.getDeclaredConstructor().newInstance() as Script
        script.binding = binding
        val result = script.run() as Map<String, Any>
        if (!result.containsKey("resultFee")) throw IllegalStateException("Commission script must return 'resultFee'")
        return result
    }

    private fun compileAndCache(code: String, scriptText: String) {
        try {
            val shell = GroovyShell(config)
            val parsed = shell.parse(scriptText)
            scriptCache[code] = parsed.javaClass
            logger.debug("Compiled script '{}'", code)
        } catch (e: Exception) {
            logger.error("Failed to compile script '$code'", e)
            throw e
        }
    }
}

object CommissionServiceProxy {
    fun calculate(code: String, clientId: Int, parameters: Map<String, Any>): Map<String, Any> {
        return ScriptEngine.executeCommission(code, clientId, parameters)
    }
}
