package com.example.skillexchangeapp.data.local.dao

import androidx.room.*
import com.example.skillexchangeapp.data.local.entity.NeedPost
import kotlinx.coroutines.flow.Flow

@Dao
interface NeedPostDao {
    @Insert
    suspend fun insertNeedPost(post: NeedPost): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun syncInsertNeedPost(post: NeedPost): Long

    @Update
    suspend fun updateNeedPost(post: NeedPost)

    @Query("SELECT * FROM need_posts WHERE status = 'Open' ORDER BY createdAt DESC")
    fun getAllOpenNeeds(): Flow<List<NeedPost>>

    @Query("SELECT * FROM need_posts WHERE status = 'Open' ORDER BY createdAt DESC")
    suspend fun getAllOpenNeedsSync(): List<NeedPost>

    @Query("SELECT * FROM need_posts ORDER BY createdAt DESC")
    suspend fun getAllNeedsSync(): List<NeedPost>

    @Query("SELECT * FROM need_posts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getNeedsByUser(userId: Long): Flow<List<NeedPost>>

    @Query("SELECT * FROM need_posts WHERE userId = :userId AND status = 'Open'")
    suspend fun getOpenNeedsByUserSync(userId: Long): List<NeedPost>

    @Query("SELECT * FROM need_posts WHERE id = :id")
    suspend fun getNeedById(id: Long): NeedPost?

    @Query("SELECT * FROM need_posts WHERE status = 'Open' AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR skillRequired LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%' OR urgencyLevel LIKE '%' || :query || '%' OR CAST(estimatedHours AS TEXT) LIKE '%' || :query || '%') ORDER BY createdAt DESC")
    fun searchOpenNeeds(query: String): Flow<List<NeedPost>>

    @Query("SELECT * FROM need_posts WHERE status = 'Open' AND skillRequired LIKE '%' || :skill || '%' ORDER BY createdAt DESC")
    fun getFilteredNeedsBySkill(skill: String): Flow<List<NeedPost>>

    @Query("SELECT skillRequired FROM need_posts GROUP BY skillRequired ORDER BY COUNT(*) DESC LIMIT 1")
    suspend fun getTopDemandedSkill(): String?
}
