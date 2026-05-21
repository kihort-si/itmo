package com.vt.plugins

import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RoutingTest {

    @Test
    fun `get portfolio rejects invalid port id`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            configureRouting()
        }

        val response = client.get("/api/v1/depository/portfolios/nope")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Invalid portId"))
    }

    @Test
    fun `rename portfolio rejects invalid port id`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            configureRouting()
        }

        val response = client.patch("/api/v1/depository/portfolios/nope")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Invalid portId"))
    }
}
