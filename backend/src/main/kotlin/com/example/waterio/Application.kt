package com.example.waterio

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class WaterEntry(
    val id: String? = null,
    val amountMl: Int,
    val timestamp: Long? = null
)

val waterEntries = mutableListOf<WaterEntry>()

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    configureRouting()
}

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("WaterIO Backend is running!")
        }
        
        get("/health") {
            call.respond(mapOf("status" to "OK"))
        }

        route("/water") {
            get {
                call.respond(waterEntries)
            }
            
            post {
                try {
                    val entry = call.receive<WaterEntry>()
                    val newEntry = entry.copy(
                        id = UUID.randomUUID().toString(), 
                        timestamp = System.currentTimeMillis()
                    )
                    waterEntries.add(newEntry)
                    call.respond(HttpStatusCode.Created, newEntry)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            delete("/{id}") {
                val id = call.parameters["id"]
                val removed = waterEntries.removeIf { it.id == id }
                if (removed) {
                    call.respond(HttpStatusCode.OK, mapOf("status" to "Deleted"))
                } else {
                    call.respondText("Not Found", status = HttpStatusCode.NotFound)
                }
            }
        }
    }
}
