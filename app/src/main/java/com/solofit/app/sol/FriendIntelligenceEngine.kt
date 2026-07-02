package com.solofit.app.sol

import com.solofit.app.data.local.entity.FriendEntity
import com.solofit.app.data.local.entity.FriendEventEntity
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object FriendIntelligenceEngine {

    fun compute(
        friends: List<FriendEntity>,
        events: Map<Long, List<FriendEventEntity>>,
        userActiveDays: Int
    ): CommunityState {
        val now = LocalDate.now()
        val relationships = friends.filter { it.status == "accepted" }.map { friend ->
            val friendEvents = events[friend.id] ?: emptyList()
            val latestEvent = friendEvents.maxByOrNull { it.createdAt }
            val daysSinceLast = latestEvent?.let {
                ChronoUnit.DAYS.between(
                    java.time.Instant.ofEpochMilli(it.createdAt).atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                    now
                ).toInt()
            } ?: 999

            val eventCount30d = friendEvents.count {
                ChronoUnit.DAYS.between(
                    java.time.Instant.ofEpochMilli(it.createdAt).atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                    now
                ) < 30
            }

            val frequency = when {
                daysSinceLast <= 1 -> "daily"
                daysSinceLast <= 7 -> "weekly"
                daysSinceLast <= 30 -> "monthly"
                else -> "inactive"
            }

            val score = computeAccountabilityScore(eventCount30d, daysSinceLast, userActiveDays)

            FriendRelationship(
                friend = FriendState(
                    soloId = friend.soloId,
                    displayName = friend.displayName,
                    relationshipType = RelationshipType.fromValue(friend.relationshipType),
                    status = friend.status
                ),
                accountabilityScore = score,
                interactionFrequency = frequency,
                daysSinceLastInteraction = daysSinceLast,
                needsEncouragement = daysSinceLast > 3 && score < 40,
                lastEventType = latestEvent?.type ?: "",
                lastEventDaysAgo = daysSinceLast
            )
        }

        val active = relationships.count { it.interactionFrequency in setOf("daily", "weekly") }
        val avgScore = if (relationships.isNotEmpty()) relationships.sumOf { it.accountabilityScore } / relationships.size else 50

        val encouragement = relationships.filter { it.needsEncouragement }.map { it.friend.displayName }

        val strongest = relationships.maxByOrNull { it.accountabilityScore }?.friend?.displayName

        val momentumLabel = when {
            active >= 3 -> "Strong"
            active >= 1 -> "Building"
            relationships.isEmpty() -> "Empty"
            else -> "Quiet"
        }

        val momentumDesc = when (momentumLabel) {
            "Strong" -> "Your circle has been consistently active this week"
            "Building" -> "Your circle is quietly building momentum"
            "Quiet" -> "Your circle has been quiet recently"
            else -> "Your circle is just getting started"
        }

        val mutualMomentum = when {
            active >= 3 && avgScore >= 60 ->
                "Your circle has quietly built momentum this week"
            active >= 1 && avgScore >= 40 ->
                "Some of your circle have been active this week"
            else -> ""
        }

        return CommunityState(
            friends = relationships,
            activeCircleCount = active,
            circleMomentumLabel = momentumLabel,
            accountabilityScore = avgScore,
            encouragementNeeded = encouragement,
            strongestPartnership = strongest,
            mutualMomentum = mutualMomentum,
            circleHealth = momentumDesc
        )
    }

    private fun computeAccountabilityScore(
        eventCount30d: Int,
        daysSinceLast: Int,
        userActiveDays: Int
    ): Int {
        if (eventCount30d == 0 && daysSinceLast > 60) return 0
        val recencyScore = when {
            daysSinceLast <= 1 -> 40
            daysSinceLast <= 3 -> 30
            daysSinceLast <= 7 -> 20
            daysSinceLast <= 14 -> 10
            else -> 0
        }
        val frequencyScore = (eventCount30d.coerceAtMost(30) * 2).coerceAtMost(40)
        val sharedActiveScore = if (userActiveDays > 0 && eventCount30d > 0) 20 else 0
        return (recencyScore + frequencyScore + sharedActiveScore).coerceIn(0, 100)
    }
}
