package com.example.skillexchangeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skillexchangeapp.ai.AiMatchEngine
import com.example.skillexchangeapp.ai.BarterSuggestionEngine
import com.example.skillexchangeapp.ai.ImpactAnalyticsEngine
import com.example.skillexchangeapp.ai.OfferDraftEngine
import com.example.skillexchangeapp.ai.SkillMatchingEngine
import com.example.skillexchangeapp.ai.TrustScoreEngine
import com.example.skillexchangeapp.data.local.entity.AdminAction
import com.example.skillexchangeapp.data.local.entity.NeedPost
import com.example.skillexchangeapp.data.local.entity.Notification
import com.example.skillexchangeapp.data.local.entity.Offer
import com.example.skillexchangeapp.data.local.entity.Report
import com.example.skillexchangeapp.data.local.entity.Review
import com.example.skillexchangeapp.data.local.entity.SkillPointTransaction
import com.example.skillexchangeapp.data.local.entity.Swap
import com.example.skillexchangeapp.data.local.entity.SwapEvent
import com.example.skillexchangeapp.data.local.entity.User
import com.example.skillexchangeapp.data.local.entity.VerificationRequest
import com.example.skillexchangeapp.data.repository.SkillExchangeRepository
import com.example.skillexchangeapp.ui.adapter.OfferWithDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NeedAssistResult(
    val suggestedTitle: String,
    val improvedDescription: String,
    val fairnessNote: String
)

class MainViewModel(private val repository: SkillExchangeRepository) : ViewModel() {

    val allOpenNeeds: StateFlow<List<NeedPost>> = repository.getAllOpenNeeds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Wallet StateFlows ──
    private val _walletBalance = MutableStateFlow(0)
    val walletBalance: StateFlow<Int> = _walletBalance.asStateFlow()

    private val _earnedTotal = MutableStateFlow(0)
    val earnedTotal: StateFlow<Int> = _earnedTotal.asStateFlow()

    private val _spentTotal = MutableStateFlow(0)
    val spentTotal: StateFlow<Int> = _spentTotal.asStateFlow()

    private val _transactionHistory = MutableStateFlow<List<SkillPointTransaction>>(emptyList())
    val transactionHistory: StateFlow<List<SkillPointTransaction>> = _transactionHistory.asStateFlow()

    // ── Profile user StateFlow ──
    private val _profileUser = MutableStateFlow<User?>(null)
    val profileUser: StateFlow<User?> = _profileUser.asStateFlow()

    private var transactionCollectJob: kotlinx.coroutines.Job? = null

    fun loadWalletData(userId: Long) {
        viewModelScope.launch {
            _walletBalance.value = repository.getBalanceSync(userId)
            _earnedTotal.value = repository.getEarnedTotal(userId)
            _spentTotal.value = repository.getSpentTotal(userId)
        }
        transactionCollectJob?.cancel()
        transactionCollectJob = viewModelScope.launch {
            repository.getSkillPointHistory(userId).collect {
                _transactionHistory.value = it
            }
        }
    }

    fun loadUserById(userId: Long) {
        viewModelScope.launch {
            _profileUser.value = repository.getUserByIdSync(userId)
        }
    }

    fun registerUser(user: User, onResult: (Boolean, Long) -> Unit) {
        viewModelScope.launch {
            try {
                val existing = repository.getUserByEmail(user.email)
                if (existing != null) { onResult(false, -1L); return@launch }
                val userId = repository.registerUser(user)
                onResult(true, userId)
            } catch (e: Exception) {
                onResult(false, -1L)
            }
        }
    }

    fun getUser(userId: Long): Flow<User?> = repository.getUserById(userId)

    fun updateUser(user: User) {
        viewModelScope.launch { repository.updateUser(user) }
    }

    fun postNeed(post: NeedPost) {
        viewModelScope.launch { repository.postNeed(post) }
    }

    fun generateNeedAssist(skill: String, location: String, description: String, hours: Int, urgency: String): NeedAssistResult {
        return NeedAssistResult(
            suggestedTitle = OfferDraftEngine.suggestNeedTitle(skill, location),
            improvedDescription = OfferDraftEngine.improveNeedDescription(description, skill, hours),
            fairnessNote = OfferDraftEngine.fairnessNote(skill, hours, urgency)
        )
    }

