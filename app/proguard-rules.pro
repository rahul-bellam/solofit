# SoloFit ProGuard rules

# ---- Room ----
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ---- Hilt / Dagger ----
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# ---- Kotlin coroutines ----
-keepclassmembernames class kotlinx.** { volatile <fields>; }

# ---- Entities ----
-keepclassmembers class com.solofit.app.data.local.entity.** { *; }

# ---- kotlinx.serialization ----
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.solofit.app.data.remote.dto.** { *; }
-keep,includedescriptorclasses class com.solofit.app.**$$serializer { *; }
-keepclassmembers class com.solofit.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.solofit.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---- Retrofit / OkHttp ----
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keepattributes Signature, Exceptions

# ---- TensorFlow Lite ----
-keep class org.tensorflow.lite.** { *; }
-dontwarn org.tensorflow.lite.**

# ---- ML Kit / Code Scanner ----
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.mlkit.**
