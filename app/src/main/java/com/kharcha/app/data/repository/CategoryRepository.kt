package com.kharcha.app.data.repository

import com.kharcha.app.data.db.dao.CategoryDao
import com.kharcha.app.data.db.entity.CategoryEntity
import com.kharcha.app.data.model.Category
import com.kharcha.app.util.DefaultCategories
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepository(private val categoryDao: CategoryDao) {

    suspend fun initializeDefaults() {
        if (categoryDao.getCount() == 0) {
            val defaults = DefaultCategories.ALL.map {
                CategoryEntity(it.id, it.name, it.color, it.icon, isDefault = true)
            }
            categoryDao.insertAll(defaults)
        }
    }

    suspend fun getAll(): List<Category> {
        return categoryDao.getAll().map { it.toDomain() }
    }

    fun observeAll(): Flow<List<Category>> {
        return categoryDao.observeAll().map { list -> list.map { it.toDomain() } }
    }

    suspend fun getById(id: String): Category? {
        return categoryDao.getById(id)?.toDomain()
    }

    suspend fun create(name: String, color: String, icon: String? = "pricetag"): Category {
        val id = name.lowercase()
            .replace(Regex("[^a-z0-9]"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
            .take(20)

        val entity = CategoryEntity(id, name, color, icon, isDefault = false)
        categoryDao.insert(entity)
        return entity.toDomain()
    }

    suspend fun update(id: String, name: String, color: String) {
        categoryDao.update(id, name, color)
    }

    suspend fun delete(id: String) {
        categoryDao.moveTransactionsToOther(id)
        categoryDao.delete(id)
    }

    suspend fun resetToDefaults() {
        categoryDao.deleteAllCustom()
    }

    private fun CategoryEntity.toDomain() = Category(id, name, color, icon, isDefault)
}
