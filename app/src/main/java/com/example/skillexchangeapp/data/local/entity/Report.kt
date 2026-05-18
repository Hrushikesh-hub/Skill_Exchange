package com.example.skillexchangeapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class Report(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reporterId: Long,
    val reportedUserId: Long? = null,
    val needPostId: Long? = null,
    val swapId: Long? = null,
    val reason: String,
    val details: String,
    val status: String = "Open", // Open, Under Review, Resolved, Dismissed
    val resolution: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null
)
