package com.ssquare.myapplication.monokrome

import android.app.Application
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig

class MagazineApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = PRDownloaderConfig.newBuilder()
            .setDatabaseEnabled(false)
            .build()
        PRDownloader.initialize(
            applicationContext,
            config
        )
    }

}