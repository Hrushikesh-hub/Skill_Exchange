package com.example.skillexchangeapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "verification_requests",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class VerificationRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val skill: String,
    val proofText: String,
    val proofImageUri: String? = null,
    val status: String = "Pending", // Pending, Approved, Rejected
    val reviewerId: Long? = null,
    val reviewerNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null
)
