package com.ssquare.myapplication.monokrome.util

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ssquare.myapplication.monokrome.databinding.AlertDialogLayoutBinding
import com.ssquare.myapplication.monokrome.util.OrderBy.MOST_RECENT
import com.ssquare.myapplication.monokrome.util.OrderBy.values
import com.ssquare.myapplication.monokrome.util.networkcheck.ConnectivityProvider


const val AUTH_HEADER_KEY = "x-auth-token"
const val AUTH_PREF_KEY = "auth_token"

const val MAGAZINE_ID = "magazine_path"
//const val HEADER_PATH = "header/header.jpg"
const val HEADER_PATH = "header.jpg"
const val DOWNLOAD_ACTIVE = "download_active"
const val LOAD_DATA_ACTIVE = "work_active"
const val DATA_CACHED = "data_cached"
const val ORDER_BY = "order_by"

const val PDF_TYPE = ".pdf"


const val MAGAZINE_URI = "magazine_uri"
const val NO_DOWNLOAD = -1
const val NO_PROGRESS = -1

const val WRITE_EXTERNAL_STORAGE_PERMISSION_CODE = 100
const val READ_EXTERNAL_STORAGE_PERMISSION_CODE = 101
const val SELECT_IMAGE_CODE = 1
const val SELECT_FILE_CODE = 2
const val UPLOAD_CODE = 3

fun getAuthToken(context: Context): String? {
    return PreferenceManager.getDefaultSharedPreferences(context).getString(
        AUTH_PREF_KEY,
        null
    )
}

fun storeAuthToken(context: Context, authToken: String) {
    PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
        putString(AUTH_PREF_KEY, authToken)
        apply()
    }
}

fun deleteAuthToken(context: Context) {
    PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
        putString(AUTH_PREF_KEY, null)
        apply()
    }
}



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

fun getBitmapFromVectorDrawable(context: Context?, drawableId: Int): Bitmap? {
    var drawable = ContextCompat.getDrawable(context!!, drawableId)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        drawable = DrawableCompat.wrap(drawable!!).mutate()
    }
    val bitmap = Bitmap.createBitmap(
        drawable!!.intrinsicWidth,
        drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}


inline fun <T : AppCompatActivity> AlertDialog.showLoading(activity: T, textId: Int) {
    val dialogBinding =
        AlertDialogLayoutBinding.inflate(LayoutInflater.from(activity))
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