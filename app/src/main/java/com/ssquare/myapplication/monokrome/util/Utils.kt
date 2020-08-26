package com.ssquare.myapplication.monokrome.util

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.ConnectivityManager
import android.os.Build
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
import timber.log.Timber


const val AUTH_HEADER_KEY = "x-auth-token"
const val AUTH_PREF_KEY = "auth_token"

//const val NO_AUTH_TOKEN = "no_auth_token"
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

const val WRITE_EXTERNAL_STORAGE_PERMISSION_CODE = 100
const val READ_EXTERNAL_STORAGE_PERMISSION_CODE = 101
const val SELECT_IMAGE_CODE = 1
const val SELECT_FILE_CODE = 2
const val UPLOAD_CODE = 3

const val PLAY_STORE = "market://details?id="
const val PACKAGE_NAME = "com.ssquare.myapplication.monokrome"
const val WEB_SITE = "https://www.monokromemag.com/"
const val FACEBOOK_BROWSER = "https://www.facebook.com/Monokromemag"
const val FACEBOOK_APP = "fb://facewebmodal/f?href="
const val FACEBOOK_PACKAGE = "com.facebook.katana"
const val FACEBOOK_ID = "1816635888599066"
const val INSTAGRAM_App = "http://instagram.com/_u/monokromemag"
const val INSTAGRAM_PACKAGE = "com.instagram.android"
const val INSTAGRAM_BROWSER = "http://instagram.com/monokromemag"

fun isEmailValid(target: CharSequence): Boolean {
    val isValidEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
    Timber.d("email: $target, is valid email: $isValidEmail")
    return isValidEmail
}

fun isPasswordValid(target: CharSequence): Boolean {
    val isValid = target.isNotEmpty() && target.length >= 5
    Timber.d("password: $target, is valid password: $isValid")
    return isValid
}

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
    Timber.d("commitWorkActive called")
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

inline fun <T : AppCompatActivity> showOneButtonDialog(
    activity: T,
    title: String,
    message: String,
    positiveButtonText: String,
    crossinline positiveFun: () -> Unit = {}
) {
    MaterialAlertDialogBuilder(activity)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveButtonText) { dialog, which ->
            positiveFun()
            dialog.dismiss()
        }
        .show()
}

inline fun <T : AppCompatActivity> showTwoButtonDialog(
    activity: T,
    title: String,
    message: String,
    positiveButtonText: String,
    negativeButtonText: String,
    crossinline positiveFun: () -> Unit = {},
    crossinline negativeFun: () -> Unit = {}
) {
    MaterialAlertDialogBuilder(activity)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveButtonText) { dialog, which ->
            positiveFun()
            dialog.dismiss()
        }.setNegativeButton(negativeButtonText) { dialog, which ->
            negativeFun()
            dialog.dismiss()
        }
        .show()
}

fun ConnectivityProvider.NetworkState.hasInternet(): Boolean {
    return (this as? ConnectivityProvider.NetworkState.ConnectedState)?.hasInternet == true
}