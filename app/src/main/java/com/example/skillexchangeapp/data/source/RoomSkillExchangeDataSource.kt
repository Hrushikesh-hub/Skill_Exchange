package com.example.skillexchangeapp.data.source

import com.example.skillexchangeapp.data.repository.SkillExchangeRepository
import com.example.skillexchangeapp.data.local.entity.Message
import com.example.skillexchangeapp.data.local.entity.NeedPost
import com.example.skillexchangeapp.data.local.entity.Offer
import com.example.skillexchangeapp.data.local.entity.User
import kotlinx.coroutines.flow.Flow

class RoomSkillExchangeDataSource(
    private val repository: SkillExchangeRepository
) : SkillExchangeDataSource {
    override val backendName: String = "Room offline demo backend"

    override fun observeOpenNeeds(): Flow<List<NeedPost>> = repository.getAllOpenNeeds()
    override fun observeUser(userId: Long): Flow<User?> = repository.getUserById(userId)
    override fun observeMessages(userId: Long, otherUserId: Long): Flow<List<Message>> =
        repository.getMessages(userId, otherUserId)

    override suspend fun postNeed(post: NeedPost): Long = repository.postNeed(post)
    override suspend fun submitOffer(offer: Offer) = repository.submitOffer(offer)
    override suspend fun sendMessage(message: Message) = repository.sendMessage(message)
}
