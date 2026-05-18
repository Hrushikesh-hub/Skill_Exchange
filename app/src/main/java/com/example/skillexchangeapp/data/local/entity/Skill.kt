package com.example.skillexchangeapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "skills",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class Skill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val skillName: String,
    val skillLevel: String, // Beginner, Intermediate, Expert
    val description: String
)
