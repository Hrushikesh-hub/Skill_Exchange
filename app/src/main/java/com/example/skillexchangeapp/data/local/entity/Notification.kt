package com.example.skillexchangeapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val message: String,
    val type: String, // OFFER_RECEIVED, OFFER_ACCEPTED, SWAP_SCHEDULED, SWAP_COMPLETED, SWAP_CANCELLED, REVIEW_RECEIVED
    val relatedId: Long? = null,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
