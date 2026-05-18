package com.example.skillexchangeapp.utils

import android.content.Context
import com.example.skillexchangeapp.data.firebase.FirebaseService
import com.example.skillexchangeapp.data.local.entity.*
import com.example.skillexchangeapp.data.repository.SkillExchangeRepository

class DemoDataSeeder(private val repository: SkillExchangeRepository, private val context: Context) {

    // Demo password shared by all seeded users
    private val demoPassword = "demo123"

    suspend fun seedIfNeeded() {
        if (repository.getUserCount() > 0) return

        val now = System.currentTimeMillis()
        val day = 86400000L

        // ── 8 Demo Users ──
        val users = listOf(
            User(fullName = "Rajesh Sharma", email = "rajesh@demo.com", passwordHash = SecurityUtils.hashPassword("demo123"), phone = "9876543210", village = "Hosur", primarySkill = "Plumbing", secondarySkills = "Pipe Fitting, Water Systems", experienceYears = 12, trustScore = 4.8f, completedSwaps = 15, skillPoints = 45, bio = "Expert plumber with 12 years of experience. Specialize in pipe fitting and water systems.", isAvailable = true, verificationStatus = "Verified", reliabilityScore = 95, portfolioSummary = "Water tank installs, pipe leak repair, bathroom fitting", isAdmin = true),
            User(fullName = "Priya Lakshmi", email = "priya@demo.com", passwordHash = SecurityUtils.hashPassword("demo123"), phone = "9876543211", village = "Hosur", primarySkill = "Carpentry", secondarySkills = "Furniture Making, Wood Finishing", experienceYears = 8, trustScore = 4.5f, completedSwaps = 10, skillPoints = 30, bio = "Skilled carpenter. Furniture making, door repair, and wood finishing.", isAvailable = true, verificationStatus = "Verified", reliabilityScore = 88),
            User(fullName = "Venkat Rao", email = "venkat@demo.com", passwordHash = SecurityUtils.hashPassword("demo123"), phone = "9876543212", village = "Kolar", primarySkill = "Masonry", secondarySkills = "Brick Work, Plastering", experienceYears = 15, trustScore = 4.9f, completedSwaps = 25, skillPoints = 80, bio = "Master mason. Brick work, plastering, and concrete finishing.", isAvailable = true, verificationStatus = "Verified", reliabilityScore = 96),
            User(fullName = "Meena Devi", email = "meena@demo.com", passwordHash = SecurityUtils.hashPassword("demo123"), phone = "9876543213", village = "Denkanikottai", primarySkill = "Electrical", secondarySkills = "Motor Repair, Wiring", experienceYears = 6, trustScore = 4.2f, completedSwaps = 8, skillPoints = 20, bio = "Licensed electrician. Wiring, motor repair, and inverter installation.", isAvailable = true, verificationStatus = "Pending", reliabilityScore = 82),
            User(fullName = "Suresh Kumar", email = "suresh@demo.com", passwordHash = SecurityUtils.hashPassword("demo123"), phone = "9876543214", village = "Thalli", primarySkill = "Welding", secondarySkills = "Fabrication, Grillwork", experienceYears = 10, trustScore = 4.6f, completedSwaps = 12, skillPoints = 35, bio = "Professional welder. Gate fabrication, grill work, and structural welding.", isAvailable = true, verificationStatus = "Verified", reliabilityScore = 86),
            User(fullName = "Kavitha M", email = "kavitha@demo.com", passwordHash = SecurityUtils.hashPassword("demo123"), phone = "9876543215", village = "Hosur", primarySkill = "Painting", secondarySkills = "POP Work, Texture Finish", experienceYears = 7, trustScore = 4.4f, completedSwaps = 9, skillPoints = 25, bio = "Interior and exterior painting. POP work and texture finishing.", isAvailable = true, reliabilityScore = 81),
            User(fullName = "Ravi Naidu", email = "ravi@demo.com", passwordHash = SecurityUtils.hashPassword("demo123"), phone = "9876543216", village = "Shoolagiri", primarySkill = "Mechanic", secondarySkills = "Engine Repair, Auto Electric", experienceYears = 11, trustScore = 4.7f, completedSwaps = 18, skillPoints = 55, bio = "Auto mechanic. Two-wheeler and four-wheeler engine repair.", isAvailable = true, verificationStatus = "Verified", reliabilityScore = 92),
            User(fullName = "Anitha Rajan", email = "anitha@demo.com", passwordHash = SecurityUtils.hashPassword("demo123"), phone = "9876543217", village = "Bagalur", primarySkill = "Plumbing", secondarySkills = "Tank Installation, Bathroom Fitting", experienceYears = 5, trustScore = 4.1f, completedSwaps = 5, skillPoints = 15, bio = "Plumbing specialist. Bathroom fitting and water tank installation.", isAvailable = true, reliabilityScore = 78)
        )

        val userIds = mutableListOf<Long>()
        for (user in users) {
            userIds.add(repository.registerUser(user))
        }

        // Create Firebase Auth accounts for all demo users (so they can sign in via Firebase)
        for (user in users) {
            try {
                FirebaseService.createAuthAccount(user.email, demoPassword)
            } catch (_: Exception) {
                // Ignore — app works offline without Firebase
            }
        }

        // ── 10 Need Posts ──
        val needPosts = listOf(
            NeedPost(userId = userIds[0], title = "Fix Leaking Roof", description = "Roof tiles are cracked and leaking during monsoon. Need urgent repair before next rain.", skillRequired = "Masonry", estimatedHours = 6, urgencyLevel = "High", location = "Hosur", deadline = now + 3 * day, offerCount = 3),
            NeedPost(userId = userIds[1], title = "Build Kitchen Shelf", description = "Need a custom wooden shelf for kitchen. Wall-mounted, 3 tiers, with finish.", skillRequired = "Carpentry", estimatedHours = 4, urgencyLevel = "Medium", location = "Hosur", deadline = now + 7 * day, offerCount = 1),
            NeedPost(userId = userIds[2], title = "Fix Water Pump Motor", description = "Agricultural water pump motor is not starting. Needs winding or replacement.", skillRequired = "Electrical", estimatedHours = 3, urgencyLevel = "High", location = "Kolar", deadline = now + 2 * day, offerCount = 2),
            NeedPost(userId = userIds[3], title = "Gate Welding Repair", description = "Iron gate hinge is broken and gate is sagging. Needs re-welding and alignment.", skillRequired = "Welding", estimatedHours = 2, urgencyLevel = "Medium", location = "Denkanikottai"),
            NeedPost(userId = userIds[4], title = "Bathroom Pipe Fitting", description = "New bathroom construction needs complete pipe fitting for hot and cold water.", skillRequired = "Plumbing", estimatedHours = 8, urgencyLevel = "Low", location = "Thalli", deadline = now + 14 * day, offerCount = 4),
            NeedPost(userId = userIds[5], title = "House Interior Painting", description = "3 rooms need interior painting with POP finish. Walls already prepared.", skillRequired = "Painting", estimatedHours = 12, urgencyLevel = "Low", location = "Hosur", deadline = now + 21 * day, offerCount = 2),
            NeedPost(userId = userIds[6], title = "Bike Engine Overhauling", description = "Royal Enfield Classic 350 engine knocking. Needs full overhaul.", skillRequired = "Mechanic", estimatedHours = 5, urgencyLevel = "Medium", location = "Shoolagiri", offerCount = 1),
            NeedPost(userId = userIds[7], title = "Overhead Tank Plumbing", description = "Connect overhead tank to all taps. Install new pipeline from tank to kitchen and bathrooms.", skillRequired = "Plumbing", estimatedHours = 6, urgencyLevel = "High", location = "Bagalur", deadline = now + 5 * day, offerCount = 3),
            NeedPost(userId = userIds[0], title = "Electrical Wiring Upgrade", description = "Old aluminum wiring needs upgrade to copper. 3 rooms + kitchen.", skillRequired = "Electrical", estimatedHours = 10, urgencyLevel = "High", location = "Hosur", deadline = now + 4 * day, offerCount = 2),
            NeedPost(userId = userIds[2], title = "Compound Wall Construction", description = "Build 50 feet compound wall with brick and cement. Foundation already done.", skillRequired = "Masonry", estimatedHours = 20, urgencyLevel = "Medium", location = "Kolar", deadline = now + 30 * day, offerCount = 5)
        )
        val postIds = mutableListOf<Long>()
        for (post in needPosts) {
            postIds.add(repository.postNeed(post))
        }

        // ── Offers ──
        val offers = listOf(
            Offer(needPostId = postIds[0], offeredByUserId = userIds[2], offeredSkill = "Masonry", offeredHours = 6, message = "I have 15 years of masonry experience. I can fix the roof tiles and apply waterproof coating."),
            Offer(needPostId = postIds[0], offeredByUserId = userIds[4], offeredSkill = "Masonry", offeredHours = 5, message = "I can handle roof repairs. Have done similar work before."),
            Offer(needPostId = postIds[0], offeredByUserId = userIds[5], offeredSkill = "Painting", offeredHours = 6, message = "I can fix roof and also paint the ceiling after repair."),
            Offer(needPostId = postIds[8], offeredByUserId = userIds[3], offeredSkill = "Electrical", offeredHours = 10, message = "Licensed electrician here. I'll rewire with proper MCB protection."),
            Offer(needPostId = postIds[8], offeredByUserId = userIds[7], offeredSkill = "Electrical", offeredHours = 8, message = "Can handle copper wiring upgrade. Have proper tools."),
            Offer(needPostId = postIds[4], offeredByUserId = userIds[0], offeredSkill = "Plumbing", offeredHours = 8, message = "Expert plumber. Complete bathroom fitting is my speciality.")
        )
        for (offer in offers) {
            repository.submitOffer(offer)
        }

        // ── Swaps ──
        val swap1 = Swap(userAId = userIds[1], userBId = userIds[0], needPostId = postIds[1], agreedHours = 4, status = "Completed", completionDate = now - 7 * day, scheduledDate = now - 8 * day, scheduledTime = "10:00 AM")
        repository.createSwap(swap1)
        repository.submitReview(Review(swapId = 1, reviewerId = userIds[1], rating = 5, comment = "Excellent plumbing work! Very professional and clean."))

        val swap2 = Swap(userAId = userIds[2], userBId = userIds[3], needPostId = postIds[2], agreedHours = 3, status = "Ongoing", scheduledDate = now + 1 * day, scheduledTime = "9:00 AM")
        repository.createSwap(swap2)

        val swap3 = Swap(userAId = userIds[0], userBId = userIds[5], needPostId = postIds[5], agreedHours = 12, status = "Scheduled", scheduledDate = now + 5 * day, scheduledTime = "8:00 AM")
        repository.createSwap(swap3)

        val swap4 = Swap(userAId = userIds[4], userBId = userIds[6], needPostId = postIds[6], agreedHours = 5, status = "Completed", completionDate = now - 3 * day)
        repository.createSwap(swap4)
        repository.submitReview(Review(swapId = 4, reviewerId = userIds[4], rating = 4, comment = "Good mechanic. Bike runs smooth now."))

        // ── Chat Messages ──
        repository.sendMessage(Message(senderId = userIds[0], receiverId = userIds[5], content = "Hi Kavitha, when can you start the painting work?", timestamp = now - 2 * day))
        repository.sendMessage(Message(senderId = userIds[5], receiverId = userIds[0], content = "Hello Rajesh! I can start this Saturday morning. Is 8 AM okay?", timestamp = now - 2 * day + 3600000))
        repository.sendMessage(Message(senderId = userIds[0], receiverId = userIds[5], content = "Perfect! Please bring your own brushes. I'll arrange the paint.", timestamp = now - 2 * day + 7200000))
        repository.sendMessage(Message(senderId = userIds[5], receiverId = userIds[0], content = "Sure, I have all the equipment. See you Saturday! \uD83D\uDC4D", timestamp = now - 1 * day))

        // ── Notifications for Rajesh ──
        repository.addNotification(Notification(userId = userIds[0], title = "New Offer Received!", message = "Venkat Rao offered help on \"Fix Leaking Roof\"", type = "OFFER_RECEIVED", relatedId = postIds[0], timestamp = now - 1 * day))
        repository.addNotification(Notification(userId = userIds[0], title = "New Offer Received!", message = "Suresh Kumar offered help on \"Fix Leaking Roof\"", type = "OFFER_RECEIVED", relatedId = postIds[0], timestamp = now - 12 * 3600000))
        repository.addNotification(Notification(userId = userIds[0], title = "Swap Completed! \u2B50", message = "Priya rated you 5 stars for the shelf work!", type = "SWAP_COMPLETED", relatedId = 1, timestamp = now - 7 * day, isRead = true))
        repository.addNotification(Notification(userId = userIds[0], title = "New Offer on Wiring", message = "Meena offered help on \"Electrical Wiring Upgrade\"", type = "OFFER_RECEIVED", relatedId = postIds[8], timestamp = now - 6 * 3600000))

        // ── Skill Point Transactions ──
        repository.addSkillPointTransaction(SkillPointTransaction(userId = userIds[0], amount = 4, type = "EARNED", relatedSwapId = 1, description = "Completed shelf build (+4 SP)"))
        repository.addSkillPointTransaction(SkillPointTransaction(userId = userIds[1], amount = -4, type = "SPENT", relatedSwapId = 1, description = "Paid Rajesh for shelf plumbing support (-4 SP)"))
        repository.addSkillPointTransaction(SkillPointTransaction(userId = userIds[6], amount = 5, type = "EARNED", relatedSwapId = 4, description = "Completed bike repair swap (+5 SP)"))

        // Platform trust, verification, and admin demo data
        repository.requestVerification(VerificationRequest(
            userId = userIds[3],
            skill = "Electrical",
            proofText = "Motor repair certificate from local workshop and 8 completed community jobs",
            status = "Pending"
        ))
        repository.submitReport(Report(
            reporterId = userIds[0],
            reportedUserId = userIds[5],
            needPostId = postIds[5],
            reason = "Schedule risk",
            details = "Worker requested a delay. Coordinator can mark this resolved during demo.",
            status = "Open"
        ))
        repository.addAdminAction(AdminAction(
            adminUserId = userIds[0],
            actionType = "DEMO_SEEDED",
            targetType = "Platform",
            targetId = 1,
            notes = "Seeded realistic SkillExchange demo story"
        ))
        repository.addSwapEvent(SwapEvent(swapId = 1, actorUserId = userIds[1], eventType = "COMPLETED", title = "Swap completed", detail = "Priya reviewed Rajesh and settled 4 SP"))
        repository.addSwapEvent(SwapEvent(swapId = 2, actorUserId = userIds[3], eventType = "STARTED", title = "Work started", detail = "Meena started pump motor diagnosis"))
    }
}
