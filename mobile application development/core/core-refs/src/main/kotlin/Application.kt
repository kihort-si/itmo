package com.vt

import io.ktor.server.netty.*
import com.vt.plugins.*
import io.ktor.server.application.Application

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureDatabase()
    configureSerialization()
    configureStatusPages()
    configureRouting()
}