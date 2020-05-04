package com.ssquare.myapplication.monokrome.util

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.ssquare.myapplication.monokrome.data.DownloadState
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.data.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.net.URI

class DownloadUtils private constructor(
    val context: Context,
    val repository: Repository,
    val startDownloadBlock: () -> Unit = {},
    val finishedDownloadBlock: () -> Unit = {}
) {

    companion object {
        private const val POLLING_DELAY = 1000L

        var INSTANCE: DownloadUtils? = null
        fun getInstance(
            context: Context,
            repository: Repository, startDownloadBlock: () -> Unit = {},
            finishedDownloadBlock: () -> Unit = {}
        ): DownloadUtils {
            var instance = INSTANCE
            if (instance == null) {
                instance = DownloadUtils(
                    context.applicationContext,
                    repository,
                    startDownloadBlock,
                    finishedDownloadBlock
                )
                INSTANCE = instance
            }
            return instance
        }
    }

    private val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    var downloadState = DownloadState.EMPTY
    var downloadId = NO_DOWNLOAD
    var progress = NO_PROGRESS


    fun downloadFile(magazine: Magazine) {
        if (downloadState != DownloadState.EMPTY && magazine.downloadId != downloadId) {
            //Download already running
            return
        }

        val fileUri = createUriString(magazine.id)
        deleteFile(fileUri)

        val request = DownloadManager.Request(Uri.parse(magazine.editionUrl))
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


        downloadId = downloadManager.enqueue(request)
        downloadState = DownloadState.PENDING
        updateDownloadStarted(magazine.id, downloadId)
        startDownloadBlock()

        CoroutineScope(Dispatchers.IO).launch {
            while (downloadState != DownloadState.COMPLETED && downloadState != DownloadState.EMPTY) {
                val cursor =
                    downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
                if (cursor.moveToFirst()) {

                    when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_PENDING -> {
                            updateDownloadPending(magazine.id)
                        }
                        DownloadManager.STATUS_PAUSED -> {
                            updateDownloadPaused(magazine.id)
                        }
                        DownloadManager.STATUS_RUNNING -> {
                            updateDownloadRunning(cursor, magazine)
                        }
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            updateDownloadCompleted(magazine.id, fileUri)
                        }
                        else -> { //download failed
                            updateDownloadFailed(magazine.id, fileUri)
                        }
                    }

                } else { //download canceled
                    updateDownloadFailed(magazine.id, fileUri)
                }
                cursor.close()
                delay(POLLING_DELAY)
            }
        }

    }

    private fun updateDownloadRunning(
        cursor: Cursor,
        magazine: Magazine
    ) {
        updateState(DownloadState.RUNNING)
        val total =
            cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
        if (total >= 0) {
            val downloadedSoFar =
                cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
            progress = (downloadedSoFar * 100 / total).toInt()
            repository.updateDownloadProgress(magazine.id, progress)
            Log.d("UtilsDownloadFile", "Download progress: $progress%")
        }
    }

    private fun updateDownloadStarted(id: Long, downloadId: Long) {
        downloadState = DownloadState.PENDING
        repository.updateDownloadState(id, DownloadState.RUNNING)
        repository.updateDownloadId(id, downloadId)
    }

    private fun updateDownloadCompleted(magazineId: Long, fileUri: String) {
        progress = 100
        updateState(DownloadState.COMPLETED)
        updateDownloadCompleted(magazineId, fileUri)
        repository.updateFileUri(magazineId, fileUri)
        repository.updateDownloadState(magazineId, DownloadState.COMPLETED)
        repository.updateDownloadProgress(magazineId, 100)
        repository.updateDownloadId(magazineId, NO_DOWNLOAD)
        downloadId = NO_DOWNLOAD
        finishedDownloadBlock()
    }

    private fun updateDownloadFailed(magazineId: Long, fileUri: String) {
        updateState(DownloadState.EMPTY)
        deleteFile(fileUri)
        repository.updateDownloadState(magazineId, DownloadState.EMPTY)
        repository.updateDownloadId(magazineId, NO_DOWNLOAD)
        repository.updateDownloadProgress(magazineId, NO_PROGRESS)
        downloadId = NO_DOWNLOAD
        finishedDownloadBlock()
    }

    private fun updateDownloadPaused(magazineId: Long) {
        updateState(DownloadState.PAUSED)
        repository.updateDownloadState(magazineId, DownloadState.PAUSED)
    }

    private fun updateDownloadPending(magazineId: Long) {
        updateState(DownloadState.PENDING)
    }


    fun cancelDownload(magazine: Magazine) {
        downloadManager.remove(magazine.downloadId)
    }

    private fun createUriString(id: Long): String {
        return DOWNLOAD_DIRECTORY_URI + id.toString() + PDF_TYPE
    }

    private fun deleteFile(uri: String): Boolean {
        if (uri == NO_FILE) return false
        val file = File(URI.create(uri))
        return if (file.exists()) {
            file.delete()
            true
        } else
            false
    }

    private fun updateState(downloadState: DownloadState) {
        if (this.downloadState != downloadState) this.downloadState = downloadState
    }

    fun cancelPaused(magazine: Magazine) {
        CoroutineScope(Dispatchers.IO).launch {
            val cursor =
                downloadManager.query(DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_PAUSED))
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID))
                    downloadManager.remove(id)
                } while (cursor.moveToNext())

            }

        }
    }

}