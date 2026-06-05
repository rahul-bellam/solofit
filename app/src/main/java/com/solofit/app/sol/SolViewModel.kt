package com.solofit.app.sol

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.core.DateUtils
import com.solofit.app.data.local.UserPreferences
import com.solofit.app.domain.repository.BodyRepository
import com.solofit.app.domain.repository.DailyLogRepository
import com.solofit.app.domain.repository.JournalRepository
import com.solofit.app.domain.repository.ProfileRepository
import com.solofit.app.domain.repository.WorkoutRepository
import com.solofit.app.core.FitnessMath
import com.solofit.app.core.StreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

data class SolUiState(
    val visible: Boolean = true,
    val userName: String = "",
    val briefingHeader: String = "Today's Focus",
    val greeting: String = "",
    val headline: String = "",
    val detail: String = "",
    val reasoning: List<String> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val voiceLine: String = "",
    val type: InsightType = InsightType.MORNING_GREETING,
    val dayLabel: DayLabel = DayLabel.BALANCED,
    val signals: List<SignalSummary> = emptyList(),
    val supplementaryHeadlines: List<String> = emptyList(),
    val expandedWhy: Boolean = false,
    val expandedWhat: Boolean = false,
    val isSpeaking: Boolean = false,
    val personality: VoicePersonality = VoicePersonality.COMPANION,
    val hasStreakMilestone: Boolean = false,
    val streakMilestone: Int = 0,
    val isSunday: Boolean = false,
    val weeklyWorkoutCount: Int = 0,
    val weeklyProteinDays: Int = 0,
    val weeklyWalkingTrend: String = ""
)

private val BRIEFING_HEADERS = listOf(
    "Today's Focus",
    "Daily Briefing",
    "Morning Check-In",
    "Recovery Update",
    "Wellness Summary",
    "Today's Overview"
)

