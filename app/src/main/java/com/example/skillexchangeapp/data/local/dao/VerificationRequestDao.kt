package com.example.skillexchangeapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.skillexchangeapp.data.local.entity.VerificationRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface VerificationRequestDao {
    @Insert
    suspend fun insertRequest(request: VerificationRequest): Long

    @Update
    suspend fun updateRequest(request: VerificationRequest)

    @Query("SELECT * FROM verification_requests ORDER BY createdAt DESC")
    fun getAllRequests(): Flow<List<VerificationRequest>>

    @Query("SELECT * FROM verification_requests WHERE status = 'Pending' ORDER BY createdAt DESC")
    fun getPendingRequests(): Flow<List<VerificationRequest>>

    @Query("SELECT * FROM verification_requests WHERE userId = :userId ORDER BY createdAt DESC")
    fun getRequestsForUser(userId: Long): Flow<List<VerificationRequest>>

    @Query("SELECT * FROM verification_requests WHERE id = :id")
    suspend fun getRequestById(id: Long): VerificationRequest?
}
