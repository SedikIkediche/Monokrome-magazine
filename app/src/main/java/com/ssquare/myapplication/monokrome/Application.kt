package com.ssquare.myapplication.monokrome

import android.app.Application
import timber.log.Timber

class MonokromeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}