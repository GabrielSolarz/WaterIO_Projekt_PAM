package com.pam.waterio

import android.content.Context
import android.content.SharedPreferences
import com.pam.waterio.data.TokenManager
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TokenManagerTest {

    @MockK
    lateinit var context: Context

    @MockK
    lateinit var sharedPrefs: SharedPreferences

    @MockK
    lateinit var editor: SharedPreferences.Editor

    private lateinit var tokenManager: TokenManager

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        every { context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE) } returns sharedPrefs
        every { sharedPrefs.edit() } returns editor
        every { editor.putString(any<String>(), any<String>()) } returns editor
        every { editor.putInt(any<String>(), any<Int>()) } returns editor
        every { editor.clear() } returns editor
        every { editor.apply() } just Runs

        tokenManager = TokenManager(context)
    }

    @Test
    fun `saveToken puts token into shared preferences`() {
        val token = "test-token"
        tokenManager.saveToken(token)
        verify { editor.putString("jwt_token", token) }
        verify { editor.apply() }
    }

    @Test
    fun `getToken retrieves token from shared preferences`() {
        val token = "test-token"
        every { sharedPrefs.getString("jwt_token", null) } returns token
        val result = tokenManager.getToken()
        assertEquals(token, result)
    }

    @Test
    fun `saveGoal puts goal into shared preferences`() {
        val goal = 2500
        tokenManager.saveGoal(goal)
        verify { editor.putInt("daily_goal", goal) }
        verify { editor.apply() }
    }

    @Test
    fun `getGoal retrieves goal or default value`() {
        every { sharedPrefs.getInt("daily_goal", 2000) } returns 2500
        val result = tokenManager.getGoal()
        assertEquals(2500, result)
    }

    @Test
    fun `clearSession clears all shared preferences`() {
        tokenManager.clearSession()
        verify { editor.clear() }
        verify { editor.apply() }
    }
}
