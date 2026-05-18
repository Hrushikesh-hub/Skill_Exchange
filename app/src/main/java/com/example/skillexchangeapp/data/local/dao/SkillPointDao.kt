package com.example.skillexchangeapp.data.local.dao

import androidx.room.*
import com.example.skillexchangeapp.data.local.entity.SkillPointTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillPointDao {
    @Insert
    suspend fun insertTransaction(transaction: SkillPointTransaction)

    @Query("SELECT COALESCE(SUM(amount), 0) FROM skill_point_transactions WHERE userId = :userId")
    suspend fun getBalance(userId: Long): Int

    @Query("SELECT * FROM skill_point_transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionHistory(userId: Long): Flow<List<SkillPointTransaction>>

    @Query("SELECT * FROM skill_point_transactions ORDER BY timestamp DESC")
    suspend fun getAllTransactionsSync(): List<SkillPointTransaction>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM skill_point_transactions WHERE userId = :userId AND amount > 0")
    suspend fun getEarnedTotal(userId: Long): Int

    @Query("SELECT COALESCE(SUM(ABS(amount)), 0) FROM skill_point_transactions WHERE userId = :userId AND amount < 0")
    suspend fun getSpentTotal(userId: Long): Int
}
