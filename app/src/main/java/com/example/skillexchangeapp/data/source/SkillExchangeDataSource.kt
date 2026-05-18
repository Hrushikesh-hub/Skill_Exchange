package com.example.skillexchangeapp.data.source

import com.example.skillexchangeapp.data.local.entity.Message
import com.example.skillexchangeapp.data.local.entity.NeedPost
import com.example.skillexchangeapp.data.local.entity.Offer
import com.example.skillexchangeapp.data.local.entity.User
import kotlinx.coroutines.flow.Flow

/**
 * Backend boundary for Room today and Firestore later.
 * The app remains offline-ready, while the contract mirrors realtime cloud data.
 */
interface SkillExchangeDataSource {
    val backendName: String
    fun observeOpenNeeds(): Flow<List<NeedPost>>
    fun observeUser(userId: Long): Flow<User?>
    fun observeMessages(userId: Long, otherUserId: Long): Flow<List<Message>>
    suspend fun postNeed(post: NeedPost): Long
    suspend fun submitOffer(offer: Offer)
    suspend fun sendMessage(message: Message)
}
