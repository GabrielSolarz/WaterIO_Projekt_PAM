package com.example.waterio.network

import retrofit2.http.*

data class LoginRequest(val email: String, val password: String)
data class AuthResponse(val token: String)
data class WaterRequest(val amount_ml: Int)

interface WaterApiService {
    @POST("/api/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("/api/water")
    suspend fun addWater(@Header("Authorization") token: String, @Body request: WaterRequest)

    @GET("/api/water/today")
    suspend fun getTodayWater(@Header("Authorization") token: String): Map<String, Int>
}