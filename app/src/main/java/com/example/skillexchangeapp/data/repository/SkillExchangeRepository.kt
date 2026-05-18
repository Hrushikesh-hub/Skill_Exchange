package com.example.skillexchangeapp.data.repository

import com.example.skillexchangeapp.data.firebase.FirebaseService
import com.example.skillexchangeapp.data.local.dao.*
import com.example.skillexchangeapp.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SkillExchangeRepository(
    private val userDao: UserDao,
    private val skillDao: SkillDao,
    private val needPostDao: NeedPostDao,
    private val offerDao: OfferDao,
    private val swapDao: SwapDao,
    private val reviewDao: ReviewDao,
    private val messageDao: MessageDao,
    private val skillPointDao: SkillPointDao,
    private val notificationDao: NotificationDao,
    private val verificationRequestDao: VerificationRequestDao,
    private val swapEventDao: SwapEventDao,
    private val reportDao: ReportDao,
    private val adminActionDao: AdminActionDao,
    private val impactMetricDao: ImpactMetricDao
) {
    private val ioScope = CoroutineScope(Dispatchers.IO)

    /**
     * Safely syncs data to Firestore in background — failures are silent to not block the UI.
     */
    private fun syncToFirebase(block: suspend () -> Unit) {
        ioScope.launch {
            try { block() } catch (_: Exception) { /* Firebase offline tolerance */ }
        }
    }

    // ── User ──
    suspend fun registerUser(user: User): Long {
        val userId = userDao.insertUser(user)
        val saved = userDao.getUserByIdSync(userId) ?: user.copy()
        syncToFirebase { FirebaseService.saveUser(saved, userId) }
        return userId
    }

    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)
    fun getUserById(id: Long): Flow<User?> = userDao.getUserById(id)
    suspend fun getUserByIdSync(id: Long): User? = userDao.getUserByIdSync(id)
    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
        syncToFirebase { FirebaseService.saveUser(user, user.id) }
    }
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()
    suspend fun getAllUsersSync(): List<User> = userDao.getAllUsersSync()
    suspend fun getAvailableUsersExcluding(userId: Long): List<User> = userDao.getAvailableUsersExcluding(userId)
    suspend fun getUserCount(): Int = userDao.getUserCount()
    suspend fun getActiveWorkerCount(): Int = userDao.getActiveWorkerCount()
    suspend fun getPlatformAverageTrustScore(): Float = userDao.getAverageTrustScore()

    // ── Skill ──
    suspend fun addSkill(skill: Skill) = skillDao.insertSkill(skill)
    fun getSkillsForUser(userId: Long) = skillDao.getSkillsForUser(userId)

    // ── Need Post ──
    suspend fun postNeed(post: NeedPost): Long {
        val postId = needPostDao.insertNeedPost(post)
        val saved = needPostDao.getNeedById(postId) ?: post
        syncToFirebase { FirebaseService.saveNeedPost(saved) }
        return postId
    }
    fun getAllOpenNeeds(): Flow<List<NeedPost>> = needPostDao.getAllOpenNeeds()
    suspend fun getAllOpenNeedsSync(): List<NeedPost> = needPostDao.getAllOpenNeedsSync()
    fun getNeedsByUser(userId: Long): Flow<List<NeedPost>> = needPostDao.getNeedsByUser(userId)
    suspend fun getOpenNeedsByUserSync(userId: Long): List<NeedPost> = needPostDao.getOpenNeedsByUserSync(userId)
    suspend fun getAllNeedsSync(): List<NeedPost> = needPostDao.getAllNeedsSync()
    suspend fun getNeedById(id: Long): NeedPost? = needPostDao.getNeedById(id)
    fun searchOpenNeeds(query: String): Flow<List<NeedPost>> = needPostDao.searchOpenNeeds(query)
    fun getFilteredNeedsBySkill(skill: String): Flow<List<NeedPost>> = needPostDao.getFilteredNeedsBySkill(skill)
    suspend fun updateNeedPost(post: NeedPost) {
        needPostDao.updateNeedPost(post)
        syncToFirebase { FirebaseService.saveNeedPost(post) }
    }
    suspend fun getTopDemandedSkill(): String = needPostDao.getTopDemandedSkill() ?: "Plumbing"

    // ── Offer ──
    suspend fun submitOffer(offer: Offer) {
        offerDao.insertOffer(offer)
        syncToFirebase {
            // Fetch with ID so we have the auto-generated PK
            val allOffers = offerDao.getOffersForPostSync(offer.needPostId)
            val saved = allOffers.lastOrNull { it.offeredByUserId == offer.offeredByUserId } ?: offer
            FirebaseService.saveOffer(saved)
        }
    }
    fun getOffersForPost(postId: Long): Flow<List<Offer>> = offerDao.getOffersForPost(postId)
    suspend fun updateOffer(offer: Offer) {
        offerDao.updateOffer(offer)
        syncToFirebase { FirebaseService.saveOffer(offer) }
    }
    fun getPendingOffersForUserPosts(userId: Long): Flow<List<Offer>> = offerDao.getPendingOffersForUserPosts(userId)
    fun getPendingOfferCount(userId: Long): Flow<Int> = offerDao.getPendingOfferCount(userId)
    suspend fun rejectOtherOffers(postId: Long, acceptedOfferId: Long) = offerDao.rejectOtherOffers(postId, acceptedOfferId)

    // ── Swap ──
    suspend fun createSwap(swap: Swap): Long {
        val swapId = swapDao.insertSwap(swap)
        val saved = swapDao.getSwapById(swapId) ?: swap
        syncToFirebase { FirebaseService.saveSwap(saved) }
        return swapId
    }
    fun getSwapsForUser(userId: Long): Flow<List<Swap>> = swapDao.getSwapsForUser(userId)
    suspend fun updateSwap(swap: Swap) {
        swapDao.updateSwap(swap)
        syncToFirebase { FirebaseService.saveSwap(swap) }
    }
    suspend fun getSwapById(id: Long): Swap? = swapDao.getSwapById(id)
    suspend fun getAllSwapsSync(): List<Swap> = swapDao.getAllSwapsSync()
    suspend fun getCompletedSwapsSync(): List<Swap> = swapDao.getCompletedSwapsSync()

    // ── Review ──
    suspend fun submitReview(review: Review) {
        reviewDao.insertReview(review)
        syncToFirebase { FirebaseService.saveReview(review) }
    }
    fun getReviewsForSwap(swapId: Long) = reviewDao.getReviewsForSwap(swapId)
    suspend fun getAverageRatingForUser(userId: Long): Float? = reviewDao.getAverageRatingForUser(userId)
    suspend fun getReviewCountForUser(userId: Long): Int = reviewDao.getReviewCountForUser(userId)
    suspend fun hasUserReviewedSwap(swapId: Long, reviewerId: Long): Boolean = reviewDao.hasUserReviewedSwap(swapId, reviewerId) > 0

    // ── Message ──
    suspend fun sendMessage(message: Message) {
        messageDao.insertMessage(message)
        // Also send to Firebase Realtime DB for real-time delivery
        syncToFirebase { FirebaseService.sendRealtimeMessage(message) }
    }
    fun getMessages(userId: Long, otherId: Long) = messageDao.getMessagesBetweenUsers(userId, otherId)

    // ── Skill Points ──
    suspend fun addSkillPointTransaction(transaction: SkillPointTransaction) {
        skillPointDao.insertTransaction(transaction)
        syncToFirebase { FirebaseService.saveSpTransaction(transaction) }
    }
    suspend fun getSkillPointBalance(userId: Long): Int = skillPointDao.getBalance(userId)
    suspend fun getBalanceSync(userId: Long): Int = skillPointDao.getBalance(userId)
    suspend fun getEarnedTotal(userId: Long): Int = skillPointDao.getEarnedTotal(userId)
    suspend fun getSpentTotal(userId: Long): Int = skillPointDao.getSpentTotal(userId)
    fun getSkillPointHistory(userId: Long): Flow<List<SkillPointTransaction>> = skillPointDao.getTransactionHistory(userId)
    suspend fun getAllSkillPointTransactionsSync(): List<SkillPointTransaction> = skillPointDao.getAllTransactionsSync()

    // ── Notifications ──
    suspend fun addNotification(notification: Notification) {
        notificationDao.insertNotification(notification)
        syncToFirebase { FirebaseService.saveNotification(notification) }
    }
    fun getNotifications(userId: Long): Flow<List<Notification>> = notificationDao.getNotificationsForUser(userId)
    fun getUnreadNotificationCount(userId: Long): Flow<Int> = notificationDao.getUnreadCount(userId)
    suspend fun markAllNotificationsRead(userId: Long) = notificationDao.markAllRead(userId)
    suspend fun markNotificationRead(id: Long) = notificationDao.markAsRead(id)

    // Verification and admin workflows
    suspend fun requestVerification(request: VerificationRequest): Long {
        val id = verificationRequestDao.insertRequest(request)
        val saved = verificationRequestDao.getRequestById(id) ?: request
        syncToFirebase { FirebaseService.saveVerificationRequest(saved) }
        return id
    }
    suspend fun updateVerificationRequest(request: VerificationRequest) {
        verificationRequestDao.updateRequest(request)
        syncToFirebase { FirebaseService.saveVerificationRequest(request) }
    }
    fun getAllVerificationRequests(): Flow<List<VerificationRequest>> = verificationRequestDao.getAllRequests()
    fun getPendingVerificationRequests(): Flow<List<VerificationRequest>> = verificationRequestDao.getPendingRequests()
    fun getVerificationRequestsForUser(userId: Long): Flow<List<VerificationRequest>> = verificationRequestDao.getRequestsForUser(userId)
    suspend fun getVerificationRequestById(id: Long): VerificationRequest? = verificationRequestDao.getRequestById(id)

    suspend fun addSwapEvent(event: SwapEvent): Long {
        val id = swapEventDao.insertEvent(event)
        val saved = swapEventDao.getEventById(id) ?: event
        syncToFirebase { FirebaseService.saveSwapEvent(saved) }
        return id
    }
    fun getSwapEvents(swapId: Long): Flow<List<SwapEvent>> = swapEventDao.getEventsForSwap(swapId)
    suspend fun getSwapEventsSync(swapId: Long): List<SwapEvent> = swapEventDao.getEventsForSwapSync(swapId)

    suspend fun submitReport(report: Report): Long {
        val id = reportDao.insertReport(report)
        val saved = reportDao.getReportById(id) ?: report
        syncToFirebase { FirebaseService.saveReport(saved) }
        return id
    }
    suspend fun updateReport(report: Report) {
        reportDao.updateReport(report)
        syncToFirebase { FirebaseService.saveReport(report) }
    }
    fun getAllReports(): Flow<List<Report>> = reportDao.getAllReports()
    fun getOpenReports(): Flow<List<Report>> = reportDao.getOpenReports()
    suspend fun getReportById(id: Long): Report? = reportDao.getReportById(id)

    suspend fun addAdminAction(action: AdminAction): Long {
        val id = adminActionDao.insertAction(action)
        val saved = adminActionDao.getActionById(id) ?: action
        syncToFirebase { FirebaseService.saveAdminAction(saved) }
        return id
    }
    fun getRecentAdminActions(): Flow<List<AdminAction>> = adminActionDao.getRecentActions()

    suspend fun addImpactMetric(metric: ImpactMetric): Long = impactMetricDao.insertMetric(metric)
    fun getLatestImpactMetric(): Flow<ImpactMetric?> = impactMetricDao.getLatestMetric()
}
