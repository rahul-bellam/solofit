package com.solofit.app.data.local

import androidx.room.TypeConverter
import com.solofit.app.domain.model.ActivityLevel
import com.solofit.app.domain.model.FitnessGoal
import com.solofit.app.domain.model.Gender

/** Room type converters for domain enums stored as their name strings. */
class Converters {
    @TypeConverter fun fromGender(value: Gender): String = value.name
    @TypeConverter fun toGender(value: String): Gender = Gender.valueOf(value)

    @TypeConverter fun fromActivity(value: ActivityLevel): String = value.name
    @TypeConverter fun toActivity(value: String): ActivityLevel = ActivityLevel.valueOf(value)

    @TypeConverter fun fromGoal(value: FitnessGoal): String = value.name
    @TypeConverter fun toGoal(value: String): FitnessGoal = FitnessGoal.valueOf(value)
}
