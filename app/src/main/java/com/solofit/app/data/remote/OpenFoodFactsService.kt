package com.solofit.app.data.remote

import com.solofit.app.data.remote.dto.OffProductResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Open Food Facts read API (v2). No auth required.
 * `fields` query trims the payload to just what we deserialize.
 */
interface OpenFoodFactsService {

    @GET("api/v2/product/{barcode}.json")
    suspend fun getProduct(
        @Path("barcode") barcode: String,
        @Query("fields") fields: String =
            "code,product_name,brands,nutriments",
        // OFF asks every client to identify itself via User-Agent.
        @Header("User-Agent") userAgent: String = USER_AGENT
    ): OffProductResponse

    companion object {
        const val BASE_URL = "https://world.openfoodfacts.org/"
        const val USER_AGENT = "SoloFit/1.1 (Android; local-fitness-tracker)"
    }
}
