package com.example.skillexchangeapp.ai

import com.example.skillexchangeapp.data.local.entity.NeedPost
import com.example.skillexchangeapp.data.local.entity.User

/**
 * Finds mutual barter opportunities — cases where two users can help each other.
 * This is the core innovation: not just matching, but finding "barter loops."
 */
object BarterSuggestionEngine {

    data class BarterSuggestion(
        val partner: User,
        val theyNeed: NeedPost,
        val youCanOffer: String,
        val theyCanOfferSkill: String,
        val mutualBenefit: String,
        val strength: Int // 0-100
    )

    fun findBarterOpportunities(
        currentUser: User,
        currentUserNeeds: List<NeedPost>,
        allOpenNeeds: List<NeedPost>,
        allUsers: List<User>
    ): List<BarterSuggestion> {
        val suggestions = mutableListOf<BarterSuggestion>()

        for (otherNeed in allOpenNeeds) {
            if (otherNeed.userId == currentUser.id) continue

            // Can current user help with this need?
            val canHelp = isSkillMatch(currentUser.primarySkill, otherNeed.skillRequired) ||
                    currentUser.secondarySkills.split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .any { isSkillMatch(it, otherNeed.skillRequired) }

            if (!canHelp) continue

            val otherUser = allUsers.find { it.id == otherNeed.userId } ?: continue

            // Can the other user help with any of current user's needs?
            for (myNeed in currentUserNeeds) {
                val theyCanHelp = isSkillMatch(otherUser.primarySkill, myNeed.skillRequired) ||
                        otherUser.secondarySkills.split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                            .any { isSkillMatch(it, myNeed.skillRequired) }

                if (theyCanHelp) {
                    val partnerName = otherUser.fullName.split(" ").first()
                    val strength = calculateStrength(currentUser, otherUser, otherNeed, myNeed)

                    suggestions.add(
                        BarterSuggestion(
                            partner = otherUser,
                            theyNeed = otherNeed,
                            youCanOffer = otherNeed.skillRequired,
                            theyCanOfferSkill = myNeed.skillRequired,
                            mutualBenefit = "$partnerName needs ${otherNeed.skillRequired} (your skill!) and can help with ${myNeed.skillRequired}",
                            strength = strength
                        )
                    )
                }
            }
        }

        return suggestions.sortedByDescending { it.strength }.take(3)
    }

    private fun isSkillMatch(skill1: String, skill2: String): Boolean {
        val s1 = skill1.lowercase().trim()
        val s2 = skill2.lowercase().trim()
        if (s1.isEmpty() || s2.isEmpty()) return false
        return s1 == s2 || s1.contains(s2) || s2.contains(s1)
    }

    private fun calculateStrength(
        currentUser: User, otherUser: User,
        otherNeed: NeedPost, myNeed: NeedPost
    ): Int {
        var score = 50 // base score for mutual match
        score += (otherUser.trustScore * 5).toInt() // up to 25
        score += minOf(otherUser.experienceYears * 2, 15) // up to 15
        if (otherNeed.urgencyLevel.equals("High", ignoreCase = true)) score += 10
        return minOf(score, 100)
    }
}
