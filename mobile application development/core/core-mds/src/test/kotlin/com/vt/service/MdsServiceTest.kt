package com.vt.service

import com.vt.model.Period
import com.vt.model.SortBy
import com.vt.model.SortOrder
import com.vt.model.StockRow
import java.math.BigDecimal
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MdsServiceTest {

    @AfterTest
    fun resetLoaders() {
        MdsService.stocksLoader = { emptyList() }
        MdsService.fxRateLoader = { _, _ -> null }
    }

    @Test
    fun `get stocks filters by search and sorts null prices last`() {
        MdsService.stocksLoader = {
            listOf(
                StockRow("SBER", "Sberbank", "RUB", BigDecimal("300.00"), 1.2),
                StockRow("GAZP", "Gazprom", "RUB", BigDecimal("150.00"), -0.4),
                StockRow("GMKN", "Norilsk", "RUB", null, 0.1)
            )
        }

        val result = MdsService.getStocks(
            search = "g",
            sortBy = SortBy.PRICE,
            sortOrder = SortOrder.ASC,
            minPrice = null,
            maxPrice = null,
            minDayChangePct = null,
            maxDayChangePct = null
        )

        assertEquals(listOf("GAZP", "GMKN"), result.map { it.ticker })
    }

    @Test
    fun `get fx rate normalizes case and returns one for identical currencies`() {
        MdsService.fxRateLoader = { _, _ -> BigDecimal("91.23") }

        val identity = MdsService.getFxRate("usd", "USD")
        val pair = MdsService.getFxRate("usd", "rub")

        assertEquals(BigDecimal.ONE, identity.rate)
        assertEquals("USD", pair.base)
        assertEquals("RUB", pair.quote)
        assertEquals(BigDecimal("91.23"), pair.rate)
    }

    @Test
    fun `get fx rate fails when pair is missing`() {
        MdsService.fxRateLoader = { _, _ -> null }

        assertFailsWith<NoSuchElementException> {
            MdsService.getFxRate("usd", "eur")
        }
    }

    @Test
    fun `period parser accepts api names case insensitively`() {
        assertEquals(Period.P1M, Period.fromApi("1m"))
    }
}
