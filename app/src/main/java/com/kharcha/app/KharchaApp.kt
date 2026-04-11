package com.kharcha.app

import android.app.Application

class KharchaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Database is lazily initialized via AppDatabase.getInstance()
    }
}
