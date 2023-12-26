package me.goldhardt.woderful.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import dagger.hilt.android.qualifiers.ApplicationContext
import me.goldhardt.woderful.R
import me.goldhardt.woderful.data.model.ClockType
import me.goldhardt.woderful.presentation.MainActivity
import java.time.Duration
import javax.inject.Inject

class ExerciseNotificationManager @Inject constructor(
    @ApplicationContext val applicationContext: Context,
    val manager: NotificationManager
) {

    fun createNotificationChannel() {
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL,
            applicationContext.getString(R.string.title_wod_wip),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        manager.createNotificationChannel(notificationChannel)
    }

    fun buildNotification(
        type: ClockType,
        duration: Duration
    ): Notification {
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder =
            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)
                .setContentTitle(applicationContext.getString(R.string.title_wod_wip))
                .setSmallIcon(R.drawable.ic_kettlebell)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_WORKOUT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        val startMillis = SystemClock.elapsedRealtime() - duration.toMillis()
        val ongoingActivityStatus = Status.Builder()
            .addTemplate(ONGOING_STATUS_TEMPLATE)
            .addPart("clock", Status.TextPart(applicationContext.getString(type.displayName)))
            .addPart("duration", Status.StopwatchPart(startMillis))
            .build()
        val ongoingActivity =
            OngoingActivity.Builder(applicationContext, NOTIFICATION_ID, notificationBuilder)
                .setAnimatedIcon(R.drawable.ic_kettlebell)
                .setStaticIcon(R.drawable.ic_kettlebell)
                .setTouchIntent(pendingIntent)
                .setStatus(ongoingActivityStatus)
                .build()

        ongoingActivity.apply(applicationContext)

        return notificationBuilder.build()
    }

    companion object {
        const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL = "me.goldhardt.woderful.ONGOING_WORKOUT"
        private const val ONGOING_STATUS_TEMPLATE = "#clock# #duration#"
    }
}