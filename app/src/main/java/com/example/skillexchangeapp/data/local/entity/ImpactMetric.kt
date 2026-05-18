package com.example.skillexchangeapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "impact_metrics")
data class ImpactMetric(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val totalHoursExchanged: Int,
    val moneySavedEstimate: Int,
    val activeWorkers: Int,
    val completedSwaps: Int,
    val topSkillInDemand: String,
    val averageTrustScore: Float,
    val generatedAt: Long = System.currentTimeMillis()
)