    fun submitOffer(offer: Offer, needPostTitle: String) {
        viewModelScope.launch {
            repository.submitOffer(offer)
            val need = repository.getNeedById(offer.needPostId)
            need?.let { repository.updateNeedPost(it.copy(offerCount = it.offerCount + 1)) }
            repository.addNotification(
                Notification(
                    userId = need?.userId ?: 0,
                    title = "New Trade Offer",
                    message = "Someone offered help on \"$needPostTitle\"",
                    type = "OFFER_RECEIVED",
                    relatedId = offer.needPostId
                )
            )
        }
    }

    fun generateOfferDraft(userId: Long, needPost: NeedPost, callback: (String, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByIdSync(userId) ?: return@launch
            callback(OfferDraftEngine.draftOffer(user, needPost), AiMatchEngine.explainOfferFit(user, needPost))
        }
    }

    fun getSwaps(userId: Long): Flow<List<Swap>> = repository.getSwapsForUser(userId)

    fun updateSwap(swap: Swap) {
        viewModelScope.launch { repository.updateSwap(swap) }
    }

    fun createSwap(swap: Swap) {
        viewModelScope.launch { repository.createSwap(swap) }
    }

    fun getOffersForPost(postId: Long): Flow<List<Offer>> = repository.getOffersForPost(postId)

    fun searchNeeds(query: String): Flow<List<NeedPost>> = repository.searchOpenNeeds(query)
    fun filterNeedsBySkill(skill: String): Flow<List<NeedPost>> = repository.getFilteredNeedsBySkill(skill)

    fun getPendingOfferCount(userId: Long): Flow<Int> = repository.getPendingOfferCount(userId)

    // StateFlow version for Dashboard banner
    fun getPendingOfferCountForUser(userId: Long): StateFlow<Int> =
        repository.getPendingOfferCount(userId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun getPendingOffersWithDetails(userId: Long): Flow<List<OfferWithDetails>> {
        return repository.getPendingOffersForUserPosts(userId).map { offers ->
            offers.mapNotNull { offer ->
                val offerer = repository.getUserByIdSync(offer.offeredByUserId)
                val need = repository.getNeedById(offer.needPostId)
                if (offerer != null && need != null) OfferWithDetails(offer, offerer, need.title) else null
            }
        }
    }

    fun acceptOffer(offer: Offer, scheduledDate: Long?, scheduledTime: String?, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.updateOffer(offer.copy(status = "Accepted"))
            repository.rejectOtherOffers(offer.needPostId, offer.id)
            val need = repository.getNeedById(offer.needPostId)
            need?.let { repository.updateNeedPost(it.copy(status = "In Progress")) }
            val swap = Swap(
                userAId = need?.userId ?: 0,
                userBId = offer.offeredByUserId,
                needPostId = offer.needPostId,
                agreedHours = if (offer.offeredHours > 0) offer.offeredHours else (need?.estimatedHours ?: 1),
                status = if (scheduledDate != null) "Scheduled" else "Ongoing",
                scheduledDate = scheduledDate,
                scheduledTime = scheduledTime
            )
            val swapId = repository.createSwap(swap)
            repository.addSwapEvent(
                SwapEvent(
                    swapId = swapId,
                    actorUserId = need?.userId ?: 0,
                    eventType = "OFFER_ACCEPTED",
                    title = "Offer accepted",
                    detail = "Swap created for ${swap.agreedHours} Skill Points"
                )
            )
            repository.addNotification(
                Notification(
                    userId = offer.offeredByUserId,
                    title = "Offer Accepted",
                    message = "Your offer on \"${need?.title}\" was accepted${if (scheduledTime != null) ". Scheduled: $scheduledTime" else ""}",
                    type = "OFFER_ACCEPTED",
                    relatedId = offer.needPostId
                )
            )
            onComplete()
        }
    }

