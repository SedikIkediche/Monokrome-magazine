package com.ssquare.myapplication.monokrome.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.data.Repository
import com.ssquare.myapplication.monokrome.work.RefreshDataWorker
import com.ssquare.myapplication.monokrome.work.TerminateDownloadsWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
const val NO_DOWNLOAD = -1

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

fun launchCleanUpWorker(context: Context) {
    val cleanUpWorkRequest =
        OneTimeWorkRequest.Builder(TerminateDownloadsWorker::class.java).build()
    WorkManager.getInstance(context.applicationContext)
        .enqueue(cleanUpWorkRequest)
    Log.d("TerminateWorker", "launchCleanUpWorker called")
}


fun downloadFileWithDownloadManager(magazine: Magazine, context: Context) {
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
    var finishedDownload = false
    var progress = 0L
    CoroutineScope(Dispatchers.Main).launch {
        while (!finishedDownload) {
            val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
            if (cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                when (status) {
                    DownloadManager.STATUS_FAILED -> {
                        finishedDownload = true
                        toast(context, "Download Failed")
                        Log.d("UtilsDownloadFile", "Download Failed")
                    }
                    DownloadManager.STATUS_RUNNING -> {
                        val total =
                            cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        if (total >= 0) {
                            val downloadedSoFar =
                                cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            progress = downloadedSoFar * 100 / total
                            toast(context, "Download Progress ${progress}%")
                            Log.d("UtilsDownloadFile", "Download progress: $progress%")
                        }
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        progress = 100
                        finishedDownload = true
                    }
                }
            }
            delay(2000)
        }
    }
}


fun downloadWithPrDownloader(
    magazine: Magazine,
    context: Context,
    repository: Repository,
    recyclerView: RecyclerView
) {
    val dirPath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.path
    val fileUri = "file://" + dirPath + File.separator + magazine.id + PDF_TYPE
    var progress = 0

    val downloadId =
        PRDownloader.download(magazine.editionUrl, dirPath, magazine.id.toString() + PDF_TYPE)
            .build().setOnStartOrResumeListener {
                recyclerView.itemAnimator = null
                repository.updateDownloadProgress(magazine.id, progress)
                Log.d("PrDownloader", "onStartOrResume() called")
            }.setOnPauseListener {
                //updateUi
            }.setOnProgressListener {
                val currentProgress = (it.currentBytes * 100 / it.totalBytes).toInt()
                if (currentProgress > progress) {
                    progress = currentProgress
                    repository.updateDownloadProgress(magazine.id, progress)
                }
            }.setOnCancelListener {
                repository.deleteUnfinishedFile(magazine, fileUri)
                recyclerView.itemAnimator = DefaultItemAnimator()
                Log.d("PrDownloader", "onCancel() called")
            }.start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    repository.updateFileUri(magazine.id, fileUri)
                    repository.updateDownloadId(magazine.id, NO_DOWNLOAD)
                    recyclerView.itemAnimator = DefaultItemAnimator()
                    Log.d("PrDownloader", "onComplete() called")
                }

                override fun onError(error: Error?) {
                    repository.deleteUnfinishedFile(magazine, fileUri)
                    recyclerView.itemAnimator = DefaultItemAnimator()
                    Log.d("PrDownloader", "onError() called")
                }
            })

    repository.updateDownloadId(magazine.id, downloadId)
    Log.d("PrDownloader", "download: $downloadId")
}

fun deleteFile(uri: String): Boolean {
    Log.d("ListFragment", "fileUri is: $uri")
    val file = File(URI.create(uri))
    return if (file.exists()) {
        file.delete()
        true
    } else
        false
}