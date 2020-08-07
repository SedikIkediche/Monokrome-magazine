package com.ssquare.myapplication.monokrome.util

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.data.Repository
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.Func
import java.io.File
import java.net.URI


class DownloadUtils(
    private val context: Context,
    private val repository: Repository
) {

    private val fetchConfiguration = FetchConfiguration.Builder(context)
        .setDownloadConcurrentLimit(10)
        .setProgressReportingInterval(1000)
        .build()

    private var fetch = Fetch.Impl.getInstance(fetchConfiguration)
    private  var authToken:String? = null

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
            Log.d("DownloadUtils", "onError called: $error")
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
            triggerActiveDownloads()
            Log.d("DownloadUtils", "onDonwloadBlockUpdated called")
        }

        override fun onRemoved(download: Download) {
            updateDownloadFailed(download.id, download.fileUri.toString())
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

    var downloadState = DownloadState.EMPTY

    private val _isDownloadRunning = MutableLiveData<Boolean>()
    val isDownloadRunning: LiveData<Boolean>
        get() = _isDownloadRunning


    private fun init() {
        if (fetch.isClosed) fetch = Fetch.Impl.getInstance(fetchConfiguration)
    }

    fun setToken(token: String?){
        authToken = token
    }

    private fun triggerActiveDownloads() {
        fetch.getDownloads(Func { list ->
            val activeDownloadsList =
                list.filter { it.status == Status.QUEUED || it.status == Status.DOWNLOADING || it.status == Status.PAUSED }
            _isDownloadRunning.postValue(activeDownloadsList.isNotEmpty())
        })
    }

    fun registerListener() {
        init()
        triggerActiveDownloads()
        fetch.getDownloads(Func {
            it.forEach { download ->
                when (download.status) {
                    Status.COMPLETED -> updateDownloadCompleted(
                        download.id,
                        download.fileUri.toString()
                    )
                    Status.FAILED -> updateDownloadFailed(download.id, download.fileUri.toString())
                    Status.NONE -> {}
                    Status.QUEUED -> {}
                    Status.DOWNLOADING -> {}
                    Status.PAUSED -> {}
                    Status.CANCELLED -> {}
                    Status.REMOVED -> {}
                    Status.DELETED -> {}
                    Status.ADDED -> {}
                }
            }
        })
        Log.d("DownloadUtils", "registerListener called")
        fetch.addListener(listener)
    }

    fun unregisterListener() {
        Log.d("DownloadUtils", "unregisterListener called")
        init()
        fetch.removeListener(listener)
    }

    fun clear(){
        authToken = null
        //clear all downloads from downloadUtils and database and files
    }

    fun close() {
        fetch.close()
    }

    fun enqueueDownload(magazine: Magazine) {
        Log.d("DownloadUtils", "Magazine: $magazine")
        val filePath = createFilePath(magazine.id)
        val request = Request(magazine.editionUrl, filePath).apply {
            networkType = NetworkType.ALL
            addHeader(AUTH_HEADER_KEY, authToken!!)
        }
        fetch.enqueue(request,
            Func {
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
        repository.updateDownloadStateByDid(dId, DownloadState.COMPLETED)
        repository.updateFileUriByDid(dId, fileUri)
        repository.updateDownloadIdByDid(dId, NO_DOWNLOAD)
        triggerActiveDownloads()
    }

    private fun updateDownloadFailed(dId: Int, fileUri: String) {
        updateState(DownloadState.EMPTY)
        repository.updateDownloadStateByDid(dId, DownloadState.EMPTY)
        deleteFile(fileUri)
        repository.updateDownloadIdByDid(dId, NO_DOWNLOAD)
        repository.updateDownloadProgressByDid(dId, NO_PROGRESS)
        triggerActiveDownloads()
    }


    fun cancelDownload(dId: Int) {
        Log.d("DownloadUtils", "cancelDownload called")
        fetch.cancel(dId)
        fetch.remove(dId)
    }

    private fun createFilePath(id: Long): String {
        return context.applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.path + id.toString() + PDF_TYPE
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