    fun rejectOffer(offer: Offer) {
        viewModelScope.launch {
            repository.updateOffer(offer.copy(status = "Rejected"))
            val need = repository.getNeedById(offer.needPostId)
            repository.addNotification(
                Notification(
                    userId = offer.offeredByUserId,
                    title = "Offer Not Accepted",
                    message = "Your offer on \"${need?.title ?: "a post"}\" was not accepted this time",
                    type = "OFFER_REJECTED",
                    relatedId = offer.needPostId
                )
            )
        }
    }

    fun cancelSwap(swap: Swap, userId: Long, reason: String) {
        viewModelScope.launch {
            repository.updateSwap(swap.copy(status = "Cancelled", cancellationReason = reason, cancelledBy = userId))
            val user = repository.getUserByIdSync(userId)
            user?.let {
                repository.updateUser(
                    it.copy(
                        trustScore = TrustScoreEngine.cancellationPenalty(it.trustScore),
                        reliabilityScore = TrustScoreEngine.reliabilityAfterCancellation(it)
                    )
                )
            }
            val need = repository.getNeedById(swap.needPostId)
            need?.let { repository.updateNeedPost(it.copy(status = "Open")) }
            val partnerId = if (swap.userAId == userId) swap.userBId else swap.userAId
            val partnerName = repository.getUserByIdSync(userId)?.fullName ?: "Partner"
            repository.addNotification(
                Notification(
                    userId = partnerId,
                    title = "Swap Cancelled",
                    message = "$partnerName cancelled the swap. Reason: $reason",
                    type = "SWAP_CANCELLED",
                    relatedId = swap.id
                )
            )
            repository.addSwapEvent(
                SwapEvent(
                    swapId = swap.id,
                    actorUserId = userId,
                    eventType = "CANCELLED",
                    title = "Swap cancelled",
                    detail = reason
                )
            )
        }
    }

    fun startSwap(swap: Swap) {
        viewModelScope.launch {
            repository.updateSwap(swap.copy(status = "Ongoing"))
            repository.addSwapEvent(
                SwapEvent(
                    swapId = swap.id,
                    actorUserId = swap.userBId,
                    eventType = "STARTED",
                    title = "Work started",
                    detail = "The accepted worker started this swap"
                )
            )
        }
    }

    fun submitProof(swap: Swap, userId: Long, note: String) {
        viewModelScope.launch {
            repository.updateSwap(swap.copy(status = "Proof Submitted", proofNote = note, proofSubmittedAt = System.currentTimeMillis()))
            repository.addSwapEvent(
                SwapEvent(
                    swapId = swap.id,
                    actorUserId = userId,
                    eventType = "PROOF_SUBMITTED",
                    title = "Proof submitted",
                    detail = note.ifBlank { "Worker submitted completion proof" }
                )
            )
        }
    }

    fun completeSwapAndReview(swap: Swap, review: Review, userId: Long) {
        viewModelScope.launch {
            if (repository.hasUserReviewedSwap(swap.id, userId)) return@launch
            repository.updateSwap(swap.copy(status = "Completed", completionDate = System.currentTimeMillis()))
            repository.submitReview(review)

            val reviewedUserId = if (swap.userAId == userId) swap.userBId else swap.userAId
            repository.addSkillPointTransaction(
                SkillPointTransaction(
                    userId = reviewedUserId,
                    amount = swap.agreedHours,
                    type = "EARNED",
                    relatedSwapId = swap.id,
                    description = "Completed swap #${swap.id} (+${swap.agreedHours} SP)"
                )
            )
            repository.addSkillPointTransaction(
                SkillPointTransaction(
                    userId = userId,
                    amount = -swap.agreedHours,
                    type = "SPENT",
                    relatedSwapId = swap.id,
                    description = "Paid ${swap.agreedHours} SP for swap #${swap.id}"
                )
            )

            repository.getUserByIdSync(reviewedUserId)?.let {
                repository.updateUser(
                    it.copy(
                        completedSwaps = it.completedSwaps + 1,
                        trustScore = repository.getAverageRatingForUser(reviewedUserId) ?: it.trustScore,
                        skillPoints = repository.getSkillPointBalance(reviewedUserId),
                        reliabilityScore = TrustScoreEngine.reliabilityAfterCompletion(it)
                    )
                )
            }
            repository.getUserByIdSync(userId)?.let {
                repository.updateUser(it.copy(skillPoints = repository.getSkillPointBalance(userId)))
            }

            repository.addSwapEvent(
                SwapEvent(
                    swapId = swap.id,
                    actorUserId = userId,
                    eventType = "COMPLETED",
                    title = "Swap completed",
                    detail = "Rated ${review.rating} stars and settled ${swap.agreedHours} Skill Points"
                )
            )
            repository.addNotification(
                Notification(
                    userId = reviewedUserId,
                    title = "Swap Completed",
                    message = "You received a ${review.rating}-star review and +${swap.agreedHours} Skill Points.",
                    type = "SWAP_COMPLETED",
                    relatedId = swap.id
                )
            )
            repository.getNeedById(swap.needPostId)?.let { repository.updateNeedPost(it.copy(status = "Fulfilled")) }
        }
    }

