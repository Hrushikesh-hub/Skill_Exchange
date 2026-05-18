package com.example.skillexchangeapp.data.local.dao

import androidx.room.*
import com.example.skillexchangeapp.data.local.entity.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert
    suspend fun insertMessage(message: Message)

    @Query("SELECT * FROM messages WHERE (senderId = :userId AND receiverId = :otherId) OR (senderId = :otherId AND receiverId = :userId) ORDER BY timestamp ASC")
    fun getMessagesBetweenUsers(userId: Long, otherId: Long): Flow<List<Message>>
}
