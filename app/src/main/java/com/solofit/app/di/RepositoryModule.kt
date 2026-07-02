package com.solofit.app.di

import com.solofit.app.data.repository.BarcodeRepositoryImpl
import com.solofit.app.data.repository.BodyRepositoryImpl
import com.solofit.app.data.repository.FriendRepositoryImpl
import com.solofit.app.data.repository.GroupRepositoryImpl
import com.solofit.app.data.repository.ProgressPhotoRepositoryImpl
import com.solofit.app.data.repository.JournalRepositoryImpl
import com.solofit.app.data.repository.SoloIdentityRepositoryImpl
import com.solofit.app.data.repository.WeightRepositoryImpl
import com.solofit.app.data.repository.DailyLogRepositoryImpl
import com.solofit.app.data.repository.FoodRepositoryImpl
import com.solofit.app.data.repository.ProfileRepositoryImpl
import com.solofit.app.data.repository.WeeklyPlanRepositoryImpl
import com.solofit.app.data.repository.WorkoutRepositoryImpl
import com.solofit.app.domain.repository.BarcodeRepository
import com.solofit.app.domain.repository.BodyRepository
import com.solofit.app.domain.repository.FriendRepository
import com.solofit.app.domain.repository.GroupRepository
import com.solofit.app.domain.repository.ProgressPhotoRepository
import com.solofit.app.domain.repository.JournalRepository
import com.solofit.app.domain.repository.SoloIdentityRepository
import com.solofit.app.domain.repository.WeightRepository
import com.solofit.app.domain.repository.DailyLogRepository
import com.solofit.app.domain.repository.FoodRepository
import com.solofit.app.domain.repository.ProfileRepository
import com.solofit.app.domain.repository.WeeklyPlanRepository
import com.solofit.app.domain.repository.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds @Singleton
    abstract fun bindFoodRepository(impl: FoodRepositoryImpl): FoodRepository

    @Binds @Singleton
    abstract fun bindDailyLogRepository(impl: DailyLogRepositoryImpl): DailyLogRepository

    @Binds @Singleton
    abstract fun bindWorkoutRepository(impl: WorkoutRepositoryImpl): WorkoutRepository

    @Binds @Singleton
    abstract fun bindBarcodeRepository(impl: BarcodeRepositoryImpl): BarcodeRepository

    @Binds @Singleton
    abstract fun bindWeightRepository(impl: WeightRepositoryImpl): WeightRepository

    @Binds @Singleton
    abstract fun bindJournalRepository(impl: JournalRepositoryImpl): JournalRepository

    @Binds @Singleton
    abstract fun bindBodyRepository(impl: BodyRepositoryImpl): BodyRepository

    @Binds @Singleton
    abstract fun bindProgressPhotoRepository(impl: ProgressPhotoRepositoryImpl): ProgressPhotoRepository

    @Binds @Singleton
    abstract fun bindWeeklyPlanRepository(impl: WeeklyPlanRepositoryImpl): WeeklyPlanRepository

    @Binds @Singleton
    abstract fun bindSoloIdentityRepository(impl: SoloIdentityRepositoryImpl): SoloIdentityRepository

    @Binds @Singleton
    abstract fun bindFriendRepository(impl: FriendRepositoryImpl): FriendRepository

    @Binds @Singleton
    abstract fun bindGroupRepository(impl: GroupRepositoryImpl): GroupRepository
}
