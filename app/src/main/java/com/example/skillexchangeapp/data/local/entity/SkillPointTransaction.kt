package com.example.skillexchangeapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "skill_point_transactions",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class SkillPointTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val amount: Int,
    val type: String, // EARNED, SPENT, BONUS
    val relatedSwapId: Long? = null,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
