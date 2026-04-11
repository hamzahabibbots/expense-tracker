package com.kharcha.app.ui.screens.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kharcha.app.data.db.AppDatabase
import com.kharcha.app.data.model.*
import com.kharcha.app.data.repository.CategoryRepository
import com.kharcha.app.data.repository.TransactionRepository
import com.kharcha.app.sms.SmsParser
import com.kharcha.app.sms.SmsReader
import com.kharcha.app.tips.TipEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class DashboardUiState(
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val dashboardData: DashboardData = DashboardData(),
    val tips: List<Tip> = emptyList(),
    val quickTip: Tip? = null,
    val categories: List<Category> = emptyList(),
    val syncResult: SyncResult? = null
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val transactionRepo = TransactionRepository(db.transactionDao(), db.merchantMappingDao())
    private val categoryRepo = CategoryRepository(db.categoryDao())
    private val smsReader = SmsReader(application)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepo.initializeDefaults()
            loadDashboard()
        }
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val now = LocalDate.now()
            val startOfMonth = now.withDayOfMonth(1).format(DateTimeFormatter.ISO_LOCAL_DATE) + "T00:00:00"
            val endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).format(DateTimeFormatter.ISO_LOCAL_DATE) + "T23:59:59"

            try {
                val categorySpending = transactionRepo.getCategorySpending(startOfMonth, endOfMonth)
                val dailySpending = transactionRepo.getSpendingTrend(30)
                val topMerchants = transactionRepo.getTopMerchants(5, startOfMonth, endOfMonth)
                val monthlyComparison = transactionRepo.getMonthlyComparison()
                val recentTransactions = transactionRepo.getTransactions(startOfMonth, endOfMonth, limit = 100)
                val (totalSpending, _) = transactionRepo.getTotalSpending(startOfMonth, endOfMonth)
                val categories = categoryRepo.getAll()

                val data = DashboardData(
                    categorySpending = categorySpending,
                    dailySpending = dailySpending,
                    topMerchants = topMerchants,
                    monthlyComparison = monthlyComparison,
                    recentTransactions = recentTransactions,
                    totalSpending = totalSpending
                )

                val tips = TipEngine.calculateTips(data)
                val quickTip = TipEngine.generateQuickTip(categorySpending, totalSpending)

                _uiState.value = DashboardUiState(
                    isLoading = false,
                    dashboardData = data,
                    tips = tips,
                    quickTip = quickTip,
                    categories = categories
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun syncSms() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)

            try {
                val existing = transactionRepo.getTransactions(limit = 10000)

                // Use real SMS if permission granted, otherwise mock
                val messages = if (smsReader.hasPermission()) {
                    smsReader.readMessages()
                } else {
                    smsReader.getMockMessages()
                }

                val parsed = SmsParser.parseMultiple(messages)
                val unique = SmsParser.filterDuplicates(parsed, existing)

                // Apply learned merchant mappings
                val mapped = unique.map { tx ->
                    val learned = transactionRepo.getMerchantCategory(tx.merchant)
                    if (learned != null) tx.copy(categoryId = learned) else tx
                }

                if (mapped.isNotEmpty()) {
                    transactionRepo.addTransactions(mapped)
                }

                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    syncResult = SyncResult(true, messages.size, parsed.size, mapped.size)
                )

                loadDashboard()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    syncResult = SyncResult(false, error = e.message)
                )
            }
        }
    }
}
