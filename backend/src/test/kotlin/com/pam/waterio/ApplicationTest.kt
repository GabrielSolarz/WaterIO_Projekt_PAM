package com.pam.waterio

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.*
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testRegisterAndLogin() = testApplication {
        application {
            initDatabase("jdbc:h2:mem:test_reg;DB_CLOSE_DELAY=-1")
            module()
        }

        // 1. Register
        val regResponse = client.post("/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(AuthRequest.serializer(), AuthRequest("test@example.com", "password123")))
        }
        assertEquals(HttpStatusCode.Created, regResponse.status)

        // 2. Login
        val loginResponse = client.post("/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(AuthRequest.serializer(), AuthRequest("test@example.com", "password123")))
        }
        assertEquals(HttpStatusCode.OK, loginResponse.status)
        val body = loginResponse.bodyAsText()
        assertTrue(body.contains("token"))
    }

    @Test
    fun testLoginFail() = testApplication {
        application {
            initDatabase("jdbc:h2:mem:test_fail;DB_CLOSE_DELAY=-1")
            module()
        }

        // Register first
        client.post("/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(AuthRequest.serializer(), AuthRequest("fail@example.com", "password123")))
        }

        // Login with wrong password
        val loginResponse = client.post("/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(AuthRequest.serializer(), AuthRequest("fail@example.com", "wrong_password")))
        }
        assertEquals(HttpStatusCode.Unauthorized, loginResponse.status)
    }

    @Test
    fun testUnauthorizedAccess() = testApplication {
        application {
            initDatabase("jdbc:h2:mem:test_unauth;DB_CLOSE_DELAY=-1")
            module()
        }

        val response = client.get("/water")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun testAddWaterEntry() = testApplication {
        application {
            initDatabase("jdbc:h2:mem:test_water;DB_CLOSE_DELAY=-1")
            module()
        }

        // 1. Register & Login to get token
        client.post("/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(AuthRequest.serializer(), AuthRequest("user@example.com", "pass")))
        }
        val loginResponse = client.post("/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(AuthRequest.serializer(), AuthRequest("user@example.com", "pass")))
        }
        val token = json.decodeFromString<AuthResponse>(loginResponse.bodyAsText()).token

        // 2. Add water
        val addResponse = client.post("/water") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(WaterEntry.serializer(), WaterEntry(amountMl = 250)))
        }
        assertEquals(HttpStatusCode.Created, addResponse.status)
        
        // 3. Verify it's there
        val getResponse = client.get("/water") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        val entries = json.decodeFromString<List<WaterEntry>>(getResponse.bodyAsText())
        assertEquals(1, entries.size)
        assertEquals(250, entries[0].amountMl)
    }

    @Test
    fun testGetStats() = testApplication {
        application {
            initDatabase("jdbc:h2:mem:test_stats;DB_CLOSE_DELAY=-1")
            module()
        }

        // 1. Get token
        client.post("/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(AuthRequest.serializer(), AuthRequest("stats@example.com", "pass")))
        }
        val loginResponse = client.post("/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(AuthRequest.serializer(), AuthRequest("stats@example.com", "pass")))
        }
        val token = json.decodeFromString<AuthResponse>(loginResponse.bodyAsText()).token

        // 2. Add some water
        client.post("/water") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(WaterEntry.serializer(), WaterEntry(amountMl = 500)))
        }

        // 3. Check stats
        val statsResponse = client.get("/stats") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, statsResponse.status)
        val stats = json.decodeFromString<List<DailyStat>>(statsResponse.bodyAsText())
        assertTrue(stats.isNotEmpty())
        assertEquals(500, stats[0].totalMl)
    }
}
