package com.example.skillexchangeapp.data.local.dao

import androidx.room.*
import com.example.skillexchangeapp.data.local.entity.Offer
import kotlinx.coroutines.flow.Flow

@Dao
interface OfferDao {
    @Insert
    suspend fun insertOffer(offer: Offer)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun syncInsertOffer(offer: Offer)

    @Update
    suspend fun updateOffer(offer: Offer)

    @Query("SELECT * FROM offers WHERE needPostId = :postId")
    fun getOffersForPost(postId: Long): Flow<List<Offer>>

    @Query("SELECT * FROM offers WHERE offeredByUserId = :userId")
    fun getOffersByUser(userId: Long): Flow<List<Offer>>

    @Query("SELECT * FROM offers WHERE needPostId IN (SELECT id FROM need_posts WHERE userId = :userId) AND status = 'Pending' ORDER BY createdAt DESC")
    fun getPendingOffersForUserPosts(userId: Long): Flow<List<Offer>>

    @Query("SELECT COUNT(*) FROM offers WHERE needPostId IN (SELECT id FROM need_posts WHERE userId = :userId) AND status = 'Pending'")
    fun getPendingOfferCount(userId: Long): Flow<Int>

    @Query("UPDATE offers SET status = 'Rejected' WHERE needPostId = :postId AND id != :acceptedOfferId")
    suspend fun rejectOtherOffers(postId: Long, acceptedOfferId: Long)

    @Query("SELECT * FROM offers WHERE needPostId = :postId")
    suspend fun getOffersForPostSync(postId: Long): List<Offer>
}
