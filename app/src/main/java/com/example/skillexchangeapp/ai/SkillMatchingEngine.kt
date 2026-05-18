package com.example.skillexchangeapp.ai

import com.example.skillexchangeapp.data.local.entity.NeedPost
import com.example.skillexchangeapp.data.local.entity.User

/**
 * On-device AI Skill Matching Engine.
 * Uses weighted multi-factor scoring with fuzzy text matching
 * to recommend the best-fit users for each need post.
 */
object SkillMatchingEngine {

    data class MatchResult(
        val user: User,
        val matchScore: Int, // 0-100
        val reason: String
    )

    fun findMatchesForNeed(
        needPost: NeedPost,
        allUsers: List<User>,
        currentUserId: Long
    ): List<MatchResult> {
        return allUsers
            .filter { it.id != currentUserId && it.id != needPost.userId && it.isAvailable }
            .map { user ->
                val skillScore = calculateSkillMatch(needPost.skillRequired, user.primarySkill, user.secondarySkills)
                val trustWeight = if (user.completedSwaps > 0) user.trustScore / 5.0f else 0.3f
                val expWeight = minOf(user.experienceYears / 10.0f, 1.0f)

                val totalScore = ((skillScore * 0.50f + trustWeight * 0.30f + expWeight * 0.20f) * 100).toInt()
                val reason = buildMatchReason(user, needPost, skillScore)

                MatchResult(user, totalScore, reason)
            }
            .filter { it.matchScore > 15 }
            .sortedByDescending { it.matchScore }
            .take(5)
    }

    private fun calculateSkillMatch(required: String, primary: String, secondary: String): Float {
        val req = required.lowercase().trim()
        val pri = primary.lowercase().trim()

        // Exact match
        if (req == pri) return 1.0f

        // Contains match
        if (pri.contains(req) || req.contains(pri)) return 0.85f

        // Check secondary skills
        val secondaryList = secondary.split(",").map { it.trim().lowercase() }.filter { it.isNotBlank() }
        for (s in secondaryList) {
            if (s == req) return 0.7f
            if (s.contains(req) || req.contains(s)) return 0.6f
        }

        // Synonym matching for common trades
        if (areSynonyms(req, pri)) return 0.75f

        // Fuzzy trigram similarity
        val similarity = trigramSimilarity(req, pri)
        if (similarity > 0.3f) return similarity * 0.5f

        return 0f
    }

    private fun areSynonyms(s1: String, s2: String): Boolean {
        val synonymGroups = listOf(
            setOf("plumbing", "plumber", "pipe fitting", "pipe repair", "water repair"),
            setOf("carpentry", "carpenter", "woodwork", "wood work", "furniture"),
            setOf("electrical", "electrician", "wiring", "electric work"),
            setOf("masonry", "mason", "brick work", "bricklaying", "construction"),
            setOf("painting", "painter", "wall painting", "house painting"),
            setOf("welding", "welder", "metal work", "fabrication"),
            setOf("mechanic", "motor repair", "engine repair", "vehicle repair", "auto repair"),
            setOf("roofing", "roof repair", "roof work", "roofer"),
            setOf("tiling", "tile work", "floor tiling", "wall tiling", "tiler")
        )
        return synonymGroups.any { group -> group.any { it.contains(s1) || s1.contains(it) } && group.any { it.contains(s2) || s2.contains(it) } }
    }

    private fun trigramSimilarity(s1: String, s2: String): Float {
        if (s1.length < 3 || s2.length < 3) return 0f
        val t1 = s1.windowed(3).toSet()
        val t2 = s2.windowed(3).toSet()
        val intersection = t1.intersect(t2).size
        val union = t1.union(t2).size
        return if (union == 0) 0f else intersection.toFloat() / union.toFloat()
    }

    private fun buildMatchReason(user: User, need: NeedPost, skillScore: Float): String {
        val name = user.fullName.split(" ").first()
        return when {
            skillScore >= 0.85f -> "$name is an expert ${user.primarySkill} — perfect match for ${need.skillRequired}"
            skillScore >= 0.6f -> "$name knows ${user.primarySkill} and has related experience"
            skillScore >= 0.3f -> "$name has relevant skills that could help"
            else -> "$name is available and trusted (${user.trustScore}★)"
        }
    }
}
