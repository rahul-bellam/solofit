package com.solofit.app.sol

data class SetbackRecoveryMessage(
    val message: String,
    val category: String,
    val action: String
)

object SetbackRecoveryEngine {

    fun detect(
        workoutToday: Boolean,
        ateWellToday: Boolean,
        movedEnough: Boolean,
        meditatedToday: Boolean,
        journaledToday: Boolean,
        workoutYesterday: Boolean,
        ateWellYesterday: Boolean,
        movedYesterday: Boolean,
        meditatedYesterday: Boolean,
        journaledYesterday: Boolean,
        daysActiveThisWeek: Int,
        momentum: MomentumLevel
    ): List<SetbackRecoveryMessage> {
        val messages = mutableListOf<SetbackRecoveryMessage>()

        if (!workoutToday && workoutYesterday) {
            messages.add(SetbackRecoveryMessage(
                "Training paused yesterday. Today's actions still count.",
                "workout",
                "Even a short session keeps the habit alive."
            ))
        } else if (!workoutToday && !workoutYesterday && daysActiveThisWeek == 0) {
            messages.add(SetbackRecoveryMessage(
                "Movement has been quieter recently. A short session today is enough.",
                "workout",
                "Start with a short workout or walk today."
            ))
        } else if (!workoutToday && !workoutYesterday && daysActiveThisWeek > 0) {
            messages.add(SetbackRecoveryMessage(
                "Two days off can happen. This week's earlier movement still matters.",
                "workout",
                "Get back to it today."
            ))
        } else if (workoutToday && !workoutYesterday) {
            messages.add(SetbackRecoveryMessage(
                "Back at it today. That is what consistency looks like.",
                "workout",
                "You resumed after a break. That matters."
            ))
        }

        if (!ateWellToday && ateWellYesterday) {
            messages.add(SetbackRecoveryMessage(
                "One meal doesn't define your nutrition. The next choice does.",
                "nutrition",
                "Focus on protein at your next meal."
            ))
        } else if (!ateWellToday && !ateWellYesterday) {
            messages.add(SetbackRecoveryMessage(
                "Nutrition habits rebuild one meal at a time.",
                "nutrition",
                "Start fresh with your next meal."
            ))
        } else if (ateWellToday && !ateWellYesterday) {
            messages.add(SetbackRecoveryMessage(
                "Strong nutrition today after a tough one yesterday.",
                "nutrition",
                "That bounce-back matters."
            ))
        }

        if (!movedEnough && movedYesterday) {
            messages.add(SetbackRecoveryMessage(
                "Movement paused yesterday. A short walk today reconnects the habit.",
                "movement",
                "Walk for 10 minutes."
            ))
        } else if (!movedEnough && !movedYesterday) {
            messages.add(SetbackRecoveryMessage(
                "Low movement days happen. They don't define your week.",
                "movement",
                "Stand up, stretch, take a short walk."
            ))
        } else if (movedEnough && !movedYesterday) {
            messages.add(SetbackRecoveryMessage(
                "Getting moving today after a quiet yesterday.",
                "movement",
                "That is forward motion."
            ))
        }

        if (!meditatedToday && meditatedYesterday) {
            messages.add(SetbackRecoveryMessage(
                "Returning to stillness is the practice. Not perfection.",
                "meditation",
                "Even 2 minutes counts."
            ))
        } else if (!meditatedToday && !meditatedYesterday) {
            messages.add(SetbackRecoveryMessage(
                "A quiet moment is always available when you return.",
                "meditation",
                "Take 60 seconds to breathe."
            ))
        }

        if (!journaledToday && journaledYesterday) {
            messages.add(SetbackRecoveryMessage(
                "Reflection is always there when you return to it.",
                "journal",
                "Write one sentence today."
            ))
        } else if (!journaledToday && !journaledYesterday) {
            messages.add(SetbackRecoveryMessage(
                "Journaling is a habit you can restart at any point.",
                "journal",
                "Just one word to begin."
            ))
        }

        if (momentum == MomentumLevel.BUILDING && messages.size <= 1) {
            messages.add(SetbackRecoveryMessage(
                "Consistency resumes with one decision. Make it now.",
                "momentum",
                "Choose one small action and take it."
            ))
        }

        return messages.take(2)
    }
}
