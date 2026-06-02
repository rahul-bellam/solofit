package com.solofit.app.data.remote

import com.solofit.app.BuildConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface UsdaFoodService {

    @GET("fdc/v1/foods/search")
    suspend fun searchFoods(
        @Query("api_key") apiKey: String = BuildConfig.USDA_API_KEY,
        @Query("query") query: String,
        @Query("pageSize") pageSize: Int = 5,
        @Query("dataType") dataType: String = "Foundation,SR Legacy,Branded"
    ): UsdaSearchResponse

    companion object {
        const val BASE_URL = "https://api.nal.usda.gov/"
    }
}

@Serializable
data class UsdaSearchResponse(
    @SerialName("foods") val foods: List<UsdaFood> = emptyList(),
    @SerialName("totalHits") val totalHits: Int = 0
)

@Serializable
data class UsdaFood(
    @SerialName("fdcId") val fdcId: Long,
    @SerialName("description") val description: String,
    @SerialName("foodNutrients") val foodNutrients: List<UsdaNutrient> = emptyList()
)

@Serializable
data class UsdaNutrient(
    @SerialName("nutrientId") val nutrientId: Int,
    @SerialName("value") val value: Double? = null,
    @SerialName("nutrientName") val nutrientName: String? = null
)
