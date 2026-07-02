package com.solofit.app.di

import android.content.Context
import com.solofit.app.data.local.SoloFitDatabase
import com.solofit.app.data.local.UserPreferences
import com.solofit.app.data.local.dao.DailyLogDao
import com.solofit.app.data.local.dao.FoodDao
import com.solofit.app.data.local.dao.FriendDao
import com.solofit.app.data.local.dao.FriendEventDao
import com.solofit.app.data.local.dao.SoloIdentityDao
import com.solofit.app.data.local.dao.UserProfileDao
import com.solofit.app.data.local.dao.JournalDao
import com.solofit.app.data.local.dao.BodyMeasurementDao
import com.solofit.app.data.local.dao.DailyMetricDao
import com.solofit.app.data.local.dao.ProgressPhotoDao
import com.solofit.app.data.local.dao.FrequentMealDao
import com.solofit.app.data.local.dao.WeeklyPlanDao
import com.solofit.app.data.local.dao.WeightDao
import com.solofit.app.data.local.dao.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Named("io")
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Named("default")
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        scope: CoroutineScope
    ): SoloFitDatabase = SoloFitDatabase.build(context, scope)

    @Provides fun provideUserProfileDao(db: SoloFitDatabase): UserProfileDao = db.userProfileDao()
    @Provides fun provideFoodDao(db: SoloFitDatabase): FoodDao = db.foodDao()
    @Provides fun provideDailyLogDao(db: SoloFitDatabase): DailyLogDao = db.dailyLogDao()
    @Provides fun provideWorkoutDao(db: SoloFitDatabase): WorkoutDao = db.workoutDao()
    @Provides fun provideWeightDao(db: SoloFitDatabase): WeightDao = db.weightDao()
    @Provides fun provideJournalDao(db: SoloFitDatabase): JournalDao = db.journalDao()
    @Provides fun provideBodyMeasurementDao(db: SoloFitDatabase): BodyMeasurementDao = db.bodyMeasurementDao()
    @Provides fun provideDailyMetricDao(db: SoloFitDatabase): DailyMetricDao = db.dailyMetricDao()
    @Provides fun provideProgressPhotoDao(db: SoloFitDatabase): ProgressPhotoDao = db.progressPhotoDao()
    @Provides fun provideWeeklyPlanDao(db: SoloFitDatabase): WeeklyPlanDao = db.weeklyPlanDao()
    @Provides fun provideFrequentMealDao(db: SoloFitDatabase): FrequentMealDao = db.frequentMealDao()

    @Provides fun provideSoloIdentityDao(db: SoloFitDatabase): SoloIdentityDao = db.soloIdentityDao()
    @Provides fun provideFriendDao(db: SoloFitDatabase): FriendDao = db.friendDao()
    @Provides fun provideFriendEventDao(db: SoloFitDatabase): FriendEventDao = db.friendEventDao()

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences =
        UserPreferences(context)
}
