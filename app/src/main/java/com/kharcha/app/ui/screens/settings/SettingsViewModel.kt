package com.kharcha.app.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kharcha.app.data.db.AppDatabase
import com.kharcha.app.data.repository.SettingsRepository
import com.kharcha.app.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val transactionCount: Int = 0,
    val notificationsEnabled: Boolean = true,
    val autoSyncEnabled: Boolean = false,
    val exportSuccess: Boolean? = null,
    val message: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val transactionRepo = TransactionRepository(db.transactionDao(), db.merchantMappingDao())
    private val settingsRepo = SettingsRepository(db.settingsDao())

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val count = transactionRepo.getTransactionCount()
            val notifications = settingsRepo.getBoolean("notifications_enabled", true)
            val autoSync = settingsRepo.getBoolean("auto_sync_enabled", false)
            _uiState.value = SettingsUiState(
                transactionCount = count,
                notificationsEnabled = notifications,
                autoSyncEnabled = autoSync
            )
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.setBoolean("notifications_enabled", enabled)
            _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
        }
    }

    fun toggleAutoSync(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.setBoolean("auto_sync_enabled", enabled)
            _uiState.value = _uiState.value.copy(autoSyncEnabled = enabled)
        }
    }

    fun clearTransactions() {
        viewModelScope.launch {
            transactionRepo.deleteAllTransactions()
            _uiState.value = _uiState.value.copy(
                transactionCount = 0,
                message = "All transactions cleared"
            )
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            transactionRepo.deleteAllTransactions()
            db.merchantMappingDao().deleteAll()
            settingsRepo.deleteAll()
            _uiState.value = SettingsUiState(message = "All data reset")
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
