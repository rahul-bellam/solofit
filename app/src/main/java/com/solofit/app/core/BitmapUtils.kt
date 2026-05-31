package com.solofit.app.core

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlin.math.max

/**
 * Memory-safe bitmap helpers.
 *
 * The classifier only needs a small (224x224) image, so decoding a full 12 MP
 * photo (~48 MB as ARGB_8888) into RAM is pure waste. These helpers decode at a
 * downsampled resolution using BitmapFactory's bounds-only first pass, keeping
 * peak memory tiny.
 */
object BitmapUtils {

    /** Target longest-edge in px; comfortably above the model's 224 input. */
    const val TARGET_MAX_EDGE = 512

    /**
     * Decode a content Uri into a downsampled ARGB_8888 bitmap whose longest edge
     * is <= [maxEdge]. Returns null if the stream can't be read.
     */
    fun decodeSampled(
        resolver: ContentResolver,
        uri: Uri,
        maxEdge: Int = TARGET_MAX_EDGE
    ): Bitmap? {
        // Pass 1: read bounds only (no pixel allocation).
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        // Pass 2: decode with an inSampleSize power-of-two downscale.
        val opts = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxEdge)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        }
    }

    /**
     * Defensive cap for an already-decoded bitmap (e.g. a camera preview thumbnail).
     * Scales it down if it somehow exceeds [maxEdge]; otherwise returns it as-is.
     */
    fun capInMemory(bitmap: Bitmap, maxEdge: Int = TARGET_MAX_EDGE): Bitmap {
        val longest = max(bitmap.width, bitmap.height)
        if (longest <= maxEdge) return bitmap
        val scale = maxEdge.toFloat() / longest
        val scaled = Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt().coerceAtLeast(1),
            (bitmap.height * scale).toInt().coerceAtLeast(1),
            true
        )
        // Free the original if a new bitmap was actually produced.
        if (scaled != bitmap) bitmap.recycle()
        return scaled
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxEdge: Int): Int {
        var sample = 1
        var w = width
        var h = height
        while (max(w, h) / 2 >= maxEdge) {
            w /= 2
            h /= 2
            sample *= 2
        }
        return sample
    }
}
