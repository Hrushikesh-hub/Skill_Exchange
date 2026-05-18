package com.example.skillexchangeapp.data.local.dao

import androidx.room.*
import com.example.skillexchangeapp.data.local.entity.Review
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Insert
    suspend fun insertReview(review: Review)

    @Query("SELECT * FROM reviews WHERE swapId = :swapId")
    fun getReviewsForSwap(swapId: Long): Flow<List<Review>>

    @Query("""
        SELECT AVG(CAST(rating AS FLOAT)) FROM reviews r 
        INNER JOIN swaps s ON r.swapId = s.id 
        WHERE (s.userAId = :userId OR s.userBId = :userId) 
        AND r.reviewerId != :userId
    """)
    suspend fun getAverageRatingForUser(userId: Long): Float?

    @Query("""
        SELECT COUNT(*) FROM reviews r 
        INNER JOIN swaps s ON r.swapId = s.id 
        WHERE (s.userAId = :userId OR s.userBId = :userId) 
        AND r.reviewerId != :userId
    """)
    suspend fun getReviewCountForUser(userId: Long): Int

    @Query("SELECT COUNT(*) FROM reviews WHERE swapId = :swapId AND reviewerId = :reviewerId")
    suspend fun hasUserReviewedSwap(swapId: Long, reviewerId: Long): Int
}
