package com.pam.waterio.network

import retrofit2.Response
import retrofit2.http.*

data class AuthRequest(val email: String, val password: String)
data class AuthResponse(val token: String)
data class RegisterResponse(val status: String? = null, val error: String? = null)
data class WaterNetworkEntry(val id: String?, val amountMl: Int, val timestamp: Long?)
data class DailyGoal(val goalMl: Int)
data class DailyStat(val date: String, val totalMl: Int)
data class StreakResponse(val streak: Int)

interface WaterApiService {
    @POST("/register")
    suspend fun register(@Body request: AuthRequest): Response<RegisterResponse>

    @POST("/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @GET("/water")
    suspend fun getHistory(@Header("Authorization") token: String): List<WaterNetworkEntry>

    @POST("/water")
    suspend fun addWater(@Header("Authorization") token: String, @Body request: WaterNetworkEntry): WaterNetworkEntry

    @DELETE("/water/{id}")
    suspend fun deleteWater(@Header("Authorization") token: String, @Path("id") id: String): Response<Unit>

    @GET("/user/goal")
    suspend fun getGoal(@Header("Authorization") token: String): DailyGoal

    @POST("/user/goal")
    suspend fun updateGoal(@Header("Authorization") token: String, @Body goal: DailyGoal): DailyGoal

    @GET("/stats")
    suspend fun getStats(@Header("Authorization") token: String): List<DailyStat>

    @GET("/streak")
    suspend fun getStreak(@Header("Authorization") token: String): StreakResponse
}