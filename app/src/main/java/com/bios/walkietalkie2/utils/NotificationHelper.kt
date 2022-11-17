package com.bios.walkietalkie2.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.bios.walkietalkie2.R
import com.bios.walkietalkie2.common.AppClass

object NotificationHelper {
    const val SOCKET_NOTIFICATION_ID = 101
    private const val CHANNEL_ID = "yacup_walkie_talkie"
    private const val CHANNEL_NAME = "YaCup Walkie Talkie Channel"

    fun getNotificationChanelId() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_NONE
            )
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val service = AppClass.getApp().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(chan)
            CHANNEL_ID
        } else {
            ""
        }

    fun showSocketListeningNotification(): Notification {
        val notificationBuilder = NotificationCompat.Builder(AppClass.getApp(), getNotificationChanelId())
        return notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }
}
