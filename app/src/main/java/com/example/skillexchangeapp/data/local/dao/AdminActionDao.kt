package com.example.skillexchangeapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.skillexchangeapp.data.local.entity.AdminAction
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminActionDao {
    @Insert
    suspend fun insertAction(action: AdminAction): Long

    @Query("SELECT * FROM admin_actions ORDER BY createdAt DESC LIMIT 50")
    fun getRecentActions(): Flow<List<AdminAction>>

    @Query("SELECT * FROM admin_actions WHERE id = :id")
    suspend fun getActionById(id: Long): AdminAction?
}
