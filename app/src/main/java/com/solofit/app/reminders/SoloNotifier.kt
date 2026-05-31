package com.solofit.app.reminders

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.solofit.app.MainActivity
import com.solofit.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralizes "silent" notifications.
 *
 * Silence is achieved by using IMPORTANCE_LOW channels: the notification appears
 * in the shade (and as a badge), but produces **no sound, no vibration, and no
 * heads-up pop-up**. We also defensively call setSound(null,null) and disable
 * vibration/lights on the channel.
 */
@Singleton
class SoloNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun ensureChannels() {
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        nm.createNotificationChannel(silentChannel(CHANNEL_HYDRATION, "Hydration reminders"))
        nm.createNotificationChannel(silentChannel(CHANNEL_WORKOUT, "Workout reminders"))
        nm.createNotificationChannel(silentChannel(CHANNEL_JOURNAL, "Journal prompts"))
    }

    private fun silentChannel(id: String, name: String): NotificationChannel =
        NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW).apply {
            description = "Gentle, silent nudges — no sound or vibration."
            setSound(null, null)
            enableVibration(false)
            enableLights(false)
            setShowBadge(true)
        }

    fun notify(channelId: String, notificationId: Int, title: String, message: String) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        ensureChannels()

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pending = PendingIntent.getActivity(
            context,
            notificationId,
            tapIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_solofit)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_LOW)   // pre-O silence
            .setSilent(true)                                 // never makes sound
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (se: SecurityException) {
            // POST_NOTIFICATIONS not granted — ignore silently.
        }
    }

    companion object {
        const val CHANNEL_HYDRATION = "hydration_reminders"
        const val CHANNEL_WORKOUT = "workout_reminders"
        const val CHANNEL_JOURNAL = "journal_prompts"

        const val ID_HYDRATION = 1001
        const val ID_WORKOUT = 1002
        const val ID_MORNING = 1003
        const val ID_EVENING = 1004
    }
}
