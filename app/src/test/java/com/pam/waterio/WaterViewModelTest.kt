package com.pam.waterio

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.pam.waterio.data.TokenManager
import com.pam.waterio.data.WaterDao
import com.pam.waterio.data.WaterEntry
import com.pam.waterio.network.*
import com.pam.waterio.ui.AuthState
import com.pam.waterio.ui.WaterViewModel
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class WaterViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    lateinit var dao: WaterDao

    @MockK
    lateinit var api: WaterApiService

    @MockK
    lateinit var tokenManager: TokenManager

    @MockK(relaxed = true)
    lateinit var context: Context

    @MockK
    lateinit var workManager: WorkManager

    @MockK
    lateinit var alarmManager: AlarmManager

    private lateinit var viewModel: WaterViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        // Mock WorkManager - try both static and companion just in case
        mockkStatic(WorkManager::class)
        try {
            mockkObject(WorkManager.Companion)
        } catch (e: Exception) {}

        every { WorkManager.getInstance(any<Context>()) } returns workManager
        every { workManager.enqueueUniqueWork(any<String>(), any<ExistingWorkPolicy>(), any<OneTimeWorkRequest>()) } returns mockk<androidx.work.Operation>(relaxed = true)

        // Mock Context
        every { context.getApplicationContext() } returns context
        every { context.getSystemService(Context.ALARM_SERVICE) } returns alarmManager
        every { alarmManager.setExactAndAllowWhileIdle(any<Int>(), any<Long>(), any<PendingIntent>()) } just Runs
        every { alarmManager.setAndAllowWhileIdle(any<Int>(), any<Long>(), any<PendingIntent>()) } just Runs
        // Mock for PendingIntent
        mockkStatic(PendingIntent::class)
        every { PendingIntent.getBroadcast(any<Context>(), any<Int>(), any<Intent>(), any<Int>()) } returns mockk<PendingIntent>()

        every { tokenManager.getToken() } returns null
        every { tokenManager.getGoal() } returns 2000

        viewModel = WaterViewModel(dao, api, tokenManager, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `init refreshes data if token exists`() = runTest {
        val token = "existing-token"
        every { tokenManager.getToken() } returns token
        
        // Mocks for refreshData
        coEvery { api.getGoal(any<String>()) } returns DailyGoal(2000)
        every { tokenManager.saveGoal(any<Int>()) } just Runs
        coEvery { api.getHistory(any<String>()) } returns emptyList()
        coEvery { api.getStreak(any<String>()) } returns StreakResponse(3)
        coEvery { api.getStats(any<String>()) } returns emptyList()
        coEvery { dao.getAllEntries() } returns emptyList()

        // Create a new ViewModel instance to trigger init
        val newViewModel = WaterViewModel(dao, api, tokenManager, context)
        
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AuthState.SUCCESS, newViewModel.authState.value)
        assertEquals(3, newViewModel.streak.value)
    }

    @Test
    fun `login success updates authState and token`() = runTest {
        val email = "test@example.com"
        val password = "password"
        val token = "jwt-token"
        val response = Response.success(AuthResponse(token))

        coEvery { api.login(AuthRequest(email, password)) } returns response
        every { tokenManager.saveToken(token) } just Runs
        every { tokenManager.getToken() } returns token
        
        // Mocks for refreshData
        coEvery { api.getGoal(any<String>()) } returns DailyGoal(2500)
        every { tokenManager.saveGoal(2500) } just Runs
        coEvery { api.getHistory(any<String>()) } returns emptyList()
        coEvery { api.getStreak(any<String>()) } returns StreakResponse(5)
        coEvery { api.getStats(any<String>()) } returns emptyList()
        coEvery { dao.getAllEntries() } returns emptyList()

        viewModel.login(email, password)
        
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AuthState.SUCCESS, viewModel.authState.value)
        verify { tokenManager.saveToken(token) }
    }

    @Test
    fun `login failure sets error message`() = runTest {
        val email = "test@example.com"
        val password = "wrong"
        val response = Response.error<AuthResponse>(401, mockk<okhttp3.ResponseBody>(relaxed = true))

        coEvery { api.login(AuthRequest(email, password)) } returns response

        viewModel.login(email, password)
        
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AuthState.UNAUTHENTICATED, viewModel.authState.value)
        assertEquals("Błędne dane logowania!", viewModel.errorMessage.value)
    }

    @Test
    fun `logout clears token and updates authState`() {
        every { tokenManager.clearSession() } just Runs

        viewModel.logout()

        assertEquals(AuthState.UNAUTHENTICATED, viewModel.authState.value)
        verify { tokenManager.clearSession() }
    }

    @Test
    fun `addWater inserts into dao and calls api when token exists`() = runTest {
        val amount = 250
        val token = "jwt-token"
        every { tokenManager.getToken() } returns token
        coEvery { dao.insert(any<WaterEntry>()) } just Runs
        coEvery { dao.getAllEntries() } returns emptyList()
        coEvery { api.addWater(any<String>(), any<WaterNetworkEntry>()) } returns WaterNetworkEntry("remote-id", amount, 123456789L)
        coEvery { dao.deletePermanently(any<String>()) } just Runs
        
        // Mocks for refreshData
        coEvery { api.getGoal(any<String>()) } returns DailyGoal(2000)
        every { tokenManager.saveGoal(any<Int>()) } just Runs
        coEvery { api.getHistory(any<String>()) } returns emptyList()
        coEvery { api.getStreak(any<String>()) } returns StreakResponse(0)
        coEvery { api.getStats(any<String>()) } returns emptyList()

        viewModel.addWater(amount)
        
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { dao.insert(any<WaterEntry>()) }
        coVerify { api.addWater("Bearer $token", any<WaterNetworkEntry>()) }
    }

    @Test
    fun `register success calls login`() = runTest {
        val email = "new@example.com"
        val password = "password"
        coEvery { api.register(AuthRequest(email, password)) } returns Response.success(RegisterResponse("User registered"))
        coEvery { api.login(AuthRequest(email, password)) } returns Response.success(AuthResponse("token"))
        
        // refreshData mocks
        every { tokenManager.saveToken(any<String>()) } just Runs
        every { tokenManager.getToken() } returns "token"
        coEvery { api.getGoal(any<String>()) } returns DailyGoal(2000)
        every { tokenManager.saveGoal(any<Int>()) } just Runs
        coEvery { api.getHistory(any<String>()) } returns emptyList()
        coEvery { api.getStreak(any<String>()) } returns StreakResponse(0)
        coEvery { api.getStats(any<String>()) } returns emptyList()
        coEvery { dao.getAllEntries() } returns emptyList()

        viewModel.register(email, password)
        
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { api.register(AuthRequest(email, password)) }
        coVerify { api.login(AuthRequest(email, password)) }
    }

    @Test
    fun `refreshData updates all states when token exists`() = runTest {
        val token = "jwt-token"
        every { tokenManager.getToken() } returns token
        
        coEvery { api.getGoal(any<String>()) } returns DailyGoal(2200)
        every { tokenManager.saveGoal(2200) } just Runs
        coEvery { api.getHistory(any<String>()) } returns listOf(WaterNetworkEntry("1", 200, 100L))
        coEvery { dao.insert(any<WaterEntry>()) } just Runs
        coEvery { api.getStreak(any<String>()) } returns StreakResponse(7)
        coEvery { api.getStats(any<String>()) } returns listOf(DailyStat("2024-01-01", 1500))
        coEvery { dao.getAllEntries() } returns listOf(WaterEntry("1", 200, System.currentTimeMillis()))

        viewModel.refreshData()
        
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2200, viewModel.dailyGoal.value)
        assertEquals(7, viewModel.streak.value)
        assertEquals(1, viewModel.stats.value.size)
        assertEquals(200, viewModel.totalWater.value)
    }
}
