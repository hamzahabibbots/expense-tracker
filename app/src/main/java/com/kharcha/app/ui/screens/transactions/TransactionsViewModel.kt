package com.kharcha.app.ui.screens.transactions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kharcha.app.data.db.AppDatabase
import com.kharcha.app.data.model.Category
import com.kharcha.app.data.model.Transaction
import com.kharcha.app.data.repository.CategoryRepository
import com.kharcha.app.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TransactionsUiState(
    val isLoading: Boolean = true,
    val transactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryFilter: String? = null,
    val showCategorySheet: Boolean = false,
    val selectedTransaction: Transaction? = null
)

class TransactionsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val transactionRepo = TransactionRepository(db.transactionDao(), db.merchantMappingDao())
    private val categoryRepo = CategoryRepository(db.categoryDao())

    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val transactions = transactionRepo.getTransactions(limit = 100)
            val categories = categoryRepo.getAll()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                transactions = transactions,
                categories = categories
            )
        }
    }

    fun setFilter(categoryId: String?) {
        _uiState.value = _uiState.value.copy(selectedCategoryFilter = categoryId)
    }

    fun openCategorySheet(transaction: Transaction) {
        _uiState.value = _uiState.value.copy(
            showCategorySheet = true,
            selectedTransaction = transaction
        )
    }

    fun closeCategorySheet() {
        _uiState.value = _uiState.value.copy(
            showCategorySheet = false,
            selectedTransaction = null
        )
    }

    fun changeCategory(transactionId: String, categoryId: String) {
        viewModelScope.launch {
            transactionRepo.updateCategory(transactionId, categoryId)
            closeCategorySheet()
            loadData()
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            transactionRepo.deleteTransaction(id)
            loadData()
        }
    }
}
