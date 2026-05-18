package com.example.skillexchangeapp.ai

import com.example.skillexchangeapp.data.local.entity.User

data class Badge(val emoji: String, val title: String, val earned: Boolean)

object BadgeEngine {
    fun getBadges(user: User): List<Badge> = listOf(
        Badge("🤝", "First Swap",       user.completedSwaps >= 1),
        Badge("⭐", "Trusted Worker",   user.trustScore >= 3.5f),
        Badge("⚡", "Power Trader",     user.completedSwaps >= 5),
        Badge("🏛️", "Community Pillar", user.completedSwaps >= 10),
        Badge("✅", "Verified Pro",     user.verificationStatus == "Verified"),
        Badge("🎯", "Reliable",         user.reliabilityScore >= 85),
        Badge("💛", "Generous",         user.skillPoints >= 50)
    )
}
