package com.example.skillexchangeapp.ai

import com.example.skillexchangeapp.data.local.entity.NeedPost
import com.example.skillexchangeapp.data.local.entity.User

object OfferDraftEngine {
    fun suggestNeedTitle(skill: String, location: String): String {
        val cleanSkill = skill.ifBlank { "Skilled Work" }.trim()
        val cleanLocation = location.ifBlank { "Nearby" }.trim()
        return "$cleanSkill help needed in $cleanLocation"
    }

    fun improveNeedDescription(raw: String, skill: String, hours: Int): String {
        val base = raw.ifBlank { "Need help from an experienced $skill worker." }.trim()
        return "$base Expected work: $hours hour${if (hours == 1) "" else "s"}. Please mention tools, availability, and any material requirement."
    }

    fun fairnessNote(skill: String, hours: Int, urgency: String): String {
        val expected = when (skill.lowercase()) {
            "plumbing" -> 4..8
            "electrical" -> 3..10
            "carpentry" -> 4..12
            "masonry" -> 6..20
            "painting" -> 8..18
            "welding" -> 2..8
            "mechanic" -> 3..8
            else -> 2..12
        }
        return when {
            hours <= 0 -> "Enter a realistic hour estimate so the Skill Point exchange is fair."
            hours < expected.first -> "This may be underestimated for $skill. Consider ${expected.first}+ hours for a fair swap."
            hours > expected.last && urgency.equals("High", ignoreCase = true) -> "High urgency and high hours may need two workers or a clear schedule."
            else -> "Fair estimate. 1 hour equals 1 Skill Point."
        }
    }

    fun draftOffer(user: User, need: NeedPost): String {
        val firstName = user.fullName.split(" ").firstOrNull() ?: user.fullName
        val verified = if (user.verificationStatus == "Verified") "I am verified on SkillExchange. " else ""
        return "Hi, I am $firstName. $verified I can help with ${need.skillRequired.lowercase()} for ${need.estimatedHours} Skill Points. I have ${user.experienceYears} years of ${user.primarySkill.lowercase()} experience and can coordinate the schedule in chat."
    }
}
