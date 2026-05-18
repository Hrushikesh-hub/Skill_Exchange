package com.example.skillexchangeapp.data.local.dao

import androidx.room.*
import com.example.skillexchangeapp.data.local.entity.Skill
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillDao {
    @Insert
    suspend fun insertSkill(skill: Skill)

    @Update
    suspend fun updateSkill(skill: Skill)

    @Delete
    suspend fun deleteSkill(skill: Skill)

    @Query("SELECT * FROM skills WHERE userId = :userId")
    fun getSkillsForUser(userId: Long): Flow<List<Skill>>
}
