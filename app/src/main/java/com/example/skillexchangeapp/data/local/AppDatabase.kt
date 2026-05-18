package com.example.skillexchangeapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.skillexchangeapp.data.local.dao.*
import com.example.skillexchangeapp.data.local.entity.*

@Database(
    entities = [
        User::class,
        Skill::class,
        NeedPost::class,
        Offer::class,
        Swap::class,
        Review::class,
        Message::class,
        SkillPointTransaction::class,
        Notification::class,
        VerificationRequest::class,
        SwapEvent::class,
        Report::class,
        AdminAction::class,
        ImpactMetric::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun skillDao(): SkillDao
    abstract fun needPostDao(): NeedPostDao
    abstract fun offerDao(): OfferDao
    abstract fun swapDao(): SwapDao
    abstract fun reviewDao(): ReviewDao
    abstract fun messageDao(): MessageDao
    abstract fun skillPointDao(): SkillPointDao
    abstract fun notificationDao(): NotificationDao
    abstract fun verificationRequestDao(): VerificationRequestDao
    abstract fun swapEventDao(): SwapEventDao
    abstract fun reportDao(): ReportDao
    abstract fun adminActionDao(): AdminActionDao
    abstract fun impactMetricDao(): ImpactMetricDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "skill_exchange_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
