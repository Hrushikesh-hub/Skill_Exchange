package com.example.skillexchangeapp.data.firebase

import com.example.skillexchangeapp.data.local.AppDatabase
import com.example.skillexchangeapp.data.local.entity.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Listens to Firestore collections in real-time and writes changes into the local Room database.
 * This is what makes two-device sync work: Device A writes → Firestore → Device B receives here.
 */
object FirebaseSyncManager {

    private val db = FirebaseFirestore.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val listeners = mutableListOf<ListenerRegistration>()

    fun startSync(appDb: AppDatabase) {
        stopSync() // Clean up any existing listeners first

        // ── Need Posts ──────────────────────────────────────────────────────
        listeners += db.collection("need_posts")
            .addSnapshotListener { snap, _ ->
                snap ?: return@addSnapshotListener
                scope.launch {
                    for (doc in snap.documents) {
                        val d = doc.data ?: continue
                        runCatching {
                            appDb.needPostDao().syncInsertNeedPost(
                                NeedPost(
                                    id = (d["id"] as? Long) ?: doc.id.toLongOrNull() ?: continue,
                                    userId = d.long("userId"),
                                    title = d.str("title"),
                                    description = d.str("description"),
                                    skillRequired = d.str("skillRequired"),
                                    estimatedHours = d.int("estimatedHours"),
                                    urgencyLevel = d.str("urgencyLevel"),
                                    location = d.str("location"),
                                    status = d.str("status", "Open"),
                                    createdAt = d.long("createdAt"),
                                    deadline = d["deadline"] as? Long,
                                    offerCount = d.int("offerCount"),
                                    requiresVerifiedWorker = d["requiresVerifiedWorker"] as? Boolean ?: false
                                )
                            )
                        }
                    }
                }
            }

        // ── Offers ──────────────────────────────────────────────────────────
        listeners += db.collection("offers")
            .addSnapshotListener { snap, _ ->
                snap ?: return@addSnapshotListener
                scope.launch {
                    for (doc in snap.documents) {
                        val d = doc.data ?: continue
                        runCatching {
                            appDb.offerDao().syncInsertOffer(
                                Offer(
                                    id = d.long("id"),
                                    needPostId = d.long("needPostId"),
                                    offeredByUserId = d.long("offeredByUserId"),
                                    offeredSkill = d.str("offeredSkill"),
                                    offeredHours = d.int("offeredHours"),
                                    message = d.str("message"),
                                    status = d.str("status", "Pending"),
                                    createdAt = d.long("createdAt"),
                                    matchScore = d.int("matchScore"),
                                    aiDraftUsed = d["aiDraftUsed"] as? Boolean ?: false
                                )
                            )
                        }
                    }
                }
            }

        // ── Swaps ────────────────────────────────────────────────────────────
        listeners += db.collection("swaps")
            .addSnapshotListener { snap, _ ->
                snap ?: return@addSnapshotListener
                scope.launch {
                    for (doc in snap.documents) {
                        val d = doc.data ?: continue
                        runCatching {
                            appDb.swapDao().syncInsertSwap(
                                Swap(
                                    id = d.long("id"),
                                    userAId = d.long("userAId"),
                                    userBId = d.long("userBId"),
                                    needPostId = d.long("needPostId"),
                                    agreedHours = d.int("agreedHours"),
                                    status = d.str("status", "Scheduled"),
                                    completionDate = d["completionDate"] as? Long,
                                    scheduledDate = d["scheduledDate"] as? Long,
                                    scheduledTime = d["scheduledTime"] as? String,
                                    cancellationReason = d["cancellationReason"] as? String,
                                    cancelledBy = d["cancelledBy"] as? Long,
                                    proofNote = d["proofNote"] as? String,
                                    proofSubmittedAt = d["proofSubmittedAt"] as? Long,
                                    disputeStatus = d.str("disputeStatus", "None")
                                )
                            )
                        }
                    }
                }
            }

        // ── Users ────────────────────────────────────────────────────────────
        listeners += db.collection("users")
            .addSnapshotListener { snap, _ ->
                snap ?: return@addSnapshotListener
                scope.launch {
                    for (doc in snap.documents) {
                        val d = doc.data ?: continue
                        runCatching {
                            appDb.userDao().syncInsertUser(
                                User(
                                    id = d.long("id"),
                                    fullName = d.str("fullName"),
                                    email = d.str("email"),
                                    phone = d.str("phone"),
                                    village = d.str("village"),
                                    primarySkill = d.str("primarySkill"),
                                    secondarySkills = d.str("secondarySkills"),
                                    experienceYears = d.int("experienceYears"),
                                    trustScore = (d["trustScore"] as? Number)?.toFloat() ?: 0f,
                                    completedSwaps = d.int("completedSwaps"),
                                    skillPoints = d.int("skillPoints"),
                                    bio = d.str("bio"),
                                    isAvailable = d["isAvailable"] as? Boolean ?: true,
                                    verificationStatus = d.str("verificationStatus", "Unverified"),
                                    reliabilityScore = d.int("reliabilityScore", 100),
                                    portfolioSummary = d.str("portfolioSummary"),
                                    reportCount = d.int("reportCount"),
                                    isAdmin = d["isAdmin"] as? Boolean ?: false,
                                    createdAt = d.long("createdAt"),
                                    passwordHash = "" // Never synced — stays local-only
                                )
                            )
                        }
                    }
                }
            }

        // ── Notifications ─────────────────────────────────────────────────────
        listeners += db.collection("notifications")
            .addSnapshotListener { snap, _ ->
                snap ?: return@addSnapshotListener
                scope.launch {
                    for (doc in snap.documents) {
                        val d = doc.data ?: continue
                        runCatching {
                            appDb.notificationDao().syncInsertNotification(
                                Notification(
                                    id = d.long("id"),
                                    userId = d.long("userId"),
                                    title = d.str("title"),
                                    message = d.str("message"),
                                    type = d.str("type"),
                                    relatedId = d["relatedId"] as? Long,
                                    isRead = d["isRead"] as? Boolean ?: false,
                                    timestamp = d.long("timestamp")
                                )
                            )
                        }
                    }
                }
            }
    }

    fun stopSync() {
        listeners.forEach { it.remove() }
        listeners.clear()
    }

    // ── Type-safe helpers ────────────────────────────────────────────────────

    private fun Map<String, Any>.long(key: String, default: Long = 0L): Long =
        (this[key] as? Number)?.toLong() ?: default

    private fun Map<String, Any>.int(key: String, default: Int = 0): Int =
        (this[key] as? Number)?.toInt() ?: default

    private fun Map<String, Any>.str(key: String, default: String = ""): String =
        this[key] as? String ?: default
}
