package com.ssquare.myapplication.monokrome.util

import android.content.Context
import android.util.Log
import com.ssquare.myapplication.monokrome.data.DownloadState
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.data.Repository
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.Func
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

    private val fetchConfiguration = FetchConfiguration.Builder(context)
        .setDownloadConcurrentLimit(3)
        .build()

    private val fetch = Fetch.Impl.getInstance(fetchConfiguration)

    var downloadState = DownloadState.EMPTY
    var downloadId = NO_DOWNLOAD
    var progress = NO_PROGRESS

    val listener = object : FetchListener {

        override fun onAdded(download: Download) {}

        override fun onCancelled(download: Download) {
            updateDownloadFailed(download.id)
        }

        override fun onCompleted(download: Download) {}

        override fun onDeleted(download: Download) {}

        override fun onError(download: Download, error: Error, throwable: Throwable?) {}

        override fun onPaused(download: Download) {
            updateDownloadPaused(download.id)
        }

        override fun onProgress(
            download: Download,
            etaInMilliSeconds: Long,
            downloadedBytesPerSecond: Long
        ) {
            updateDownloadRunning(download.id, etaInMilliSeconds)
        }

        override fun onQueued(download: Download, waitingOnNetwork: Boolean) {}
        override fun onDownloadBlockUpdated(
            download: Download,
            downloadBlock: DownloadBlock,
            totalBlocks: Int
        ) {
        }

        override fun onRemoved(download: Download) {}
        override fun onResumed(download: Download) {}
        override fun onStarted(
            download: Download,
            downloadBlocks: List<DownloadBlock>,
            totalBlocks: Int
        ) {
        }

        override fun onWaitingNetwork(download: Download) {}
    }


    fun enqueueDownload(magazine: Magazine) {
        if (downloadState != DownloadState.EMPTY) return

        val fileUri = createUriString(magazine.id)
        val request = Request(magazine.editionUrl, fileUri).apply { networkType = NetworkType.ALL }

        fetch.enqueue(request,
            Func { updatedRequest: Request? ->
                updateDownloadPending(magazine.id, request.id)
            },
            Func { error: Error? ->
            }
        )
    }

    private fun updateDownloadRunning(
        dId: Int, inputProgress: Long
    ) {
        updateState(DownloadState.RUNNING)

        val inSeconds = (inputProgress / 1000).toInt()
        val inMinutes = (inSeconds / 60)

        val progress = if (inMinutes != 0) inMinutes else inSeconds
        repository.updateDownloadProgressByDid(dId, progress)
            Log.d("UtilsDownloadFile", "Download progress: $progress%")

    }



    private fun updateDownloadStarted(id: Long, downloadId: Long) {
        downloadState = DownloadState.RUNNING
        repository.updateDownloadState(id, DownloadState.RUNNING)
    }

    private fun updateDownloadCompleted(dId: Int, fileUri: String) {
        progress = 100
        updateState(DownloadState.COMPLETED)
        repository.updateFileUriByDid(downloadId, fileUri)
        repository.updateDownloadStateByDid(downloadId, DownloadState.COMPLETED)
        repository.updateDownloadProgressByDid(downloadId, 100)
        repository.updateDownloadIdByDid(downloadId, NO_DOWNLOAD)
        downloadId = NO_DOWNLOAD
        finishedDownloadBlock()
    }

    private fun updateDownloadFailed(dId: Int) {
        val fileUri = DOWNLOAD_DIRECTORY_URI +
        updateState(DownloadState.EMPTY)
        deleteFile(fileUri)
        repository.updateDownloadStateByDid(dId, DownloadState.EMPTY)
        repository.updateDownloadIdByDid(dId, NO_DOWNLOAD)
        repository.updateDownloadProgressByDid(dId, NO_PROGRESS)
        downloadId = NO_DOWNLOAD
        finishedDownloadBlock()
    }

    private fun updateDownloadPaused(dId: Int) {
        updateState(DownloadState.PAUSED)
        repository.updateDownloadStateByDid(dId, DownloadState.PAUSED)
    }

    private fun updateDownloadPending(magazineId: Long, downloadId: Int) {
        updateState(DownloadState.PENDING)
        repository.updateDownloadState(magazineId, DownloadState.PENDING)
        repository.updateDownloadId(magazineId, downloadId)
    }


    fun cancelDownload(dId: Int) {
        fetch.cancel(dId)
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

}