    fun hasUserReviewedSwap(swapId: Long, userId: Long, callback: (Boolean) -> Unit) {
        viewModelScope.launch { callback(repository.hasUserReviewedSwap(swapId, userId)) }
    }

    fun getMessages(userId: Long, otherId: Long) = repository.getMessages(userId, otherId)
    fun sendMessage(message: com.example.skillexchangeapp.data.local.entity.Message) {
        viewModelScope.launch { repository.sendMessage(message) }
    }

    fun getNotifications(userId: Long) = repository.getNotifications(userId)
    fun getUnreadNotificationCount(userId: Long) = repository.getUnreadNotificationCount(userId)
    fun markAllNotificationsRead(userId: Long) {
        viewModelScope.launch { repository.markAllNotificationsRead(userId) }
    }
    fun markNotificationRead(id: Long) {
        viewModelScope.launch { repository.markNotificationRead(id) }
    }

    fun getAIMatchesForNeed(needPost: NeedPost, currentUserId: Long, callback: (List<SkillMatchingEngine.MatchResult>) -> Unit) {
        viewModelScope.launch {
            val users = repository.getAvailableUsersExcluding(currentUserId)
            callback(SkillMatchingEngine.findMatchesForNeed(needPost, users, currentUserId))
        }
    }

    fun getWorkerMatchesForNeed(needPost: NeedPost, callback: (List<AiMatchEngine.WorkerMatch>) -> Unit) {
        viewModelScope.launch {
            callback(AiMatchEngine.findWorkersForNeed(needPost, repository.getAllUsersSync(), needPost.userId))
        }
    }

