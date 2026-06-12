package com.pam.waterio.data

import android.content.Context

class TokenManager(context: Context) {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) = prefs.edit().putString("jwt_token", token).apply()
    fun getToken(): String? = prefs.edit().let { prefs.getString("jwt_token", null) }
    fun clearSession() = prefs.edit().clear().apply()

    fun saveGoal(goal: Int) = prefs.edit().putInt("daily_goal", goal).apply()
    fun getGoal(): Int = prefs.getInt("daily_goal", 2000)
}