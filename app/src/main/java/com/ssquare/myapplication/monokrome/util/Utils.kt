package com.ssquare.myapplication.monokrome.util

import android.app.DownloadManager
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.preference.PreferenceManager
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.work.RefreshDataWorker
import java.io.File
import java.net.URI
import java.util.concurrent.TimeUnit

const val MAGAZINE_ID = "magazine_path"
const val HEADER_PATH = "header/header.jpg"
const val DATA_CACHED = "data_cached"
const val REFRESH_TIME = 1L
const val REQUEST_CODE: Int = 10

const val PDF_TYPE = ".pdf"
const val DOWNLOAD_DIRECTORY_URI =
    "file:///storage/emulated/0/Android/data/com.ssquare.myapplication.monokrome/files/Download/"
const val NO_FILE = "no_file"
const val MAGAZINE_URI = "magazine_uri"

fun toast(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

fun isDataCached(context: Context): Boolean =
    PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
        DATA_CACHED, false
    )


fun commitCacheData(context: Context) {
    PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
        putBoolean(DATA_CACHED, true)
        apply()
    }
}


fun isConnected(context: Context): Boolean {
    val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = cm.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
}

fun launchUpdateWorker(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED).build()

    val cacheWorkRequest = OneTimeWorkRequest.Builder(RefreshDataWorker::class.java)
        .setInitialDelay(REFRESH_TIME, TimeUnit.DAYS)
        .setConstraints(constraints)
        .build()
    WorkManager.getInstance(context.applicationContext)
        .enqueue(cacheWorkRequest)
}


fun downloadFile(magazine: Magazine, context: Context) {
    //delete unfinished file if exists
    val uri = DOWNLOAD_DIRECTORY_URI + magazine.id + PDF_TYPE
    deleteFile(uri)

    val fileUrl = Uri.parse(magazine.editionUrl)
    val request = DownloadManager.Request(fileUrl)
        .setTitle(magazine.title)
        .setDescription(magazine.id.toString())
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)
        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
        .setDestinationInExternalFilesDir(
            context,
            Environment.DIRECTORY_DOWNLOADS,
            magazine.id.toString() + PDF_TYPE
        )
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val downloadId = downloadManager.enqueue(request)

}

fun deleteFile(uri: String): Boolean {
    val file = File(URI.create(uri))
    return if (file.exists()) {
        file.delete()
        true
    } else
        false
}