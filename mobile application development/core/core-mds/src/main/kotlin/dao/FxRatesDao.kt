package com.vt.dao

import com.vt.clickhouse.ClickHouseHolder
import java.math.BigDecimal

/**
 * Reads FX rates from broker.fx_rates (seeded by Liquibase migration v2).
 * FINAL keyword ensures deduplication of the ReplacingMergeTree.
 */
object FxRatesDao {

    fun getRate(base: String, quote: String): BigDecimal? =
        ClickHouseHolder.withConnection { conn ->
            val sql = """
                SELECT rate
                FROM broker.fx_rates FINAL
                WHERE base = ? AND quote = ?
                LIMIT 1
            """.trimIndent()

            conn.prepareStatement(sql).use { ps ->
                ps.setString(1, base.uppercase())
                ps.setString(2, quote.uppercase())
                ps.executeQuery().use { rs ->
                    if (rs.next()) rs.getBigDecimal("rate") else null
                }
            }
        }
}
