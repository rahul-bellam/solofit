package com.solofit.app.data.local

import androidx.room.TypeConverter
import com.solofit.app.domain.model.ActivityLevel
import com.solofit.app.domain.model.FitnessGoal
import com.solofit.app.domain.model.Gender

/** Room type converters for domain enums stored as their name strings. */
class Converters {
    @TypeConverter fun fromGender(value: Gender): String = value.name
    @TypeConverter fun toGender(value: String): Gender =
        runCatching { Gender.valueOf(value) }.getOrDefault(Gender.MALE)

    @TypeConverter fun fromActivity(value: ActivityLevel): String = value.name
    @TypeConverter fun toActivity(value: String): ActivityLevel =
        runCatching { ActivityLevel.valueOf(value) }.getOrDefault(ActivityLevel.MODERATE)

    @TypeConverter fun fromGoal(value: FitnessGoal): String = value.name
    @TypeConverter fun toGoal(value: String): FitnessGoal =
        runCatching { FitnessGoal.valueOf(value) }.getOrDefault(FitnessGoal.MAINTAIN)
}
