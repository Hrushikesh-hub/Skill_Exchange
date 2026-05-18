package com.example.skillexchangeapp

import android.app.Application
import com.example.skillexchangeapp.data.local.AppDatabase
import com.example.skillexchangeapp.data.repository.SkillExchangeRepository

class SkillExchangeApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        SkillExchangeRepository(
            database.userDao(),
            database.skillDao(),
            database.needPostDao(),
            database.offerDao(),
            database.swapDao(),
            database.reviewDao(),
            database.messageDao(),
            database.skillPointDao(),
            database.notificationDao(),
            database.verificationRequestDao(),
            database.swapEventDao(),
            database.reportDao(),
            database.adminActionDao(),
            database.impactMetricDao()
        )
    }
}
