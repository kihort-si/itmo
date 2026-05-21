package com.vt.plugins

import com.vt.model.StockRow
import com.vt.service.MdsService
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import java.math.BigDecimal
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RoutingTest {

    @AfterTest
    fun resetLoaders() {
        MdsService.stocksLoader = { emptyList() }
    }

    @Test
    fun `stocks route applies query handling and returns payload`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            configureRouting()
        }
        MdsService.stocksLoader = {
            listOf(
                StockRow("SBER", "Sberbank", "RUB", BigDecimal("300.00"), 1.2),
                StockRow("GAZP", "Gazprom", "RUB", BigDecimal("150.00"), -0.4)
            )
        }

        val response = client.get("/v1/stocks?search=gaz&sortBy=PRICE&sortOrder=DESC")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("GAZP"))
        assertTrue(body.contains("Gazprom"))
    }

    @Test
    fun `stocks route rejects invalid sort order`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            configureRouting()
        }

        val response = client.get("/v1/stocks?sortOrder=sideways")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Unknown sortOrder"))
    }
}
