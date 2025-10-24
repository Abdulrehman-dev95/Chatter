package com.example.chatter.ui.notification

import android.Manifest
import android.annotation.SuppressLint
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.chatter.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class Notification() : FirebaseMessagingService() {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        message.notification?.let {

            showNotification(it.title, it.body)
        }

    }


    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(title: String?, message: String?) {
        Firebase.auth.currentUser?.let {
            if (title?.contains(it.displayName.toString()) == true || message?.contains(it.displayName.toString()) == true) return
        }

        val notificationId = (1..1000).random()
        val channelId = "Chatter-Messages"

        val notification = NotificationCompat.Builder(this, channelId).apply {
            setContentTitle(title)
            setContentText(message)
            setSmallIcon(R.drawable.logo)
            setPriority(NotificationCompat.PRIORITY_MAX)

        }.build()
        NotificationManagerCompat.from(this).notify(notificationId, notification)

    }
}
