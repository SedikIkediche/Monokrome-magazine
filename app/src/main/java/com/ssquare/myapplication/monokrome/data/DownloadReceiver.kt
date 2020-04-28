package com.ssquare.myapplication.monokrome.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DownloadReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        //invalidate data then updateUi (Download -> Read)
    }
}
