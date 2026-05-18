package com.example.skillexchangeapp.data.local.dao

import androidx.room.*
import com.example.skillexchangeapp.data.local.entity.Swap
import kotlinx.coroutines.flow.Flow

@Dao
interface SwapDao {
    @Insert
    suspend fun insertSwap(swap: Swap): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun syncInsertSwap(swap: Swap)

    @Update
    suspend fun updateSwap(swap: Swap)

    @Query("SELECT * FROM swaps WHERE userAId = :userId OR userBId = :userId")
    fun getSwapsForUser(userId: Long): Flow<List<Swap>>

    @Query("SELECT * FROM swaps WHERE id = :id")
    suspend fun getSwapById(id: Long): Swap?

    @Query("SELECT * FROM swaps")
    suspend fun getAllSwapsSync(): List<Swap>

    @Query("SELECT * FROM swaps WHERE status = 'Completed'")
    suspend fun getCompletedSwapsSync(): List<Swap>
}
