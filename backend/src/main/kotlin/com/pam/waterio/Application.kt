package com.pam.waterio

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import java.util.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.mindrot.jbcrypt.BCrypt

@Serializable
data class WaterEntry(val id: String? = null, val amountMl: Int, val timestamp: Long? = null)

@Serializable
data class AuthRequest(val email: String, val password: String)

@Serializable
data class AuthResponse(val token: String)

@Serializable
data class DailyGoal(val goalMl: Int)

@Serializable
data class DailyStat(val date: String, val totalMl: Int)

val jwtSecret = "secret-key-waterio-2024" // W prawdziwym projekcie użyj zmiennej środowiskowej!
val jwtIssuer = "com.example.waterio"
val jwtAudience = "waterio-users"

fun main() {
    initDatabase()
    val port = System.getenv("PORT")?.toInt() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        })
    }

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "Access to water entries"
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                val email = credential.payload.getClaim("email").asString()
                val userId = credential.payload.getClaim("userId").asString()
                if (email != null && userId != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    configureRouting()
}

fun Application.configureRouting() {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()

    routing {
        get("/") { call.respondText("WaterIO Backend with Auth and Validation!") }

        post("/register") {
            val req = call.receive<AuthRequest>()

            if (!req.email.matches(emailRegex)) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid email format"))
                return@post
            }

            if (req.password.length < 6) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Password must be at least 6 characters long"))
                return@post
            }

            val userId = UUID.randomUUID().toString()
            val hashedPw = BCrypt.hashpw(req.password, BCrypt.gensalt())
            
            try {
                transaction {
                    UsersTable.insert {
                        it[id] = userId
                        it[email] = req.email
                        it[passwordHash] = hashedPw
                    }
                }
                call.respond(HttpStatusCode.Created, mapOf("status" to "User registered"))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.Conflict, mapOf("error" to "Email already exists or database error"))
            }
        }

        post("/login") {
            val req = call.receive<AuthRequest>()
            val user = transaction {
                UsersTable.selectAll().where { UsersTable.email eq req.email }.singleOrNull()
            }

            if (user != null && BCrypt.checkpw(req.password, user[UsersTable.passwordHash])) {
                val token = JWT.create()
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .withClaim("email", user[UsersTable.email])
                    .withClaim("userId", user[UsersTable.id])
                    .withExpiresAt(Date(System.currentTimeMillis() + 3600000 * 24)) // 24h
                    .sign(Algorithm.HMAC256(jwtSecret))
                call.respond(AuthResponse(token))
            } else {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
            }
        }

        authenticate("auth-jwt") {
            route("/water") {
                get {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!.payload.getClaim("userId").asString()
                    
                    val entries = transaction {
                        WaterEntriesTable.selectAll().where { WaterEntriesTable.userId eq userId }.map {
                            WaterEntry(it[WaterEntriesTable.id], it[WaterEntriesTable.amountMl], it[WaterEntriesTable.timestamp])
                        }
                    }
                    call.respond(entries)
                }

                post {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!.payload.getClaim("userId").asString()
                    val entry = call.receive<WaterEntry>()

                    if (entry.amountMl <= 0) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Amount must be greater than 0"))
                        return@post
                    }

                    val newId = UUID.randomUUID().toString()
                    
                    // Jeśli klient wysłał timestamp (synchronizacja offline), używamy go. 
                    // W przeciwnym razie nadajemy aktualny czas serwera.
                    val finalTimestamp = entry.timestamp ?: System.currentTimeMillis()

                    transaction {
                        WaterEntriesTable.insert {
                            it[id] = newId
                            it[WaterEntriesTable.userId] = userId
                            it[amountMl] = entry.amountMl
                            it[timestamp] = finalTimestamp
                        }
                    }
                    call.respond(HttpStatusCode.Created, WaterEntry(newId, entry.amountMl, finalTimestamp))
                }
                
                delete("/{id}") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!.payload.getClaim("userId").asString()
                    val idToDelete = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    
                    val deleted = transaction {
                        WaterEntriesTable.deleteWhere { (WaterEntriesTable.id eq idToDelete) and (WaterEntriesTable.userId eq userId) }
                    }
                    
                    if (deleted > 0) call.respond(mapOf("status" to "Deleted"))
                    else call.respond(HttpStatusCode.NotFound)
                }
            }

            route("/user/goal") {
                get {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!.payload.getClaim("userId").asString()
                    val user = transaction {
                        UsersTable.selectAll().where { UsersTable.id eq userId }.singleOrNull()
                    }
                    if (user != null) {
                        call.respond(DailyGoal(user[UsersTable.dailyGoalMl]))
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }

                post {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!.payload.getClaim("userId").asString()
                    val req = call.receive<DailyGoal>()
                    
                    if (req.goalMl <= 0) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Goal must be greater than 0"))
                        return@post
                    }

                    transaction {
                        UsersTable.update({ UsersTable.id eq userId }) {
                            it[dailyGoalMl] = req.goalMl
                        }
                    }
                    call.respond(HttpStatusCode.OK, req)
                }
            }

            get("/stats") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
                
                // Pobieramy dane z ostatnich 7 dni
                val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 3600 * 1000L)
                
                val stats = transaction {
                    WaterEntriesTable.selectAll()
                        .where { (WaterEntriesTable.userId eq userId) and (WaterEntriesTable.timestamp greaterEq sevenDaysAgo) }
                        .map { 
                            it[WaterEntriesTable.timestamp] to it[WaterEntriesTable.amountMl]
                        }
                }

                // Grupowanie w pamięci po datach (YYYY-MM-DD)
                val grouped = stats.groupBy { 
                    val date = java.time.Instant.ofEpochMilli(it.first)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                    date.toString()
                }.map { (date, entries) ->
                    DailyStat(date, entries.sumOf { it.second })
                }.sortedByDescending { it.date }

                call.respond(grouped)
            }

            get("/streak") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()

                val (goal, entries) = transaction {
                    val userGoal = UsersTable.selectAll().where { UsersTable.id eq userId }
                        .singleOrNull()?.get(UsersTable.dailyGoalMl) ?: 2000
                    
                    val waterEntries = WaterEntriesTable.selectAll()
                        .where { WaterEntriesTable.userId eq userId }
                        .map { it[WaterEntriesTable.timestamp] to it[WaterEntriesTable.amountMl] }
                    
                    userGoal to waterEntries
                }

                // Grupowanie po dniach
                val dailyTotals = entries.groupBy {
                    java.time.Instant.ofEpochMilli(it.first)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                }.mapValues { it.value.sumOf { entry -> entry.second } }

                var streak = 0
                val currentDate = java.time.LocalDate.now()

                // Sprawdzamy czy dzisiaj cel osiągnięty
                if ((dailyTotals[currentDate] ?: 0) >= goal) {
                    streak++
                }

                // Sprawdzamy dni wstecz od wczoraj
                var checkDate = currentDate.minusDays(1)
                while (dailyTotals.containsKey(checkDate) && dailyTotals[checkDate]!! >= goal) {
                    streak++
                    checkDate = checkDate.minusDays(1)
                }

                call.respond(mapOf("streak" to streak))
            }
        }
    }
}
