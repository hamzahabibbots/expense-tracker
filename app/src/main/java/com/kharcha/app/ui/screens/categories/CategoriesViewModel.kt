package com.kharcha.app.ui.screens.categories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kharcha.app.data.db.AppDatabase
import com.kharcha.app.data.model.Category
import com.kharcha.app.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoriesUiState(
    val categories: List<Category> = emptyList(),
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val editingCategory: Category? = null,
    val newName: String = "",
    val selectedColor: String = "#FF6B6B",
    val error: String? = null
)

class CategoriesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val categoryRepo = CategoryRepository(db.categoryDao())

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            val categories = categoryRepo.getAll()
            _uiState.value = _uiState.value.copy(categories = categories)
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(
            showAddDialog = true, newName = "", selectedColor = "#FF6B6B", error = null
        )
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false, error = null)
    }

    fun showEditDialog(category: Category) {
        _uiState.value = _uiState.value.copy(
            showEditDialog = true,
            editingCategory = category,
            newName = category.name,
            selectedColor = category.color,
            error = null
        )
    }

    fun hideEditDialog() {
        _uiState.value = _uiState.value.copy(
            showEditDialog = false, editingCategory = null, error = null
        )
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(newName = name)
    }

    fun updateColor(color: String) {
        _uiState.value = _uiState.value.copy(selectedColor = color)
    }

    fun addCategory() {
        viewModelScope.launch {
            val name = _uiState.value.newName.trim()
            if (name.isEmpty()) {
                _uiState.value = _uiState.value.copy(error = "Please enter a name")
                return@launch
            }
            if (_uiState.value.categories.any { it.name.equals(name, ignoreCase = true) }) {
                _uiState.value = _uiState.value.copy(error = "Category already exists")
                return@launch
            }
            categoryRepo.create(name, _uiState.value.selectedColor)
            hideAddDialog()
            loadCategories()
        }
    }

    fun editCategory() {
        viewModelScope.launch {
            val cat = _uiState.value.editingCategory ?: return@launch
            val name = _uiState.value.newName.trim()
            if (name.isEmpty()) {
                _uiState.value = _uiState.value.copy(error = "Please enter a name")
                return@launch
            }
            categoryRepo.update(cat.id, name, _uiState.value.selectedColor)
            hideEditDialog()
            loadCategories()
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            categoryRepo.delete(id)
            loadCategories()
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            categoryRepo.resetToDefaults()
            loadCategories()
        }
    }
}
