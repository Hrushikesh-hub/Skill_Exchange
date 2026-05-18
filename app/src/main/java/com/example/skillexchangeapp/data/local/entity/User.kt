package com.example.skillexchangeapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val email: String,
    val phone: String,
    val village: String,
    val primarySkill: String,
    val secondarySkills: String,
    val experienceYears: Int,
    var trustScore: Float = 0f,
    var completedSwaps: Int = 0,
    val profileImageUri: String? = null,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis(),
    var skillPoints: Int = 0,
    val bio: String = "",
    var isAvailable: Boolean = true,
    val verificationStatus: String = "Unverified", // Unverified, Pending, Verified, Rejected
    var reliabilityScore: Int = 70,
    val portfolioSummary: String = "",
    var reportCount: Int = 0,
    val isAdmin: Boolean = false
)
