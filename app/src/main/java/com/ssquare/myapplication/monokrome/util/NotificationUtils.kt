package com.ssquare.myapplication.monokrome.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.ui.main.MainActivity


private val REQUEST_CODE = 0
private val FLAGS = 0

const val CHANNEL_ID = "fcm_default_channel"
const val NOTIFICATION_ID = 0
fun NotificationManager.sendNotification(context: Context) {

    val intent = Intent(context, MainActivity::class.java)

    val pendingIntent = PendingIntent.getActivity(context, REQUEST_CODE, intent, FLAGS)

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher_icon)
        .setContentTitle(context.getString(R.string.new_edition_title))
        .setContentText(context.getString(R.string.new_edition_description))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = context.getString(R.string.new_edition_notification_channel_name)
        val descriptionText = context.getString(R.string.new_edition_notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
            enableLights(true)
            lightColor = Color.RED
            enableVibration(true)

        }
        this.createNotificationChannel(channel)
    }

    this.notify(NOTIFICATION_ID, builder.build())
}

fun NotificationManager.cancelNotifications() {
    cancelAll()
}