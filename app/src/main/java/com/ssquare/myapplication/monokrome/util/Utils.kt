package com.ssquare.myapplication.monokrome.util

import android.app.DownloadManager
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Uri
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.databinding.AlertDialogLayoutBinding
import com.ssquare.myapplication.monokrome.ui.auth.AuthActivity
import com.ssquare.myapplication.monokrome.util.networkcheck.ConnectivityProvider

const val MAGAZINE_PATH = "magazine_path"
const val HEADER_PATH = "header/header.jpg"
const val DATA_CACHED = "data_cached"
const val DATA_UP_TO_DATE = "data_outdated"
const val REFRESH_TIME = 1L
const val REQUEST_CODE: Int = 10
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
        .setDescription(magazine.description)
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)
    //set destination later

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val downloadId = downloadManager.enqueue(request)
}

inline fun < T : AppCompatActivity> AlertDialog.showLoading(activity :  T, textId: Int){
    val dialogBinding =
        AlertDialogLayoutBinding.inflate(LayoutInflater.from(activity as AuthActivity))
    dialogBinding.logInTextDialog.text = activity.getString(textId)
        this.setView(dialogBinding.root)
        this.setCancelable(false)
        this.show()
}

inline fun AlertDialog.hideDialog(){
    this.dismiss()
}

inline fun < T : AppCompatActivity> showErrorDialog(activity : T, message :String,buttonText : String,title: String){
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