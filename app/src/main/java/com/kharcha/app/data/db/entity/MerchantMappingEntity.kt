package com.kharcha.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "merchant_mappings")
data class MerchantMappingEntity(
    @PrimaryKey val merchant: String,
    @ColumnInfo(name = "category_id") val categoryId: String,
    val frequency: Int = 1,
    @ColumnInfo(name = "last_used") val lastUsed: String?
)
