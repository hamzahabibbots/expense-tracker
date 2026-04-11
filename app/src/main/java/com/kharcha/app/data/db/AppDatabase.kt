package com.kharcha.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kharcha.app.data.db.dao.CategoryDao
import com.kharcha.app.data.db.dao.MerchantMappingDao
import com.kharcha.app.data.db.dao.SettingsDao
import com.kharcha.app.data.db.dao.TransactionDao
import com.kharcha.app.data.db.entity.CategoryEntity
import com.kharcha.app.data.db.entity.MerchantMappingEntity
import com.kharcha.app.data.db.entity.SettingEntity
import com.kharcha.app.data.db.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        MerchantMappingEntity::class,
        SettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun merchantMappingDao(): MerchantMappingDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kharcha.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
