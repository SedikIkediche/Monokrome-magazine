package com.ssquare.myapplication.monokrome.util

import  android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ssquare.myapplication.monokrome.data.DownloadState
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.data.Repository
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.FetchObserver
import com.tonyodev.fetch2core.Func
import com.tonyodev.fetch2core.Reason
import java.io.File
import java.net.URI

class DownloadUtils private constructor(
    val context: Context,
    private val repository: Repository,
    val block: (isDownloading: Boolean) -> Unit = {}
) {

    companion object {
        const val DOWNLOADS_TAG = "monokrome_downloads"
        var INSTANCE: DownloadUtils? = null
        fun getInstance(
            context: Context,
            repository: Repository, block: (isDownloading: Boolean) -> Unit = {}
        ): DownloadUtils {
            var instance = INSTANCE
            if (instance == null) {
                instance = DownloadUtils(
                    context.applicationContext,
                    repository,
                    block
                )
                INSTANCE = instance
            }
            return instance
        }
    }

    private val fetchConfiguration = FetchConfiguration.Builder(context)
        .setDownloadConcurrentLimit(10)
        .setProgressReportingInterval(1000)
        .setHasActiveDownloadsCheckInterval(5000)
        .build()

    private val listener = object : FetchListener {

        override fun onAdded(download: Download) {
            Log.d("DownloadUtils", "onAdded called")
        }

        override fun onCancelled(download: Download) {
            updateDownloadFailed(download.id, download.fileUri.toString())
            Log.d("DownloadUtils", "onCancelled called")
        }

        override fun onCompleted(download: Download) {
            updateDownloadCompleted(download.id, download.fileUri.toString())
            Log.d("DownloadUtils", "onCompleted called")
        }

        override fun onDeleted(download: Download) {
            updateDownloadFailed(download.id, download.fileUri.toString())
            Log.d("DownloadUtils", "onDeleted called")
        }

        override fun onError(download: Download, error: Error, throwable: Throwable?) {
            updateDownloadFailed(download.id, download.fileUri.toString())
            Log.d("DownloadUtils", "onError called")
        }

        override fun onPaused(download: Download) {
            updateDownloadPaused(download.id)
            Log.d("DownloadUtils", "onPaused called")
        }

        override fun onProgress(
            download: Download,
            etaInMilliSeconds: Long,
            downloadedBytesPerSecond: Long
        ) {
            updateDownloadProgress(download.id, download.progress)
            Log.d("DownloadUtils", "onProgress called")
        }

        override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
            updateDownloadStarted(download.id)
            Log.d("DownloadUtils", "onQueued called")
        }

        override fun onDownloadBlockUpdated(
            download: Download,
            downloadBlock: DownloadBlock,
            totalBlocks: Int
        ) {
            Log.d("DownloadUtils", "onDonwloadBlockUpdated called")
        }

        override fun onRemoved(download: Download) {
            Log.d("DownloadUtils", "onRemoved called")
        }

        override fun onResumed(download: Download) {
            updateDownloadResumed(download.id)
            Log.d("DownloadUtils", "onResumed called")
        }

        override fun onStarted(
            download: Download,
            downloadBlocks: List<DownloadBlock>,
            totalBlocks: Int
        ) {
            updateDownloadStarted(download.id)
            Log.d("DownloadUtils", "onStarted called")
        }

        override fun onWaitingNetwork(download: Download) {
            Log.d("DownloadUtils", "onWaitingForNetwork called")
        }
    }


    private val fetch = Fetch.Impl.getInstance(fetchConfiguration)


    var downloadState = DownloadState.EMPTY

    private val _isDownloadRunning = MutableLiveData<Boolean>()
    val isDownloadRunning: LiveData<Boolean>
        get() = _isDownloadRunning


    fun registerListener() {
        fetch.addActiveDownloadsObserver(true, object : FetchObserver<Boolean> {
            override fun onChanged(data: Boolean, reason: Reason) {
                Log.d("DownloadUtils", " activeDownloadsObserver triggered: $data")
                block(data)
            }

        })
        fetch.getDownloads(Func {
            it.forEach { download ->
                when (download.status) {
                    Status.COMPLETED -> updateDownloadCompleted(
                        download.id,
                        download.fileUri.toString()
                    )
                    Status.FAILED -> updateDownloadFailed(download.id, download.fileUri.toString())
                }
            }
        })
        fetch.addListener(listener)
        Log.d("DownloadUtils", "regist,erListener called")
    }

    fun unregisterListener() {
        fetch.removeListener(listener)
        Log.d("DownloadUtils", "unregisterListener called")
    }

    fun close() {
        fetch.close()
    }

    fun enqueueDownload(magazine: Magazine) {

        val fileUri = createUriString(magazine.id)
        val filePath = File(URI.create(fileUri)).path
        val request = Request(magazine.editionUrl, filePath).apply {
            networkType = NetworkType.ALL
            tag = DOWNLOADS_TAG
        }
        fetch.enqueue(request,
            Func { updatedRequest: Request? ->
                updateDownloadPending(magazine.id, request.id)
            },
            Func { error: Error? ->
                Log.d("DownloadUtils", "Error queueing download: error:${error}")
            }
        )
    }


    private fun updateDownloadPending(magazineId: Long, downloadId: Int) {
        updateState(DownloadState.PENDING)
        repository.updateDownloadProgress(magazineId, 0)
        repository.updateDownloadState(magazineId, DownloadState.PENDING)
        repository.updateDownloadId(magazineId, downloadId)
    }

    private fun updateDownloadStarted(dId: Int) {
        downloadState = DownloadState.RUNNING
        repository.updateDownloadStateByDid(dId, DownloadState.RUNNING)
    }

    private fun updateDownloadProgress(dId: Int, inputProgress: Int) {
        updateState(DownloadState.RUNNING)
        repository.updateDownloadProgressByDid(dId, inputProgress)
    }

    private fun updateDownloadPaused(dId: Int) {
        updateState(DownloadState.PAUSED)
        repository.updateDownloadStateByDid(dId, DownloadState.PAUSED)
    }

    private fun updateDownloadResumed(dId: Int) {
        updateState(DownloadState.RUNNING)
        repository.updateDownloadStateByDid(dId, DownloadState.RUNNING)
    }

    private fun updateDownloadCompleted(dId: Int, fileUri: String) {
        updateState(DownloadState.EMPTY)
        repository.updateFileUriByDid(dId, fileUri)
        repository.updateDownloadStateByDid(dId, DownloadState.COMPLETED)
        repository.updateDownloadProgressByDid(dId, 100)
        repository.updateDownloadIdByDid(dId, 100)
    }

    private fun updateDownloadFailed(dId: Int, fileUri: String) {

        updateState(DownloadState.EMPTY)
        deleteFile(fileUri)
        repository.updateDownloadStateByDid(dId, DownloadState.EMPTY)
        repository.updateDownloadIdByDid(dId, NO_DOWNLOAD)
        repository.updateDownloadProgressByDid(dId, NO_PROGRESS)
    }


    fun cancelDownload(dId: Int) {
        Log.d("DownloadUtils", "cancelDownload called")
        fetch.cancel(dId)
        fetch.remove(dId)
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