package com.ssquare.myapplication.monokrome.util

import android.app.DownloadManager
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.ssquare.myapplication.monokrome.data.Magazine

const val MAGAZINE_ID = "magazine_path"
const val HEADER_PATH = "header/header.jpg"
const val DATA_CACHED = "data_cached"
const val REFRESH_TIME = 1L
const val REQUEST_CODE: Int = 10

const val PDF_TYPE = ".pdf"
const val DOWNLOAD_DIRECTORY_URI =
    "file:///storage/emulated/0/Android/data/com.ssquare.myapplication.monokrome/files/Download/"
const val NO_FILE = "no_file"

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


fun downloadFile(magazine: Magazine, context: Context) {
    //ask for storage permission
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
    //set destination later

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val downloadId = downloadManager.enqueue(request)
}
