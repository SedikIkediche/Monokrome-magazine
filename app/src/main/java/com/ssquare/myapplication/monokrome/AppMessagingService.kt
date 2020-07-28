package com.ssquare.myapplication.monokrome

import android.app.NotificationManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ssquare.myapplication.monokrome.util.sendNotification

class AppMessagingService : FirebaseMessagingService() {



    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        Log.d(TAG, "From: ${remoteMessage?.from}")

        remoteMessage?.data?.let {
            Log.d(TAG, "Message data payload: ${it}")
        }

        remoteMessage?.notification?.let {
            Log.d(TAG, "Message Notification body: ${it.body}")
            sendNotification()
        }
    }


    override fun onNewToken(token: String?) {
        Log.d(TAG, "Refreshed token: $token")
    }


    private fun sendNotification() {
        val notificationManager = ContextCompat.getSystemService(applicationContext, NotificationManager::class.java) as NotificationManager
        notificationManager.sendNotification(applicationContext)
    }

    companion object {
        const val TAG = "AppMessagingService"
        const val TOPIC = "new_edition"
    }

}