package com.kaltura.kflow.service

import android.app.*
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kaltura.kflow.BuildConfig
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.extension.notificationManager
import com.kaltura.kflow.presentation.extension.runSinceOreo
import com.kaltura.kflow.presentation.main.MainActivity

/**
 * Created by alex_lytvynenko on 23.01.2021.
 */
class FCMService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            sendNotification(it.title ?: "", it.body ?: "", remoteMessage.data)
        }
    }

    private fun sendNotification(title: String, body: String, data: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT)

        val channelId = BuildConfig.APPLICATION_ID
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setContentTitle(title)
                .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

        // Since android Oreo notification channel is needed.
        runSinceOreo {
            val channel = NotificationChannel(channelId, getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(channel)
        }

        val id = System.currentTimeMillis().toInt()
        notificationManager.notify(id, notificationBuilder.build())
    }
}