@HiltViewModel
class SolViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileRepository: ProfileRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val workoutRepository: WorkoutRepository,
    private val bodyRepository: BodyRepository,
    private val journalRepository: JournalRepository,
    private val prefs: UserPreferences,
    private val insightEngine: InsightEngine
) : ViewModel() {

    private val _state = MutableStateFlow(SolUiState())
    val state: StateFlow<SolUiState> = _state.asStateFlow()

    private var tts: TextToSpeech? = null
    private val today = DateUtils.today()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val profile = profileRepository.observeProfile().first()
            val totals = dailyLogRepository.observeTotalsForDate(today).first()
            val history = workoutRepository.observeHistory().first()
            val metric = bodyRepository.observeMetric(today).first()
            val yesterdayStr = LocalDate.now().minusDays(1).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
            val prevMetric = runCatching { bodyRepository.observeMetric(yesterdayStr).first() }.getOrNull()
            val measurements = bodyRepository.observeMeasurements().first()
            val setRows = workoutRepository.observeCompletedSetRows().first()
            val recentEntries = journalRepository.observeRecentGratitude(7).first()
            val wellness = prefs.dailyWellness(today).first()
            val personality = prefs.voicePersonality.first()

            val dates = history.map { it.session.date }
            val now = LocalDate.now()

            val previousRecovery = prevMetric?.let {
                FitnessMath.recoveryScore(
                    sleepHours = it.sleepHours, steps = it.steps,
                    workoutDone = null, waterMl = null,
                    waterGoalMl = 3000, energyScore = it.energyScore
                )
            }
            val recoveryScore = metric?.let {
                FitnessMath.recoveryScore(
                    sleepHours = it.sleepHours, steps = it.steps,
                    workoutDone = dates.contains(today), waterMl = null,
                    waterGoalMl = 3000, energyScore = it.energyScore
                )
            }

            val last3 = history.count { h ->
                runCatching {
                    ChronoUnit.DAYS.between(LocalDate.parse(h.session.date), now).toInt()
                }.getOrNull()?.let { it in 0..3 } == true
            }
            val prev3 = history.count { h ->
                runCatching {
                    ChronoUnit.DAYS.between(LocalDate.parse(h.session.date), now).toInt()
                }.getOrNull()?.let { it in 4..7 } == true
            }
            val volumeIncrease = last3 > prev3 && last3 >= 2

            val waists = measurements.mapNotNull { it.waistCm }
            val measurementImproving = if (waists.size >= 2) waists.last() < waists.first() else null
            val strengthIncreasing = setRows.isNotEmpty()

            val yesterdayMetric = prevMetric
            val prevSleep = yesterdayMetric?.sleepHours
            val prevSteps = yesterdayMetric?.steps

            val journalSentiment: JournalSentiment? = null

            val input = SolInput(
                recoveryScore = recoveryScore,
                previousRecoveryScore = previousRecovery,
                streakDays = StreakCalculator.currentStreak(dates, now),
                daysActiveThisWeek = StreakCalculator.daysActiveInWindow(dates, now, 7),
                workoutToday = dates.contains(today),
                consumedCalories = totals.calories.roundToInt(),
                consumedProtein = totals.proteinG.roundToInt(),
                targetCalories = profile?.targetCalories ?: 2000,
                targetProtein = profile?.targetProtein ?: 150,
                sleepHours = metric?.sleepHours,
                previousSleepHours = prevSleep,
                steps = metric?.steps,
                previousSteps = prevSteps,
                waterMl = 0,
                waterGoalMl = 3000,
                energyScore = metric?.energyScore,
                stressLevel = wellness.stressLevel,
                previousStressLevel = null,
                moodScore = metric?.moodScore,
                meditationMinutes = wellness.meditationMinutes,
                journalDays = recentEntries.size,
                journalSentiment = journalSentiment,
                measurementImproving = measurementImproving,
                strengthIncreasing = strengthIncreasing,
                phaseDay = 1,
                phaseTargetDays = 365,
                historySessionCount = history.size,
                recentTrainingVolumeIncrease = volumeIncrease
            )

            val briefing = insightEngine.computeBriefing(input)
            val transformedVoice = VoicePersonalityTransformer.transform(briefing.primary.voiceLine, personality)

            val streakMilestone = when (input.streakDays) {
                7 -> 7; 30 -> 30; 100 -> 100; else -> 0
            }

            val headerIndex = (now.dayOfYear % BRIEFING_HEADERS.size).coerceIn(0, BRIEFING_HEADERS.size - 1)

            val isSunday = now.dayOfWeek == DayOfWeek.SUNDAY

            val weeklyWorkoutCount = StreakCalculator.daysActiveInWindow(dates, now, 7)
            val weeklyProteinDays = calculateProteinDays(history, totals, profile?.targetProtein ?: 150)
            val weeklyWalkingTrend = calculateWeeklySteps(bodyRepository, now)

            _state.value = SolUiState(
                visible = true,
                userName = profile?.name ?: "",
                briefingHeader = BRIEFING_HEADERS[headerIndex],
                greeting = briefing.greeting,
                headline = briefing.primary.headline,
                detail = briefing.primary.detail,
                reasoning = briefing.primary.reasoning,
                recommendations = briefing.primary.recommendations,
                voiceLine = transformedVoice,
                type = briefing.primary.type,
                dayLabel = briefing.dayLabel,
                signals = briefing.signals,
                supplementaryHeadlines = briefing.supplementary.map { it.headline },
                expandedWhy = true,
                personality = personality,
                hasStreakMilestone = streakMilestone > 0,
                streakMilestone = streakMilestone,
                isSunday = isSunday,
                weeklyWorkoutCount = weeklyWorkoutCount,
                weeklyProteinDays = weeklyProteinDays,
                weeklyWalkingTrend = weeklyWalkingTrend
            )
        }
    }

    private suspend fun calculateProteinDays(history: List<*>, totals: Any, targetProtein: Int): Int {
        return 0
    }

    private suspend fun calculateWeeklySteps(bodyRepository: BodyRepository, now: LocalDate): String {
        return ""
    }

    fun toggleWhy() {
        _state.value = _state.value.copy(expandedWhy = !_state.value.expandedWhy)
    }

    fun toggleWhat() {
        _state.value = _state.value.copy(expandedWhat = !_state.value.expandedWhat)
    }

    fun setPersonality(personality: VoicePersonality) {
        _state.value = _state.value.copy(personality = personality)
        viewModelScope.launch {
            prefs.setVoicePersonality(personality)
            val s = _state.value
            val transformed = VoicePersonalityTransformer.transform(s.voiceLine, personality)
            _state.value = _state.value.copy(voiceLine = transformed)
        }
    }

    fun speak() {
        val text = _state.value.voiceLine.ifEmpty { return }
        speakText(text)
    }

    fun speakLine(line: String) {
        val transformed = VoicePersonalityTransformer.transform(line, _state.value.personality)
        speakText(transformed)
    }

    private fun speakText(text: String) {
        if (tts == null) {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.US
                    doSpeak(text)
                }
            }
        } else {
            doSpeak(text)
        }
    }

    private fun doSpeak(text: String) {
        _state.value = _state.value.copy(isSpeaking = true)
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                _state.value = _state.value.copy(isSpeaking = false)
            }
            override fun onError(utteranceId: String?) {
                _state.value = _state.value.copy(isSpeaking = false)
            }
        })
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "sol")
    }

    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
        super.onCleared()
    }
}
