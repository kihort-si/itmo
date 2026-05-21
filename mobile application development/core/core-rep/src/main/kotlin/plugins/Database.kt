package com.vt.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabase() {
    val config = environment.config.config("ktor.database")
    val hikariConfig = HikariConfig().apply {
        jdbcUrl             = config.property("url").getString()
        driverClassName     = config.property("driver").getString()
        username            = config.property("user").getString()
        password            = config.property("password").getString()
        maximumPoolSize     = 10
        isAutoCommit        = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
    }
    Database.connect(HikariDataSource(hikariConfig))
}
