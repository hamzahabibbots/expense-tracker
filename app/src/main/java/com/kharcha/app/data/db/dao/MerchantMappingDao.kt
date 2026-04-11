package com.kharcha.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kharcha.app.data.db.entity.MerchantMappingEntity

@Dao
interface MerchantMappingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mapping: MerchantMappingEntity)

    @Query("SELECT category_id FROM merchant_mappings WHERE merchant = :merchant")
    suspend fun getCategoryForMerchant(merchant: String): String?

    @Query("""
        UPDATE merchant_mappings 
        SET category_id = :categoryId, frequency = frequency + 1, last_used = :lastUsed 
        WHERE merchant = :merchant
    """)
    suspend fun updateMapping(merchant: String, categoryId: String, lastUsed: String)

    @Query("SELECT * FROM merchant_mappings WHERE merchant = :merchant")
    suspend fun getByMerchant(merchant: String): MerchantMappingEntity?

    @Query("SELECT * FROM merchant_mappings ORDER BY frequency DESC")
    suspend fun getAll(): List<MerchantMappingEntity>

    @Query("DELETE FROM merchant_mappings")
    suspend fun deleteAll()
}
