package com.solofit.app.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Plays subtle, calm wellness sounds.
 *
 * V1 uses ToneGenerator (DTMF tones shaped to sound warm) and
 * a synthesized sine-wave chime. Fully offline, no cloud, no AI.
 */
@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 60)

    /** Soft two-tone chime for completing an action (workout, goal, etc.). */
    fun playCompletionChime() {
        playChime(880f, 120, 200)
        playChime(1100f, 120, 350)
    }

    /** Single gentle tone for meditation start/end. */
    fun playMeditationTone() {
        playChime(660f, 200, 0)
    }

    /** Soft tap for logging an entry (food, journal, etc.). */
    fun playLogTone() {
        playChime(520f, 80, 0)
    }

    /** Gentle notification ping. */
    fun playNotificationPing() {
        toneGen.startTone(ToneGenerator.TONE_PROP_NACK, 100)
    }

    private fun playChime(freqHz: Float, durationMs: Int, delayMs: Int) {
        try {
            if (Build.VERSION.SDK_INT >= 21) {
                val sampleRate = 22050
                val numSamples = (sampleRate * durationMs / 1000f).toInt()
                val samples = ShortArray(numSamples)

                val envelope = 0.7f // amplitude
                for (i in 0 until numSamples) {
                    val t = i.toFloat() / sampleRate
                    val fade = 1.0f - (i.toFloat() / numSamples) * 0.6f // gentle decay
                    val sample = (envelope * fade * kotlin.math.sin(2.0 * Math.PI * freqHz * t)).toFloat()
                    samples[i] = (sample * Short.MAX_VALUE).toInt().toShort().coerceIn(Short.MIN_VALUE, Short.MAX_VALUE)
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(samples, 0, samples.size)
                track.setNotificationMarkerPosition(if (delayMs > 0) (delayMs * sampleRate / 1000f).toInt() else numSamples)
                track.play()
            } else {
                toneGen.startTone(ToneGenerator.TONE_PROP_NACK, durationMs.coerceAtMost(200))
            }
        } catch (_: Exception) {
            // silently fall back
            toneGen.startTone(ToneGenerator.TONE_PROP_NACK, durationMs.coerceAtMost(200))
        }
    }

    fun release() {
        toneGen.release()
    }
}
