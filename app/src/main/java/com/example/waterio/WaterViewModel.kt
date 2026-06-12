package com.example.waterio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waterio.data.WaterDao
import com.example.waterio.data.WaterEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class AppState { IDLE, LOADING, SUCCESS, ERROR, OFFLINE_SAVED }

class WaterViewModel(private val dao: WaterDao) : ViewModel() {

    private val _currentState = MutableStateFlow(AppState.IDLE)
    val currentState: StateFlow<AppState> = _currentState

    private val _totalWater = MutableStateFlow(0)
    val totalWater: StateFlow<Int> = _totalWater

    fun addWater(amount: Int, hasNetwork: Boolean) {
        _currentState.value = AppState.LOADING

        viewModelScope.launch {
            val isOnlineReady = hasNetwork && amount > 0
            val isOfflineReady = !hasNetwork && amount > 0

            if (isOnlineReady) {
                dao.insert(WaterEntry(amountMl = amount, isSynced = true))
                _totalWater.value += amount
                _currentState.value = AppState.SUCCESS
            }

            if (isOfflineReady) {
                dao.insert(WaterEntry(amountMl = amount, isSynced = false))
                _totalWater.value += amount
                _currentState.value = AppState.OFFLINE_SAVED
            }

            if (amount <= 0) {
                _currentState.value = AppState.ERROR
            }
        }
    }
}