package com.example.skillexchangeapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "swap_events",
    foreignKeys = [ForeignKey(
        entity = Swap::class,
        parentColumns = ["id"],
        childColumns = ["swapId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("swapId")]
)
data class SwapEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val swapId: Long,
    val actorUserId: Long,
    val eventType: String,
    val title: String,
    val detail: String,
    val timestamp: Long = System.currentTimeMillis()
)
