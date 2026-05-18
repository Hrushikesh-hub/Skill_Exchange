package com.example.skillexchangeapp.ai

import com.example.skillexchangeapp.data.local.entity.NeedPost
import com.example.skillexchangeapp.data.local.entity.User
import kotlin.math.abs

object AiMatchEngine {
    data class NeedRecommendation(
        val need: NeedPost,
        val score: Int,
        val label: String,
        val explanation: String
    )

    data class WorkerMatch(
        val user: User,
        val score: Int,
        val explanation: String
    )

    fun rankNeedsForUser(currentUser: User, needs: List<NeedPost>): List<NeedRecommendation> {
        return needs
            .filter { it.userId != currentUser.id && it.status == "Open" }
            .map { need ->
                val skill = skillSimilarity(currentUser.allSkills(), need.skillRequired)
                val village = if (currentUser.village.equals(need.location, ignoreCase = true)) 1f else 0.35f
                val urgency = urgencyWeight(need.urgencyLevel)
                val verificationFit = if (!need.requiresVerifiedWorker || currentUser.verificationStatus == "Verified") 1f else 0.35f
                val trust = normalizedTrust(currentUser)
                val score = ((skill * 0.42f + village * 0.18f + urgency * 0.16f + trust * 0.14f + verificationFit * 0.10f) * 100).toInt()
                val label = when {
                    score >= 85 -> "Best for you"
                    score >= 65 -> "Strong match"
                    score >= 45 -> "Good opportunity"
                    else -> "Nearby need"
                }
                NeedRecommendation(
                    need = need,
                    score = score.coerceIn(0, 100),
                    label = label,
                    explanation = explainNeedMatch(currentUser, need, score)
                )
            }
            .sortedWith(compareByDescending<NeedRecommendation> { it.score }.thenByDescending { urgencyWeight(it.need.urgencyLevel) })
    }

    fun findWorkersForNeed(need: NeedPost, users: List<User>, posterId: Long): List<WorkerMatch> {
        return users
            .filter { it.id != posterId && it.isAvailable }
            .map { user ->
                val skill = skillSimilarity(user.allSkills(), need.skillRequired)
                val trust = normalizedTrust(user)
                val experience = (user.experienceYears / 12f).coerceIn(0f, 1f)
                val verified = if (user.verificationStatus == "Verified") 1f else 0.55f
                val locality = if (user.village.equals(need.location, ignoreCase = true)) 1f else 0.45f
                val score = ((skill * 0.42f + trust * 0.22f + experience * 0.16f + verified * 0.12f + locality * 0.08f) * 100).toInt()
                WorkerMatch(user, score.coerceIn(0, 100), explainWorkerMatch(user, need, score))
            }
            .filter { it.score >= 30 }
            .sortedByDescending { it.score }
            .take(5)
    }

    fun explainOfferFit(user: User, need: NeedPost): String {
        val score = (skillSimilarity(user.allSkills(), need.skillRequired) * 100).toInt()
        val trustText = if (user.completedSwaps > 0) "${"%.1f".format(user.trustScore)} trust" else "new but available"
        val verified = if (user.verificationStatus == "Verified") "Verified worker" else "Verification pending"
        return "$verified. ${user.primarySkill} fits ${need.skillRequired} at $score%. $trustText, ${user.experienceYears} years experience."
    }

    fun skillSimilarity(skills: List<String>, requiredSkill: String): Float {
        val required = requiredSkill.normalized()
        if (required.isBlank()) return 0f
        return skills
            .map { it.normalized() }
            .filter { it.isNotBlank() }
            .maxOfOrNull { skill ->
                when {
                    skill == required -> 1f
                    skill.contains(required) || required.contains(skill) -> 0.86f
                    areRelated(skill, required) -> 0.78f
                    else -> trigram(skill, required) * 0.70f
                }
            }
            ?.coerceIn(0f, 1f) ?: 0f
    }

    private fun explainNeedMatch(user: User, need: NeedPost, score: Int): String {
        val reasons = mutableListOf<String>()
        if (skillSimilarity(user.allSkills(), need.skillRequired) >= 0.75f) reasons += "your ${user.primarySkill} skill fits"
        if (user.village.equals(need.location, ignoreCase = true)) reasons += "same village"
        if (need.urgencyLevel.equals("High", ignoreCase = true)) reasons += "urgent work"
        if (user.verificationStatus == "Verified") reasons += "verified profile"
        if (reasons.isEmpty()) reasons += "available nearby work"
        return "${score}% match because ${reasons.joinToString(", ")}."
    }

    private fun explainWorkerMatch(user: User, need: NeedPost, score: Int): String {
        val reliability = if (user.completedSwaps > 0) "${user.completedSwaps} completed swaps" else "new community worker"
        val verified = if (user.verificationStatus == "Verified") "verified" else "not yet verified"
        return "${score}% fit: ${user.primarySkill}, $reliability, $verified, ${user.village}."
    }

    private fun normalizedTrust(user: User): Float {
        val base = if (user.completedSwaps > 0) user.trustScore / 5f else 0.55f
        return ((base * 0.75f) + (user.reliabilityScore.coerceIn(0, 100) / 100f * 0.25f)).coerceIn(0f, 1f)
    }

    private fun urgencyWeight(urgency: String): Float = when (urgency.lowercase()) {
        "high" -> 1f
        "medium" -> 0.68f
        else -> 0.38f
    }

    private fun User.allSkills(): List<String> = listOf(primarySkill) + secondarySkills.split(",")

    private fun String.normalized(): String = lowercase().trim().replace("-", " ")

    private fun areRelated(a: String, b: String): Boolean {
        val groups = listOf(
            setOf("plumbing", "plumber", "pipe fitting", "water", "bathroom fitting", "tank installation"),
            setOf("carpentry", "carpenter", "woodwork", "furniture", "shelf", "door repair"),
            setOf("electrical", "electrician", "wiring", "motor repair", "pump", "inverter"),
            setOf("masonry", "mason", "brick work", "roof", "plastering", "construction"),
            setOf("painting", "painter", "pop work", "texture", "wall painting"),
            setOf("welding", "welder", "fabrication", "grillwork", "gate"),
            setOf("mechanic", "engine repair", "vehicle repair", "bike", "auto electric"),
            setOf("tiling", "tile work", "floor", "bathroom tiles")
        )
        return groups.any { group -> group.any { a.contains(it) || it.contains(a) } && group.any { b.contains(it) || it.contains(b) } }
    }

    private fun trigram(a: String, b: String): Float {
        if (a.length < 3 || b.length < 3) return if (abs(a.length - b.length) <= 1) 0.2f else 0f
        val one = a.windowed(3).toSet()
        val two = b.windowed(3).toSet()
        val union = one.union(two).size
        return if (union == 0) 0f else one.intersect(two).size.toFloat() / union.toFloat()
    }
}
