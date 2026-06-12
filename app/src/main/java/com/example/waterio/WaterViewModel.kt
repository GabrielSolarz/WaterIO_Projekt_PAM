package com.example.waterio.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.waterio.data.TokenManager
import com.example.waterio.data.WaterDao
import com.example.waterio.data.WaterEntry
import com.example.waterio.network.*
import com.example.waterio.notifications.WaterNotificationReceiver
import com.example.waterio.sync.SyncWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

enum class AuthState { UNAUTHENTICATED, LOADING, SUCCESS, ERROR }

class WaterViewModel(
    private val dao: WaterDao,
    private val api: WaterApiService,
    private val tokenManager: TokenManager,
    private val context: Context
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState.UNAUTHENTICATED)
    val authState: StateFlow<AuthState> = _authState

    private val _totalWater = MutableStateFlow(0)
    val totalWater: StateFlow<Int> = _totalWater

    private val _dailyGoal = MutableStateFlow(tokenManager.getGoal())
    val dailyGoal: StateFlow<Int> = _dailyGoal

    private val _history = MutableStateFlow<List<WaterEntry>>(emptyList())
    val history: StateFlow<List<WaterEntry>> = _history

    private val _stats = MutableStateFlow<List<DailyStat>>(emptyList())
    val stats: StateFlow<List<DailyStat>> = _stats

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        if (tokenManager.getToken() != null) {
            _authState.value = AuthState.SUCCESS
            refreshData()
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                val response = api.login(AuthRequest(email, password))
                if (response.isSuccessful && response.body() != null) {
                    tokenManager.saveToken(response.body()!!.token)
                    _authState.value = AuthState.SUCCESS
                    refreshData()
                } else {
                    _errorMessage.value = "Błędne dane logowania!"
                    _authState.value = AuthState.UNAUTHENTICATED
                }
            } catch (e: Exception) {
                _errorMessage.value = "Błąd połączenia z serwerem!"
                _authState.value = AuthState.UNAUTHENTICATED
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                val response = api.register(AuthRequest(email, password))
                if (response.isSuccessful) {
                    login(email, password)
                } else {
                    _errorMessage.value = "Email jest już zajęty!"
                    _authState.value = AuthState.UNAUTHENTICATED
                }
            } catch (e: Exception) {
                _errorMessage.value = "Błąd rejestracji!"
                _authState.value = AuthState.UNAUTHENTICATED
            }
        }
    }

    fun logout() {
        tokenManager.clearSession()
        _authState.value = AuthState.UNAUTHENTICATED
    }

    fun addWater(amount: Int) {
        if (amount <= 0) return
        viewModelScope.launch {
            val newEntry = WaterEntry(amountMl = amount, isSynced = false)
            dao.insert(newEntry)
            loadLocalData()
            scheduleNotification()

            // Szybka aktualizacja UI online jeśli to możliwe
            val token = tokenManager.getToken()
            if (token != null) {
                try {
                    val result = api.addWater("Bearer $token", WaterNetworkEntry(newEntry.id, amount, newEntry.timestamp))
                    
                    // Jeśli serwer nadał nowe ID, podmieniamy lokalnie, aby uniknąć duplikatów przy refreshData()
                    if (result.id != null && result.id != newEntry.id) {
                        dao.deletePermanently(newEntry.id)
                        dao.insert(newEntry.copy(id = result.id, isSynced = true))
                    } else {
                        dao.insert(newEntry.copy(isSynced = true))
                    }
                    refreshData()
                } catch (e: Exception) {
                    // W razie błędu odpalamy WorkManagera
                    triggerOfflineSync()
                }
            } else {
                triggerOfflineSync()
            }
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            dao.markAsDeleted(id)
            loadLocalData()

            val token = tokenManager.getToken()
            if (token != null) {
                try {
                    api.deleteWater("Bearer $token", id)
                    dao.deletePermanently(id)
                    refreshData()
                } catch (e: Exception) {
                    triggerOfflineSync()
                }
            } else {
                triggerOfflineSync()
            }
        }
    }

    fun updateGoal(newGoal: Int) {
        tokenManager.saveGoal(newGoal)
        _dailyGoal.value = newGoal
        val token = tokenManager.getToken() ?: return
        viewModelScope.launch {
            try {
                api.updateGoal("Bearer $token", DailyGoal(newGoal))
            } catch (e: Exception) {}
        }
    }

    fun refreshData() {
        loadLocalData()
        val token = tokenManager.getToken() ?: return
        val bearer = "Bearer $token"
        viewModelScope.launch {
            try {
                val remoteGoal = api.getGoal(bearer)
                tokenManager.saveGoal(remoteGoal.goalMl)
                _dailyGoal.value = remoteGoal.goalMl

                val remoteHistory = api.getHistory(bearer)
                remoteHistory.forEach {
                    dao.insert(com.example.waterio.data.WaterEntry(it.id ?: "", it.amountMl, it.timestamp ?: 0L, isSynced = true))
                }

                _streak.value = api.getStreak(bearer).streak
                _stats.value = api.getStats(bearer)
                loadLocalData()
            } catch (e: Exception) {
                android.util.Log.e("WaterViewModel", "Refresh failed: ${e.message}", e)
            }
        }
    }

    private fun loadLocalData() {
        viewModelScope.launch {
            val localEntries = dao.getAllEntries()
            _history.value = localEntries

            // Filtrowanie i sumowanie dzisiejszego spożycia lokalnie (czas lokalny)
            val calendar = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            val startOfDay = calendar.timeInMillis
            _totalWater.value = localEntries.filter { it.timestamp >= startOfDay }.sumOf { it.amountMl }
        }
    }

    private fun triggerOfflineSync() {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(constraints).build()
        WorkManager.getInstance(context).enqueueUniqueWork("WaterSync", ExistingWorkPolicy.REPLACE, syncRequest)
    }

    private fun scheduleNotification() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Na Android 13+ musimy sprawdzić, czy mamy uprawnienie do dokładnych alarmów
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Jeśli nie mamy uprawnień, używamy zwykłego alarmu (nie będzie on co do sekundy)
                val intent = Intent(context, WaterNotificationReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                val triggerTime = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(3)
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                return
            }
        }

        val intent = Intent(context, WaterNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Ustaw powiadomienie na za 3 godziny
        val triggerTime = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(3)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }

    fun clearError() { _errorMessage.value = null }
}