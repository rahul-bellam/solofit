package com.solofit.app.data.repository

import com.solofit.app.data.local.UserPreferences
import com.solofit.app.data.local.dao.UserProfileDao
import com.solofit.app.data.local.entity.UserProfileEntity
import com.solofit.app.domain.model.ThemeMode
import com.solofit.app.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val dao: UserProfileDao,
    private val prefs: UserPreferences
) : ProfileRepository {

    override fun observeProfile(): Flow<UserProfileEntity?> = dao.observeProfile()
    override suspend fun getProfile(): UserProfileEntity? = dao.getProfile()
    override suspend fun saveProfile(profile: UserProfileEntity): Long = dao.insert(profile)

    override val onboardingComplete: Flow<Boolean> = prefs.onboardingComplete
    override suspend fun setOnboardingComplete(value: Boolean) = prefs.setOnboardingComplete(value)

    override val themeMode: Flow<ThemeMode> = prefs.themeMode
    override suspend fun setThemeMode(mode: ThemeMode) = prefs.setThemeMode(mode)

    override val animationsEnabled: Flow<Boolean> = prefs.animationsEnabled
    override suspend fun setAnimationsEnabled(enabled: Boolean) = prefs.setAnimationsEnabled(enabled)

    override val reducedMotionApplied: Flow<Boolean> = prefs.reducedMotionApplied
    override suspend fun setReducedMotionApplied(value: Boolean) = prefs.setReducedMotionApplied(value)

    override fun waterMl(date: String): Flow<Int> = prefs.waterMl(date)
    override suspend fun addWaterMl(date: String, deltaMl: Int) = prefs.addWaterMl(date, deltaMl)
    override suspend fun setWaterMl(date: String, valueMl: Int) = prefs.setWaterMl(date, valueMl)

    override val phaseName: Flow<String> = prefs.phaseName
    override val phaseStartDate: Flow<String?> = prefs.phaseStartDate
    override val phaseTargetDays: Flow<Int> = prefs.phaseTargetDays
    override suspend fun setPhase(name: String, startDateIso: String, targetDays: Int) =
        prefs.setPhase(name, startDateIso, targetDays)

    override val waterGoalMl: Flow<Int> = prefs.waterGoalMl
    override suspend fun setWaterGoalMl(ml: Int) = prefs.setWaterGoalMl(ml)

    override val trainingGoal: Flow<com.solofit.app.domain.model.TrainingGoal> = prefs.trainingGoal
    override suspend fun setTrainingGoal(goal: com.solofit.app.domain.model.TrainingGoal) =
        prefs.setTrainingGoal(goal)
}
