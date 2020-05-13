package com.ssquare.myapplication.monokrome.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ssquare.myapplication.monokrome.ui.main.MainActivity

class DownloadReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val activityIntent = Intent(context.applicationContext, MainActivity::class.java)
        context.startActivity(activityIntent)


    }


}
