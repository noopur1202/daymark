package com.daymark.app

import android.app.Application
import com.daymark.app.data.AppDatabase
import com.daymark.app.data.ActivityRepository

class DayMarkApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.create(this) }
    val repository: ActivityRepository by lazy {
        ActivityRepository(database.activityDao(), database.entryDao())
    }
}
