package com.vt.clickhouse

import java.sql.Connection
import javax.sql.DataSource

/**
 * Singleton holder for the ClickHouse DataSource.
 * Initialized once in Application.module() before any DAO calls.
 */
object ClickHouseHolder {

    private lateinit var dataSource: DataSource

    fun init(ds: DataSource) {
        dataSource = ds
    }

    fun <T> withConnection(block: (Connection) -> T): T =
        dataSource.connection.use(block)
}
