package com.example.skillexchangeapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "need_posts",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class NeedPost(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val description: String,
    val skillRequired: String,
    val estimatedHours: Int,
    val urgencyLevel: String,
    val location: String,
    val status: String = "Open", // Open, In Progress, Fulfilled, Cancelled
    val createdAt: Long = System.currentTimeMillis(),
    val deadline: Long? = null,     // When work must be done by
    val offerCount: Int = 0,        // Cached count of offers received
    val requiresVerifiedWorker: Boolean = false,
    val proofImageUri: String? = null,
    val aiSummary: String = "",
    val fairnessNote: String = ""
)
