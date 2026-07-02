package com.solofit.app.sol

enum class RelationshipType(val displayName: String) {
    SUPPORT("Support"),
    PARTNER("Partner"),
    CLOSE_FRIEND("Close Friend"),
    COWORKER("Coworker"),
    FAMILY("Family"),
    COACH("Coach"),
    WORKOUT_BUDDY("Workout Buddy"),
    ACCOUNTABILITY_PARTNER("Accountability Partner");

    companion object {
        fun fromValue(v: String): RelationshipType =
            entries.firstOrNull { it.name == v || it.displayName == v } ?: ACCOUNTABILITY_PARTNER
    }
}

data class FriendState(
    val soloId: String,
    val displayName: String,
    val relationshipType: RelationshipType,
    val status: String
)

data class FriendRelationship(
    val friend: FriendState,
    val accountabilityScore: Int = 50,
    val interactionFrequency: String = "unknown",
    val daysSinceLastInteraction: Int = 0,
    val sharedHabits: List<String> = emptyList(),
    val needsEncouragement: Boolean = false,
    val lastEventType: String = "",
    val lastEventDaysAgo: Int = 0
)

data class CommunityState(
    val friends: List<FriendRelationship> = emptyList(),
    val activeCircleCount: Int = 0,
    val circleMomentumLabel: String = "Building",
    val accountabilityScore: Int = 50,
    val encouragementNeeded: List<String> = emptyList(),
    val strongestPartnership: String? = null,
    val mutualMomentum: String = "",
    val circleHealth: String = "Your circle is just getting started"
)
