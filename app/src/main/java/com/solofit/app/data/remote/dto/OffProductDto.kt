package com.solofit.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Minimal Open Food Facts response model.
 * Endpoint: https://world.openfoodfacts.org/api/v2/product/{barcode}.json
 * We only deserialize the fields we use to keep parsing light.
 */
@Serializable
data class OffProductResponse(
    @SerialName("status") val status: Int = 0,           // 1 = found, 0 = not found
    @SerialName("code") val code: String? = null,
    @SerialName("product") val product: OffProduct? = null
)

@Serializable
data class OffProduct(
    @SerialName("product_name") val productName: String? = null,
    @SerialName("product_name_en") val productNameEn: String? = null,
    @SerialName("generic_name") val genericName: String? = null,
    @SerialName("brands") val brands: String? = null,
    @SerialName("nutriments") val nutriments: OffNutriments? = null
)

/**
 * Open Food Facts exposes "*_100g" fields already normalized per 100g, which maps
 * directly onto our FoodItemEntity's per-100g schema. "energy-kcal_100g" is the
 * preferred calorie field; we fall back to converting kJ if it's missing.
 */
@Serializable
data class OffNutriments(
    @SerialName("energy-kcal_100g") val energyKcal100g: Double? = null,
    @SerialName("energy_100g") val energyKj100g: Double? = null,
    @SerialName("proteins_100g") val proteins100g: Double? = null,
    @SerialName("carbohydrates_100g") val carbs100g: Double? = null,
    @SerialName("fat_100g") val fat100g: Double? = null,
    @SerialName("fiber_100g") val fiber100g: Double? = null
)
