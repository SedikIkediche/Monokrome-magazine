package com.ssquare.myapplication.monokrome.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import java.io.File
import java.net.URI


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
const val NO_DOWNLOAD = -1
const val NO_PROGRESS = -1

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


fun deleteFile(uri: String): Boolean {
    if (uri == NO_FILE) return false
    Log.d("ListFragment", "fileUri is: $uri")
    val file = File(URI.create(uri))
    return if (file.exists()) {
        file.delete()
        true
    } else
        false
}