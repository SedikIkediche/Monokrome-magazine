package com.ssquare.myapplication.monokrome

import android.app.NotificationManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ssquare.myapplication.monokrome.util.sendNotification
import com.ssquare.myapplication.monokrome.work.RefreshDataWorker
import java.util.concurrent.TimeUnit

class AppMessagingService : FirebaseMessagingService() {


    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        Log.d(TAG, "onMessageReceived() called")
        Log.d(TAG, "From: ${remoteMessage?.from}")

        remoteMessage?.data?.let {
            Log.d(TAG, "Message data payload: ${it}")
        }

        remoteMessage?.notification?.let {
            Log.d(TAG, "Message Notification body: ${it.body}")
            //sendNotification()
        }
        sendNotification()
        setupRefreshDataWorker()
    }


    override fun onNewToken(token: String?) {
        Log.d(TAG, "Refreshed token: $token")
    }

    override fun onDeletedMessages() {
        Log.d(TAG, "Messages deleted")

    }


    private fun sendNotification() {
        val notificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java
        ) as NotificationManager
        notificationManager.sendNotification(applicationContext)
    }

    private fun setupRefreshDataWorker() {
        Log.d(TAG, "setupRefreshDataWorker() called")
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED).build()

        val cacheWorkRequest = OneTimeWorkRequest.Builder(RefreshDataWorker::class.java)
            // .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(applicationContext)
            .enqueue(cacheWorkRequest)
    }

    companion object {
        const val TAG = "AppMessagingService"
        const val TOPIC = "new_edition"
    }

}