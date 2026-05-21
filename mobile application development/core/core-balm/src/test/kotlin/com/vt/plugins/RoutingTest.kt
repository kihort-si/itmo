package com.vt.plugins

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RoutingTest {

    @Test
    fun `manual report send requires numeric hour`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            configureRouting()
        }

        val response = client.post("/api/v1/balm/reports/send?hour=abc")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Parameter 'hour' must be an integer"))
    }

    @Test
    fun `calculate requires calc scheme code`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            configureRouting()
        }

        val response = client.post("/api/v1/balm/calculate") {
            contentType(ContentType.Application.Json)
            setBody("""{"clientId":1,"regionId":1,"parameters":{"amount":10}}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("calcSchemeCode is required"))
    }
}
