package com.solofit.app.sol

data class SolMemoryData(
    val consistentWorkoutTime: String = "",
    val strongestDay: String = "",
    val bestRecoveryDay: String = "",
    val bestProteinCondition: String = ""
)

class SolMemoryEngine {

    fun compute(
        historySessions: List<String>,
        weeklyRecovery: Map<String, Int>,
        weeklyProtein: Map<String, Double>
    ): SolMemoryData {
        val timeCounts = mutableMapOf<String, Int>()
        val dayCounts = mutableMapOf<String, Int>()
        val dayRecovery = mutableMapOf<String, MutableList<Int>>()
        val proteinByCondition = mutableMapOf<String, MutableList<Double>>()

        historySessions.forEach { dateStr ->
            try {
                val date = java.time.LocalDate.parse(dateStr)
                val dayName = date.dayOfWeek.name
                dayCounts[dayName] = (dayCounts[dayName] ?: 0) + 1
            } catch (_: Exception) {}
        }

        weeklyRecovery.forEach { (day, rec) ->
            try {
                val date = java.time.LocalDate.parse(day)
                val dayName = date.dayOfWeek.name
                dayRecovery.getOrPut(dayName) { mutableListOf() }.add(rec)
            } catch (_: Exception) {}
        }

        weeklyProtein.forEach { (day, pct) ->
            try {
                val date = java.time.LocalDate.parse(day)
                val dayName = date.dayOfWeek.name
                proteinByCondition.getOrPut(dayName) { mutableListOf() }.add(pct)
            } catch (_: Exception) {}
        }

        val strongestDay = dayCounts.maxByOrNull { it.value }?.let {
            val name = it.key.lowercase().replaceFirstChar { c -> c.uppercase() }
            if (it.value >= 3) "$name (${it.value} sessions)" else ""
        } ?: ""

        val bestRecoveryDay = dayRecovery.mapValues { (_, vals) ->
            vals.average()
        }.maxByOrNull { it.value }?.let {
            val name = it.key.lowercase().replaceFirstChar { c -> c.uppercase() }
            if (it.value >= 50.0) "$name (avg ${it.value.toInt()}%)" else ""
        } ?: ""

        val bestProteinCondition = proteinByCondition.mapValues { (_, vals) ->
            vals.average()
        }.maxByOrNull { it.value }?.let {
            val name = it.key.lowercase().replaceFirstChar { c -> c.uppercase() }
            if (it.value >= 0.7) "$name (avg ${(it.value * 100).toInt()}%)" else ""
        } ?: ""

        return SolMemoryData(
            strongestDay = strongestDay,
            bestRecoveryDay = bestRecoveryDay,
            bestProteinCondition = bestProteinCondition
        )
    }
}
