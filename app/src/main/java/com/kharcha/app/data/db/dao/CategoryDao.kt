package com.kharcha.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kharcha.app.data.db.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Query("SELECT * FROM categories ORDER BY is_default DESC, name ASC")
    suspend fun getAll(): List<CategoryEntity>

    @Query("SELECT * FROM categories ORDER BY is_default DESC, name ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: String): CategoryEntity?

    @Query("UPDATE categories SET name = :name, color = :color WHERE id = :id")
    suspend fun update(id: String, name: String, color: String)

    @Query("DELETE FROM categories WHERE id = :id AND is_default = 0")
    suspend fun delete(id: String)

    @Query("DELETE FROM categories WHERE is_default = 0")
    suspend fun deleteAllCustom()

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCount(): Int

    @Query("UPDATE transactions SET category_id = 'other' WHERE category_id = :categoryId")
    suspend fun moveTransactionsToOther(categoryId: String)
}
