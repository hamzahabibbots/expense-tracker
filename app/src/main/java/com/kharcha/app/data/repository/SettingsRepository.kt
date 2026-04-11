package com.kharcha.app.data.repository

import com.kharcha.app.data.db.dao.SettingsDao
import com.kharcha.app.data.db.entity.SettingEntity

class SettingsRepository(private val settingsDao: SettingsDao) {

    suspend fun getString(key: String, default: String? = null): String? {
        return settingsDao.get(key) ?: default
    }

    suspend fun setString(key: String, value: String) {
        settingsDao.set(SettingEntity(key, value))
    }

    suspend fun getBoolean(key: String, default: Boolean = false): Boolean {
        return settingsDao.get(key)?.toBooleanStrictOrNull() ?: default
    }

    suspend fun setBoolean(key: String, value: Boolean) {
        settingsDao.set(SettingEntity(key, value.toString()))
    }

    suspend fun getLong(key: String, default: Long = 0L): Long {
        return settingsDao.get(key)?.toLongOrNull() ?: default
    }

    suspend fun setLong(key: String, value: Long) {
        settingsDao.set(SettingEntity(key, value.toString()))
    }

    suspend fun deleteAll() {
        settingsDao.deleteAll()
    }
}
