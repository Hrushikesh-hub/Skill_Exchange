package com.example.skillexchangeapp.data.source

import com.example.skillexchangeapp.data.local.entity.Message
import com.example.skillexchangeapp.data.local.entity.NeedPost
import com.example.skillexchangeapp.data.local.entity.Offer
import com.example.skillexchangeapp.data.local.entity.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Placeholder adapter documenting the Firestore migration contract.
 * Add Firebase dependencies and map collection snapshots here without touching ViewModels.
 */
class FirestoreSkillExchangeDataSource : SkillExchangeDataSource {
    override val backendName: String = "Firestore realtime backend"

    override fun observeOpenNeeds(): Flow<List<NeedPost>> = emptyFlow()
    override fun observeUser(userId: Long): Flow<User?> = emptyFlow()
    override fun observeMessages(userId: Long, otherUserId: Long): Flow<List<Message>> = emptyFlow()
    override suspend fun postNeed(post: NeedPost): Long = error("Firestore backend is not configured")
    override suspend fun submitOffer(offer: Offer) = error("Firestore backend is not configured")
    override suspend fun sendMessage(message: Message) = error("Firestore backend is not configured")
}
