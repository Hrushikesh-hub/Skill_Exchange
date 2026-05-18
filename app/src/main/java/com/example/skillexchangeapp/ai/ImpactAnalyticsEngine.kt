package com.example.skillexchangeapp.ai

import com.example.skillexchangeapp.data.local.entity.ImpactMetric
import com.example.skillexchangeapp.data.local.entity.NeedPost
import com.example.skillexchangeapp.data.local.entity.Swap
import com.example.skillexchangeapp.data.local.entity.User

object ImpactAnalyticsEngine {
    data class CommunityImpact(
        val totalHours: Int,
        val moneySavedEstimate: Int,
        val activeWorkers: Int,
        val completedSwaps: Int,
        val topSkill: String,
        val averageTrust: Float,
        val openNeeds: Int
    )

    fun calculate(users: List<User>, needs: List<NeedPost>, swaps: List<Swap>): CommunityImpact {
        val completed = swaps.filter { it.status == "Completed" }
        val totalHours = completed.sumOf { it.agreedHours }
        val topSkill = needs.groupingBy { it.skillRequired }.eachCount().maxByOrNull { it.value }?.key ?: "Plumbing"
        val trustedUsers = users.filter { it.completedSwaps > 0 }
        val avgTrust = if (trustedUsers.isEmpty()) 0f else trustedUsers.map { it.trustScore }.average().toFloat()
        return CommunityImpact(
            totalHours = totalHours,
            moneySavedEstimate = totalHours * 350,
            activeWorkers = users.count { it.isAvailable },
            completedSwaps = completed.size,
            topSkill = topSkill,
            averageTrust = avgTrust,
            openNeeds = needs.count { it.status == "Open" }
        )
    }

    fun toMetric(impact: CommunityImpact): ImpactMetric {
        return ImpactMetric(
            totalHoursExchanged = impact.totalHours,
            moneySavedEstimate = impact.moneySavedEstimate,
            activeWorkers = impact.activeWorkers,
            completedSwaps = impact.completedSwaps,
            topSkillInDemand = impact.topSkill,
            averageTrustScore = impact.averageTrust
        )
    }
}
