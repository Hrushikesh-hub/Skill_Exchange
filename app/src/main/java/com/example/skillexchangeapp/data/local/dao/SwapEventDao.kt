package com.example.skillexchangeapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.skillexchangeapp.data.local.entity.SwapEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface SwapEventDao {
    @Insert
    suspend fun insertEvent(event: SwapEvent): Long

    @Query("SELECT * FROM swap_events WHERE swapId = :swapId ORDER BY timestamp ASC")
    fun getEventsForSwap(swapId: Long): Flow<List<SwapEvent>>

    @Query("SELECT * FROM swap_events WHERE swapId = :swapId ORDER BY timestamp ASC")
    suspend fun getEventsForSwapSync(swapId: Long): List<SwapEvent>

    @Query("SELECT * FROM swap_events WHERE id = :id")
    suspend fun getEventById(id: Long): SwapEvent?
}
