package com.example.skillexchangeapp.data.firebase

import com.example.skillexchangeapp.data.local.entity.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object FirebaseService {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val realtimeDb: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }

    // ── Collections ──
    private val usersCol get() = firestore.collection("users")
    private val needsCol get() = firestore.collection("need_posts")
    private val offersCol get() = firestore.collection("offers")
    private val swapsCol get() = firestore.collection("swaps")
    private val reviewsCol get() = firestore.collection("reviews")
    private val notificationsCol get() = firestore.collection("notifications")
    private val spTransactionsCol get() = firestore.collection("sp_transactions")
    private val verificationCol get() = firestore.collection("verification_requests")
    private val reportsCol get() = firestore.collection("reports")
    private val adminActionsCol get() = firestore.collection("admin_actions")
    private val swapEventsCol get() = firestore.collection("swap_events")
    private val messagesRef get() = realtimeDb.getReference("messages")

    // ─────────────────────────────────────────────────────────────────────────
    // AUTH
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Signs in with email/password. Returns Firebase UID on success.
     */
    suspend fun signIn(email: String, password: String): String {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw Exception("Sign-in failed")
    }

    /**
     * Creates a Firebase Auth account. Returns UID.
     * Silently ignores EmailAlreadyInUse (user already has account from seeding).
     */
    suspend fun createAuthAccount(email: String, password: String): String {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.uid ?: throw Exception("Account creation failed")
        } catch (e: FirebaseAuthUserCollisionException) {
            // Account already exists — sign in instead
            signIn(email, password)
        }
    }

    fun signOut() = auth.signOut()

    fun isSignedIn(): Boolean = auth.currentUser != null
    fun currentUid(): String? = auth.currentUser?.uid

    // ─────────────────────────────────────────────────────────────────────────
    // USERS
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun saveUser(user: User, localId: Long) {
        val map = userToMap(user, localId)
        usersCol.document(localId.toString()).set(map, SetOptions.merge()).await()
    }

    suspend fun getFirestoreUserByEmail(email: String): Map<String, Any>? {
        val snap = usersCol.whereEqualTo("email", email).limit(1).get().await()
        return if (snap.isEmpty) null else snap.documents[0].data
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NEED POSTS
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun saveNeedPost(post: NeedPost) {
        needsCol.document(post.id.toString()).set(needToMap(post), SetOptions.merge()).await()
    }

    suspend fun updateNeedPost(post: NeedPost) = saveNeedPost(post)

    // ─────────────────────────────────────────────────────────────────────────
    // OFFERS
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun saveOffer(offer: Offer) {
        offersCol.document(offer.id.toString()).set(offerToMap(offer), SetOptions.merge()).await()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SWAPS
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun saveSwap(swap: Swap) {
        swapsCol.document(swap.id.toString()).set(swapToMap(swap), SetOptions.merge()).await()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REVIEWS
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun saveReview(review: Review) {
        reviewsCol.document(review.id.toString()).set(reviewToMap(review), SetOptions.merge()).await()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NOTIFICATIONS
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun saveNotification(notification: Notification) {
        notificationsCol.document(notification.id.toString())
            .set(notifToMap(notification), SetOptions.merge()).await()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SKILL POINTS
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun saveSpTransaction(tx: SkillPointTransaction) {
        spTransactionsCol.document(tx.id.toString()).set(spTxToMap(tx), SetOptions.merge()).await()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VERIFICATION
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun saveVerificationRequest(req: VerificationRequest) {
        verificationCol.document(req.id.toString()).set(verToMap(req), SetOptions.merge()).await()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REPORTS
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun saveReport(report: Report) {
        reportsCol.document(report.id.toString()).set(reportToMap(report), SetOptions.merge()).await()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADMIN ACTIONS
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun saveAdminAction(action: AdminAction) {
        adminActionsCol.document(action.id.toString()).set(adminActionToMap(action), SetOptions.merge()).await()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SWAP EVENTS
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun saveSwapEvent(event: SwapEvent) {
        swapEventsCol.document(event.id.toString()).set(swapEventToMap(event), SetOptions.merge()).await()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REALTIME CHAT MESSAGES
    // ─────────────────────────────────────────────────────────────────────────

    fun sendRealtimeMessage(message: Message) {
        val chatKey = getChatKey(message.senderId, message.receiverId)
        val ref = messagesRef.child(chatKey).push()
        ref.setValue(
            mapOf(
                "id" to message.id,
                "senderId" to message.senderId,
                "receiverId" to message.receiverId,
                "content" to message.content,
                "timestamp" to message.timestamp
            )
        )
    }

    fun listenToMessages(userId: Long, otherId: Long): Flow<List<Map<String, Any>>> = callbackFlow {
        val chatKey = getChatKey(userId, otherId)
        val ref = messagesRef.child(chatKey)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Map<String, Any>>()
                for (child in snapshot.children) {
                    @Suppress("UNCHECKED_CAST")
                    val msg = child.value as? Map<String, Any> ?: continue
                    messages.add(msg)
                }
                trySend(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                // Ignore — Room fallback will be used
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    private fun getChatKey(id1: Long, id2: Long): String {
        return if (id1 < id2) "${id1}_${id2}" else "${id2}_${id1}"
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MAPPING HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private fun userToMap(user: User, localId: Long) = mapOf(
        "id" to localId,
        "fullName" to user.fullName,
        "email" to user.email,
        "phone" to user.phone,
        "village" to user.village,
        "primarySkill" to user.primarySkill,
        "secondarySkills" to user.secondarySkills,
        "experienceYears" to user.experienceYears,
        "trustScore" to user.trustScore,
        "completedSwaps" to user.completedSwaps,
        "skillPoints" to user.skillPoints,
        "bio" to user.bio,
        "isAvailable" to user.isAvailable,
        "verificationStatus" to user.verificationStatus,
        "reliabilityScore" to user.reliabilityScore,
        "portfolioSummary" to user.portfolioSummary,
        "reportCount" to user.reportCount,
        "isAdmin" to user.isAdmin,
        "createdAt" to user.createdAt
    )

    private fun needToMap(post: NeedPost) = mapOf(
        "id" to post.id,
        "userId" to post.userId,
        "title" to post.title,
        "description" to post.description,
        "skillRequired" to post.skillRequired,
        "estimatedHours" to post.estimatedHours,
        "urgencyLevel" to post.urgencyLevel,
        "location" to post.location,
        "status" to post.status,
        "createdAt" to post.createdAt,
        "deadline" to post.deadline,
        "offerCount" to post.offerCount,
        "requiresVerifiedWorker" to post.requiresVerifiedWorker
    )

    private fun offerToMap(offer: Offer) = mapOf(
        "id" to offer.id,
        "needPostId" to offer.needPostId,
        "offeredByUserId" to offer.offeredByUserId,
        "offeredSkill" to offer.offeredSkill,
        "offeredHours" to offer.offeredHours,
        "message" to offer.message,
        "status" to offer.status,
        "createdAt" to offer.createdAt,
        "matchScore" to offer.matchScore,
        "aiDraftUsed" to offer.aiDraftUsed
    )

    private fun swapToMap(swap: Swap) = mapOf(
        "id" to swap.id,
        "userAId" to swap.userAId,
        "userBId" to swap.userBId,
        "needPostId" to swap.needPostId,
        "agreedHours" to swap.agreedHours,
        "status" to swap.status,
        "completionDate" to swap.completionDate,
        "scheduledDate" to swap.scheduledDate,
        "scheduledTime" to swap.scheduledTime,
        "cancellationReason" to swap.cancellationReason,
        "cancelledBy" to swap.cancelledBy,
        "proofNote" to swap.proofNote,
        "proofSubmittedAt" to swap.proofSubmittedAt,
        "disputeStatus" to swap.disputeStatus
    )

    private fun reviewToMap(review: Review) = mapOf(
        "id" to review.id,
        "swapId" to review.swapId,
        "reviewerId" to review.reviewerId,
        "rating" to review.rating,
        "comment" to review.comment
    )

    private fun notifToMap(n: Notification) = mapOf(
        "id" to n.id,
        "userId" to n.userId,
        "title" to n.title,
        "message" to n.message,
        "type" to n.type,
        "relatedId" to n.relatedId,
        "isRead" to n.isRead,
        "timestamp" to n.timestamp
    )

    private fun spTxToMap(tx: SkillPointTransaction) = mapOf(
        "id" to tx.id,
        "userId" to tx.userId,
        "amount" to tx.amount,
        "type" to tx.type,
        "relatedSwapId" to tx.relatedSwapId,
        "description" to tx.description,
        "timestamp" to tx.timestamp
    )

    private fun verToMap(req: VerificationRequest) = mapOf(
        "id" to req.id,
        "userId" to req.userId,
        "skill" to req.skill,
        "proofText" to req.proofText,
        "status" to req.status,
        "reviewerNotes" to req.reviewerNotes,
        "createdAt" to req.createdAt,
        "reviewedAt" to req.reviewedAt
    )

    private fun reportToMap(report: Report) = mapOf(
        "id" to report.id,
        "reporterId" to report.reporterId,
        "reportedUserId" to report.reportedUserId,
        "needPostId" to report.needPostId,
        "swapId" to report.swapId,
        "reason" to report.reason,
        "details" to report.details,
        "status" to report.status,
        "resolution" to report.resolution,
        "createdAt" to report.createdAt,
        "resolvedAt" to report.resolvedAt
    )

    private fun adminActionToMap(action: AdminAction) = mapOf(
        "id" to action.id,
        "adminUserId" to action.adminUserId,
        "actionType" to action.actionType,
        "targetType" to action.targetType,
        "targetId" to action.targetId,
        "notes" to action.notes,
        "createdAt" to action.createdAt
    )

    private fun swapEventToMap(event: SwapEvent) = mapOf(
        "id" to event.id,
        "swapId" to event.swapId,
        "actorUserId" to event.actorUserId,
        "eventType" to event.eventType,
        "title" to event.title,
        "detail" to event.detail,
        "timestamp" to event.timestamp
    )
}
