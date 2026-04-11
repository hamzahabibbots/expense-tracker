package com.kharcha.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_DEFAULT
        )
    ],
    indices = [
        Index("date"),
        Index("category_id"),
        Index("merchant")
    ]
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val amount: Double,
    val merchant: String,
    val date: String,
    @ColumnInfo(name = "bank_name") val bankName: String?,
    @ColumnInfo(name = "category_id", defaultValue = "other") val categoryId: String,
    @ColumnInfo(name = "raw_sms") val rawSms: String?,
    val sender: String?,
    @ColumnInfo(defaultValue = "DEBIT") val type: String = "DEBIT",
    val balance: Double? = null,
    @ColumnInfo(name = "created_at") val createdAt: String
)
