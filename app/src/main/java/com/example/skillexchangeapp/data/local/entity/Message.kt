package com.example.skillexchangeapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["senderId"]),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["receiverId"])
    ],
    indices = [Index("senderId"), Index("receiverId")]
)
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderId: Long,
    val receiverId: Long,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
