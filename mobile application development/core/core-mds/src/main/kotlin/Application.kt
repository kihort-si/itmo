package com.vt

import com.vt.clickhouse.ClickHouseHolder
import com.vt.plugins.configureClickHouse
import com.vt.plugins.configureRouting
import com.vt.plugins.configureSerialization
import com.vt.plugins.configureStatusPages
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val dataSource = configureClickHouse()
    ClickHouseHolder.init(dataSource)
    configureSerialization()
    configureStatusPages()
    configureRouting()
}
