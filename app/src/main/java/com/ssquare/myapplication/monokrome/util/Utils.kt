package com.ssquare.myapplication.monokrome.util

import android.app.DownloadManager
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Uri
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.ssquare.myapplication.monokrome.data.Magazine

const val MAGAZINE_PATH = "magazine_path"
const val HEADER_PATH = "header/header.png"
const val DATA_CACHED = "data_cached"
const val DATA_UP_TO_DATE = "data_outdated"

fun toast(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

fun isDataCached(context: Context): Boolean =
    PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
        DATA_CACHED, false
    )

fun isUpToDate(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)
    .getBoolean(DATA_UP_TO_DATE, false)


fun isConnected(context: Context): Boolean {
    val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = cm.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
}

fun commitCacheData(context: Context, isUpToDate: Boolean, isCached: Boolean) {
    PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
        putBoolean(DATA_UP_TO_DATE, isUpToDate)
        putBoolean(DATA_CACHED, isCached)
        apply()
    }
}

fun downloadFile(magazine: Magazine, context: Context) {
    //ask for storage permission
    val fileUrl = Uri.parse(magazine.editionUrl)
    val request = DownloadManager.Request(fileUrl)
        .setTitle(magazine.title)
        .setDescription(magazine.description)
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)
    //set destination later

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val downloadId = downloadManager.enqueue(request)
}
