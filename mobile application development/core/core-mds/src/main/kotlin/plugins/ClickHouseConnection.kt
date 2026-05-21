package com.vt.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import javax.sql.DataSource

fun Application.configureClickHouse(): DataSource {
    val config = environment.config.config("ktor.clickhouse")
    val hikariConfig = HikariConfig().apply {
        jdbcUrl         = config.property("url").getString()
        driverClassName = "com.clickhouse.jdbc.ClickHouseDriver"
        username        = config.property("user").getString()
        password        = config.property("password").getString()
        maximumPoolSize = 5
        isAutoCommit    = true          // ClickHouse does not support transactions
        connectionTestQuery  = "SELECT 1"
        connectionTimeout    = 30_000
        idleTimeout          = 300_000
        maxLifetime          = 900_000
        validationTimeout    = 5_000
    }
    return HikariDataSource(hikariConfig)
}
