package com.solofit.app.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.solofit.app.BuildConfig
import com.solofit.app.data.remote.GeminiService
import com.solofit.app.data.remote.OpenFoodFactsService
import com.solofit.app.data.remote.UsdaFoodService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class UsdaRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient {
        // BASIC logging prints the request line, which includes the full URL. The
        // Gemini/USDA keys are passed as `?key=…`/`?api_key=…` query params, so we
        // route the log through a redacting logger to keep keys out of logcat.
        val logging = HttpLoggingInterceptor { message ->
            android.util.Log.d("OkHttp", KEY_QUERY_REGEX.replace(message, "$1=***"))
        }.apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val KEY_QUERY_REGEX = Regex("(?i)\\b(key|api_key|apikey)=[^&\\s]+")

    @Provides
    @Singleton
    fun provideRetrofit(json: Json, client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(OpenFoodFactsService.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideOpenFoodFactsService(retrofit: Retrofit): OpenFoodFactsService =
        retrofit.create(OpenFoodFactsService::class.java)

    @Provides
    @Singleton
    @UsdaRetrofit
    fun provideUsdaRetrofit(json: Json, client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(UsdaFoodService.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideUsdaFoodService(@UsdaRetrofit retrofit: Retrofit): UsdaFoodService =
        retrofit.create(UsdaFoodService::class.java)

    @Provides
    @Singleton
    @GeminiRetrofit
    fun provideGeminiRetrofit(json: Json, client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(GeminiService.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideGeminiService(@GeminiRetrofit retrofit: Retrofit): GeminiService =
        retrofit.create(GeminiService::class.java)
}

@Qualifier
annotation class GeminiRetrofit
