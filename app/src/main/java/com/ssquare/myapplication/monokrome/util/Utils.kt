package com.ssquare.myapplication.monokrome.util

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ssquare.myapplication.monokrome.databinding.AlertDialogLayoutBinding
import com.ssquare.myapplication.monokrome.ui.auth.AuthActivity
import com.ssquare.myapplication.monokrome.util.OrderBy.MOST_RECENT
import com.ssquare.myapplication.monokrome.util.OrderBy.values
import com.ssquare.myapplication.monokrome.util.networkcheck.ConnectivityProvider
import java.io.File
import java.net.URI

const val AUTH_HEADER_KEY = "x-auth-token"
 const val AUTH_TOKEN =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6NiwiaXNBZG1pbiI6dHJ1ZSwiaWF0IjoxNTk1NTAzMzI0fQ.mzWfcFy4i1HDl7D_J2AF48UC2P_2Mm52hQuBLcWmam0"

const val MAGAZINE_ID = "magazine_path"
//const val HEADER_PATH = "header/header.jpg"
const val HEADER_PATH = "header.jpg"
const val DOWNLOAD_ACTIVE = "download_active"
const val LOAD_DATA_ACTIVE = "work_active"
const val DATA_CACHED = "data_cached"
const val ORDER_BY = "order_by"

const val PDF_TYPE = ".pdf"

const val FILE_PREFIX = "file://"
const val NO_FILE = "no_file"
const val MAGAZINE_URI = "magazine_uri"
const val NO_DOWNLOAD = -1
const val STORAGE_PERMISSION_CODE = 100
const val NO_PROGRESS = -1

fun toast(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

fun getOrderBy(context: Context): OrderBy {
    val ordinal =
        PreferenceManager.getDefaultSharedPreferences(context).getInt(ORDER_BY, MOST_RECENT.ordinal)
    return values()[ordinal]
}

fun commitOrderBy(context: Context, orderBy: OrderBy) {
    PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
        putInt(ORDER_BY, orderBy.ordinal)
        apply()
    }
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

fun isLoadDataActive(context: Context): Boolean =
    PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
        LOAD_DATA_ACTIVE, false
    )

fun commitLoadDataActive(context: Context, state: Boolean) {
    PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
        putBoolean(LOAD_DATA_ACTIVE, state)
        apply()
    }
    Log.d("Utils", "commitWorkActive called")
}

fun isDownloadActive(context: Context): Boolean =
    PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
        DOWNLOAD_ACTIVE, false
    )

fun commitDownloadActive(context: Context, state: Boolean) {
    PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
        putBoolean(DOWNLOAD_ACTIVE, state)
        apply()
    }
}


fun createUriString(context: Context, id: Long): String {
    return FILE_PREFIX +
            context.applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.path + id.toString() + PDF_TYPE
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

inline fun <T : AppCompatActivity> AlertDialog.showLoading(activity: T, textId: Int) {
    val dialogBinding =
        AlertDialogLayoutBinding.inflate(LayoutInflater.from(activity as AuthActivity))
    dialogBinding.logInTextDialog.text = activity.getString(textId)
    this.setView(dialogBinding.root)
    this.setCancelable(false)
    this.show()
}

inline fun AlertDialog.hideDialog() {
    this.dismiss()
}

inline fun <T : AppCompatActivity> showErrorDialog(
    activity: T,
    message: String,
    buttonText: String,
    title: String
) {
    MaterialAlertDialogBuilder(activity)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(buttonText) { dialog, which ->
            dialog.dismiss()
        }
        .show()
}

fun ConnectivityProvider.NetworkState.hasInternet(): Boolean {
    return (this as? ConnectivityProvider.NetworkState.ConnectedState)?.hasInternet == true
}