    fun getRankedNeedsForUser(userId: Long, callback: (List<AiMatchEngine.NeedRecommendation>) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByIdSync(userId) ?: return@launch
            callback(AiMatchEngine.rankNeedsForUser(user, repository.getAllOpenNeedsSync()))
        }
    }

    fun getBarterSuggestions(currentUserId: Long, callback: (List<BarterSuggestionEngine.BarterSuggestion>) -> Unit) {
        viewModelScope.launch {
            val currentUser = repository.getUserByIdSync(currentUserId) ?: return@launch
            val myNeeds = repository.getOpenNeedsByUserSync(currentUserId)
            val allNeeds = repository.getAllOpenNeedsSync()
            val allUsers = repository.getAllUsersSync()
            callback(BarterSuggestionEngine.findBarterOpportunities(currentUser, myNeeds, allNeeds, allUsers))
        }
    }

    fun getSwapPartnerName(swap: Swap, currentUserId: Long, callback: (String) -> Unit) {
        viewModelScope.launch {
            val partnerId = if (swap.userAId == currentUserId) swap.userBId else swap.userAId
            callback(repository.getUserByIdSync(partnerId)?.fullName ?: "Unknown")
        }
    }

    fun getSwapPartnerUser(swap: Swap, currentUserId: Long, callback: (User?) -> Unit) {
        viewModelScope.launch {
            val partnerId = if (swap.userAId == currentUserId) swap.userBId else swap.userAId
            callback(repository.getUserByIdSync(partnerId))
        }
    }

    fun getNeedForSwap(swap: Swap, callback: (NeedPost?) -> Unit) {
        viewModelScope.launch { callback(repository.getNeedById(swap.needPostId)) }
    }

    fun getSwapEvents(swapId: Long) = repository.getSwapEvents(swapId)

    fun getUserStats(userId: Long, callback: (Int, Float?, Int) -> Unit) {
        viewModelScope.launch {
            val points = repository.getSkillPointBalance(userId)
            val avgRating = repository.getAverageRatingForUser(userId)
            val reviewCount = repository.getReviewCountForUser(userId)
            callback(points, avgRating, reviewCount)
        }
    }

    fun getSkillPointHistory(userId: Long) = repository.getSkillPointHistory(userId)

    fun getImpactSnapshot(callback: (ImpactAnalyticsEngine.CommunityImpact) -> Unit) {
        viewModelScope.launch {
            val impact = ImpactAnalyticsEngine.calculate(
                users = repository.getAllUsersSync(),
                needs = repository.getAllNeedsSync(),
                swaps = repository.getAllSwapsSync()
            )
            repository.addImpactMetric(ImpactAnalyticsEngine.toMetric(impact))
            callback(impact)
        }
    }

    fun requestVerification(userId: Long, skill: String, proofText: String) {
        viewModelScope.launch {
            repository.requestVerification(VerificationRequest(userId = userId, skill = skill, proofText = proofText))
            repository.getUserByIdSync(userId)?.let { repository.updateUser(it.copy(verificationStatus = "Pending")) }
        }
    }

    fun getPendingVerificationRequests() = repository.getPendingVerificationRequests()
    fun getAllVerificationRequests() = repository.getAllVerificationRequests()
    fun getOpenReports() = repository.getOpenReports()
    fun getAllReports() = repository.getAllReports()
    fun getRecentAdminActions() = repository.getRecentAdminActions()

    fun reviewVerificationRequest(request: VerificationRequest, adminId: Long, approved: Boolean, notes: String) {
        viewModelScope.launch {
            repository.updateVerificationRequest(
                request.copy(
                    status = if (approved) "Approved" else "Rejected",
                    reviewerId = adminId,
                    reviewerNotes = notes,
                    reviewedAt = System.currentTimeMillis()
                )
            )
            repository.getUserByIdSync(request.userId)?.let {
                repository.updateUser(
                    it.copy(
                        verificationStatus = if (approved) "Verified" else "Rejected",
                        reliabilityScore = if (approved) TrustScoreEngine.verificationBoost(it) else it.reliabilityScore
                    )
                )
            }
            val notifMessage = if (approved)
                "Your ${request.skill} skill is now verified! ✅"
            else
                "Verification rejected. Notes: $notes"
            repository.addNotification(
                Notification(
                    userId = request.userId,
                    title = if (approved) "✅ Verification Approved!" else "Verification Update",
                    message = notifMessage,
                    type = "REVIEW_RECEIVED",
                    relatedId = request.id
                )
            )
            repository.addAdminAction(
                AdminAction(
                    adminUserId = adminId,
                    actionType = if (approved) "VERIFY_APPROVE" else "VERIFY_REJECT",
                    targetType = "VerificationRequest",
                    targetId = request.id,
                    notes = notes
                )
            )
        }
    }

    fun submitReport(report: Report) {
        viewModelScope.launch {
            repository.submitReport(report)
            report.reportedUserId?.let { reportedId ->
                repository.getUserByIdSync(reportedId)?.let { user ->
                    repository.updateUser(user.copy(reportCount = user.reportCount + 1))
                }
            }
        }
    }

    fun resolveReport(report: Report, adminId: Long, resolution: String) {
        viewModelScope.launch {
            repository.updateReport(report.copy(status = "Resolved", resolution = resolution, resolvedAt = System.currentTimeMillis()))
            repository.addAdminAction(
                AdminAction(
                    adminUserId = adminId,
                    actionType = "REPORT_RESOLVED",
                    targetType = "Report",
                    targetId = report.id,
                    notes = resolution
                )
            )
        }
    }
}
