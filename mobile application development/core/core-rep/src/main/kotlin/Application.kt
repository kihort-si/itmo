package com.vt

import com.vt.plugins.configureDatabase
import com.vt.plugins.configureRouting
import com.vt.plugins.configureSerialization
import com.vt.plugins.configureStatusPages
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureDatabase()
    configureSerialization()
    configureStatusPages()
    configureRouting()
}
