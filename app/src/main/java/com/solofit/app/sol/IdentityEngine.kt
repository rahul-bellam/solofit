package com.solofit.app.sol

import java.time.LocalDate
import kotlin.random.Random

data class IdentityMessage(
    val statement: String,
    val trait: String,
    val priority: Int
)

object IdentityEngine {

    private val identityMessages = listOf(
        IdentityMessage("You are becoming someone who trains consistently.", "consistency", 80),
        IdentityMessage("You are developing strong nutrition habits.", "nutrition", 75),
        IdentityMessage("You are making recovery a priority.", "recovery", 70),
        IdentityMessage("You are building a movement practice that fits your life.", "movement", 65),
        IdentityMessage("You are learning to listen to your body.", "awareness", 60),
        IdentityMessage("You are becoming someone who naturally moves.", "movement", 75),
        IdentityMessage("You are becoming someone who naturally recovers.", "recovery", 70),
        IdentityMessage("You are becoming someone who naturally nourishes themselves.", "nutrition", 75),
        IdentityMessage("Each logged meal is you learning what your body needs.", "nutrition", 50),
        IdentityMessage("Each workout is a vote for the person you want to become.", "consistency", 85),
        IdentityMessage("Each night of good sleep is an investment in tomorrow.", "recovery", 55),
        IdentityMessage("You are proving to yourself that you show up.", "consistency", 80),
        IdentityMessage("You are rewiring what 'normal' looks like for your body.", "identity", 70),
        IdentityMessage("Small choices repeated become your new default.", "identity", 65),
        IdentityMessage("You are no longer someone who skips — you are someone who adapts.", "resilience", 75),
        IdentityMessage("You are becoming the kind of person who takes care of themselves.", "identity", 90),
        IdentityMessage("Your habits are quietly reshaping who you are.", "identity", 70),
        IdentityMessage("You are not chasing perfection — you are building direction.", "mindset", 80),
        IdentityMessage("The person you are becoming shows up on the hard days.", "resilience", 75),
        IdentityMessage("You are learning that rest is productive.", "recovery", 60)
    )

    private val shownHistory = mutableMapOf<String, LocalDate>()
    private const val MIN_HOURS_BETWEEN = 48

    fun select(
        activeMetrics: List<String>,
        recentStatements: List<String>,
        forceCategory: String? = null
    ): IdentityMessage? {
        val now = LocalDate.now()

        val candidates = if (forceCategory != null) {
            identityMessages.filter { it.trait == forceCategory }
        } else {
            val metricMatch = identityMessages.filter { msg ->
                activeMetrics.any { metric ->
                    when (metric) {
                        "workout" -> msg.trait == "consistency" || msg.trait == "resilience"
                        "nutrition" -> msg.trait == "nutrition"
                        "recovery" -> msg.trait == "recovery"
                        "movement" -> msg.trait == "movement"
                        "meditation" -> msg.trait == "recovery" || msg.trait == "awareness"
                        "journal" -> msg.trait == "awareness" || msg.trait == "mindset"
                        else -> true
                    }
                }
            }
            if (metricMatch.isEmpty()) identityMessages else metricMatch
        }

        val filtered = candidates.filter { msg ->
            msg.statement !in recentStatements &&
            shownHistory[msg.statement]?.plusDays(MIN_HOURS_BETWEEN / 24L)?.isBefore(now) != false
        }

        val chosen = if (filtered.isNotEmpty()) {
            filtered.maxByOrNull { it.priority + Random.nextInt(-10, 10) }
        } else {
            candidates.maxByOrNull { it.priority }
        }

        if (chosen != null) {
            shownHistory[chosen.statement] = now
        }
        return chosen
    }
}
