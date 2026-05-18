package com.example.skillexchangeapp.ai

import com.example.skillexchangeapp.data.local.entity.User

object TrustScoreEngine {
    fun cancellationPenalty(currentTrust: Float): Float = (currentTrust - 0.3f).coerceAtLeast(0f)

    fun reliabilityAfterCompletion(user: User): Int {
        val base = user.reliabilityScore
        val completionBoost = if (user.completedSwaps >= 10) 4 else 2
        val verifiedBoost = if (user.verificationStatus == "Verified") 3 else 0
        return (base + completionBoost + verifiedBoost).coerceIn(0, 100)
    }

    fun reliabilityAfterCancellation(user: User): Int = (user.reliabilityScore - 8).coerceIn(0, 100)

    fun verificationBoost(user: User): Int = (user.reliabilityScore + 10).coerceIn(0, 100)

    fun trustLabel(user: User): String = when {
        user.verificationStatus == "Verified" && user.reliabilityScore >= 85 -> "Verified Pro"
        user.reliabilityScore >= 75 -> "Reliable"
        user.completedSwaps == 0 -> "New Worker"
        else -> "Needs Consistency"
    }
}
