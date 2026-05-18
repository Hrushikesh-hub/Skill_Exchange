package com.example.skillexchangeapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "swaps",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userAId"]),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userBId"]),
        ForeignKey(entity = NeedPost::class, parentColumns = ["id"], childColumns = ["needPostId"])
    ],
    indices = [Index("userAId"), Index("userBId"), Index("needPostId")]
)
data class Swap(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userAId: Long,
    val userBId: Long,
    val needPostId: Long,
    val agreedHours: Int,
    val status: String = "Scheduled",  // Scheduled, Ongoing, Completed, Cancelled
    val completionDate: Long? = null,
    val scheduledDate: Long? = null,     // When the work is scheduled
    val scheduledTime: String? = null,   // e.g. "10:00 AM"
    val cancellationReason: String? = null,
    val cancelledBy: Long? = null,
    val proofNote: String? = null,
    val proofSubmittedAt: Long? = null,
    val disputeStatus: String = "None" // None, Reported, Under Review, Resolved
)
