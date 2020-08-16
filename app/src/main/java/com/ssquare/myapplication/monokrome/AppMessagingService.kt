package com.ssquare.myapplication.monokrome

import android.app.NotificationManager
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.work.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ssquare.myapplication.monokrome.util.sendNotification
import com.ssquare.myapplication.monokrome.work.RefreshDataWorker
import timber.log.Timber
import java.util.concurrent.TimeUnit

class AppMessagingService : FirebaseMessagingService() {

    companion object {
        const val TAG = "AppMessagingService"
        const val TOPIC = "new_edition"
        const val TITLE_KEY = "title"
        const val DESCRIPTION_KEY = "description"


        private fun checkGooglePlayServices(context: Context): Boolean {
            val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
            return if (status != ConnectionResult.SUCCESS) {
                Timber.e("Error")
                // ask user to update google play services.
                false
            } else {
                Timber.i("Google play services updated")
                true
            }
        }

        fun subscribeTopic(context: Context) {
            if (checkGooglePlayServices(context)) {
                FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
                    .addOnCompleteListener { task ->
                        Timber.d(task.toString())
                        var msg = context.getString(R.string.message_subscribe_failed)
                        if (task.isSuccessful) {
                            msg = context.getString(R.string.message_subscribed)
                        }
                        Timber.d(msg)
                    }.addOnFailureListener { exception ->
                        Timber.e(exception, context.getString(R.string.message_subscribe_failed))
                    }
            } else {
                Timber.d("Error with GooglePlayServices")
            }

        }

        fun unsubscribeFromTopic() {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC)
        }
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        Timber.d("onMessageReceived() called")
        Timber.d("From: ${remoteMessage?.from}")

        remoteMessage?.data?.let {
            Timber.d("Message data payload: $it")
            val title = it[TITLE_KEY]
            val content = it[DESCRIPTION_KEY]
            if (title != null && content != null) {
                sendNotification(title, content)
                setupRefreshDataWorker()
            }

        }

    }


    override fun onNewToken(token: String?) {
        Timber.d("Refreshed token: $token")
    }

    override fun onDeletedMessages() {
        Timber.d("Messages deleted")

    }


    private fun sendNotification(title: String, content: String) {
        val notificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java
        ) as NotificationManager
        notificationManager.sendNotification(
            context = applicationContext,
            title = title,
            contentText = content
        )
    }

    private fun setupRefreshDataWorker() {
        Timber.d("setupRefreshDataWorker() called")
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED).build()

        val cacheWorkRequest = OneTimeWorkRequest.Builder(RefreshDataWorker::class.java)
            // .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(applicationContext)
            .enqueue(cacheWorkRequest)
    }


}