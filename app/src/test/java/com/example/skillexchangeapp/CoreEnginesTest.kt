package com.example.skillexchangeapp

import com.example.skillexchangeapp.ai.AiMatchEngine
import com.example.skillexchangeapp.ai.BarterSuggestionEngine
import com.example.skillexchangeapp.ai.ImpactAnalyticsEngine
import com.example.skillexchangeapp.ai.OfferDraftEngine
import com.example.skillexchangeapp.ai.TrustScoreEngine
import com.example.skillexchangeapp.data.local.entity.NeedPost
import com.example.skillexchangeapp.data.local.entity.Swap
import com.example.skillexchangeapp.data.local.entity.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreEnginesTest {
    private val plumber = User(
        id = 1,
        fullName = "Rajesh Sharma",
        email = "r@demo.com",
        phone = "1",
        village = "Hosur",
        primarySkill = "Plumbing",
        secondarySkills = "Pipe Fitting, Water Systems",
        experienceYears = 12,
        trustScore = 4.8f,
        completedSwaps = 15,
        passwordHash = "x",
        verificationStatus = "Verified",
        reliabilityScore = 95
    )

    private val electrician = User(
        id = 2,
        fullName = "Meena Devi",
        email = "m@demo.com",
        phone = "2",
        village = "Hosur",
        primarySkill = "Electrical",
        secondarySkills = "Motor Repair, Wiring",
        experienceYears = 8,
        trustScore = 4.6f,
        completedSwaps = 9,
        passwordHash = "x",
        verificationStatus = "Verified",
        reliabilityScore = 90
    )

    @Test
    fun aiRanksRelevantNearbyNeedsFirst() {
        val plumbingNeed = NeedPost(
            id = 1,
            userId = 2,
            title = "Bathroom pipe leak",
            description = "Pipe is leaking",
            skillRequired = "Plumbing",
            estimatedHours = 3,
            urgencyLevel = "High",
            location = "Hosur"
        )
        val paintingNeed = plumbingNeed.copy(id = 2, skillRequired = "Painting", title = "Paint wall")

        val ranked = AiMatchEngine.rankNeedsForUser(plumber, listOf(paintingNeed, plumbingNeed))

        assertEquals(plumbingNeed.id, ranked.first().need.id)
        assertTrue(ranked.first().score >= 85)
    }

    @Test
    fun barterEngineFindsMutualSkillLoop() {
        val myNeed = NeedPost(10, plumber.id, "Pump repair", "Pump issue", "Electrical", 4, "High", "Hosur")
        val theirNeed = NeedPost(11, electrician.id, "Pipe fitting", "Bathroom pipe", "Plumbing", 4, "Medium", "Hosur")

        val suggestions = BarterSuggestionEngine.findBarterOpportunities(
            currentUser = plumber,
            currentUserNeeds = listOf(myNeed),
            allOpenNeeds = listOf(myNeed, theirNeed),
            allUsers = listOf(plumber, electrician)
        )

        assertEquals(1, suggestions.size)
        assertEquals(electrician.id, suggestions.first().partner.id)
    }

    @Test
    fun fairnessAssistantWarnsUnrealisticHours() {
        val warning = OfferDraftEngine.fairnessNote("Masonry", 1, "High")

        assertTrue(warning.contains("underestimated", ignoreCase = true))
    }

    @Test
    fun impactAnalyticsCalculatesCommunityValue() {
        val swaps = listOf(
            Swap(id = 1, userAId = 1, userBId = 2, needPostId = 1, agreedHours = 4, status = "Completed"),
            Swap(id = 2, userAId = 2, userBId = 1, needPostId = 2, agreedHours = 3, status = "Ongoing")
        )
        val needs = listOf(NeedPost(1, 2, "Pipe", "Fix", "Plumbing", 4, "High", "Hosur"))

        val impact = ImpactAnalyticsEngine.calculate(listOf(plumber, electrician), needs, swaps)

        assertEquals(4, impact.totalHours)
        assertEquals(1400, impact.moneySavedEstimate)
        assertEquals("Plumbing", impact.topSkill)
    }

    @Test
    fun trustEngineAppliesPenaltyAndVerificationBoost() {
        assertEquals(4.5f, TrustScoreEngine.cancellationPenalty(4.8f), 0.001f)
        assertTrue(TrustScoreEngine.verificationBoost(plumber) <= 100)
        assertEquals("Verified Pro", TrustScoreEngine.trustLabel(plumber))
    }
}
