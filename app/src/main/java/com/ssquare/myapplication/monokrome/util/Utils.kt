package com.ssquare.myapplication.monokrome.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ssquare.myapplication.monokrome.databinding.AlertDialogLayoutBinding
import com.ssquare.myapplication.monokrome.util.OrderBy.MOST_RECENT
import com.ssquare.myapplication.monokrome.util.OrderBy.values
import timber.log.Timber


const val AUTH_HEADER_KEY = "x-auth-token"
const val AUTH_PREF_KEY = "auth_token"

const val MAGAZINE_ID = "magazine_path"

const val DOWNLOAD_ACTIVE = "download_active"
const val LOAD_DATA_ACTIVE = "work_active"
const val UPLOAD_ACTIVE = "upload_active"
const val DATA_CACHED = "data_cached"
const val ORDER_BY = "order_by"

const val PDF_TYPE = ".pdf"

const val PDF_FILE_NAME = "pdf_file_name"
const val NO_DOWNLOAD = -1
const val NO_PROGRESS = -1

const val WRITE_EXTERNAL_STORAGE_PERMISSION_CODE = 100
const val SELECT_IMAGE_CODE = 1
const val SELECT_FILE_CODE = 2
const val UPLOAD_CODE = 3

const val JPEGTYPE  = "image/jpeg"
const val PNGTYPE =  "image/png"

const val PLAY_STORE = "market://details?id="
const val PACKAGE_NAME = "com.ssquare.myapplication.monokrome"
const val WEB_SITE = "https://www.monokromemag.com/"
const val FACEBOOK_BROWSER = "https://www.facebook.com/Monokromemag"
const val INSTAGRAM_App = "http://instagram.com/_u/monokromemag"
const val INSTAGRAM_PACKAGE = "com.instagram.android"
const val INSTAGRAM_BROWSER = "http://instagram.com/monokromemag"
const val USER_ADMIN_KEY = "isAdmin"

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
        PreferenceManager.getDefaultSharedPreferences(context)
            .getInt(ORDER_BY, MOST_RECENT.ordinal)
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

fun isUploadingActive(context: Context): Boolean =
    PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
        UPLOAD_ACTIVE, false
    )

fun commitUploadingActive(context: Context, state: Boolean) {
    PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
        putBoolean(UPLOAD_ACTIVE, state)
        apply()
    }
    Timber.d("commitWorkActive called")
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


inline fun AlertDialog.showLoading(context: Context, textId: Int) {
    val dialogBinding =
        AlertDialogLayoutBinding.inflate(LayoutInflater.from(context))
    dialogBinding.logInTextDialog.text = context.getString(textId)
    this.setView(dialogBinding.root)
    this.setCancelable(false)
    this.show()
}

inline fun AlertDialog.hideDialog() {
    this.dismiss()
}

inline fun showOneButtonDialog(
    context: Context,
    title: String,
    message: String,
    positiveButtonText: String,
    crossinline positiveFun: () -> Unit = {},
    crossinline dismissFun: () -> Unit = {}

): AlertDialog {
    return MaterialAlertDialogBuilder(context)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveButtonText) { dialog, which ->
            positiveFun()
            dialog.dismiss()
        }.setOnDismissListener {
            dismissFun()
        }
        .show()
}

inline fun showTwoButtonDialog(
    context: Context,
    title: String,
    message: String,
    positiveButtonText: String,
    negativeButtonText: String,
    crossinline positiveFun: () -> Unit = {},
    crossinline negativeFun: () -> Unit = {},
    crossinline dismissFun: () -> Unit = {}
): AlertDialog {
    return MaterialAlertDialogBuilder(context)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveButtonText) { dialog, which ->
            positiveFun()
            dialog.dismiss()
        }.setNegativeButton(negativeButtonText) { dialog, which ->
            dialog.dismiss()
            negativeFun()
        }.setOnDismissListener {
            dismissFun()
        }
        .show()
}


fun getPdfFileName(fileUrl: String): String? {
    return Uri.parse(fileUrl).lastPathSegment
}