package com.ssquare.myapplication.monokrome.util

import android.content.Context
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ssquare.myapplication.monokrome.data.DomainMagazine
import com.ssquare.myapplication.monokrome.data.Repository
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.Func
import timber.log.Timber
import java.io.File
import java.net.URI
import java.util.*


class DownloadUtils(
    private val context: Context,
    private val repository: Repository
) {

    private val fetchConfiguration = FetchConfiguration.Builder(context)
        .setDownloadConcurrentLimit(10)
        .setProgressReportingInterval(1000)
        .enableAutoStart(false)
        .build()

    private var fetch = Fetch.Impl.getInstance(fetchConfiguration)
    private var errorCallback: ((downloadCallBack : DownloadCallBack) -> Unit)? = null

    fun setErrorCallback(errorCallback: (downloadCallBack : DownloadCallBack) -> Unit){
        this.errorCallback = errorCallback
    }

    private val listener = object : FetchListener {

        override fun onAdded(download: Download) {
            Timber.d("onAdded called")
        }

        override fun onCancelled(download: Download) {
            updateDownloadFailed(download.id, download.fileUri.toString())
            Timber.d("onCancelled called")
        }

        override fun onCompleted(download: Download) {
            updateDownloadCompleted(download.id, download.fileUri.toString())
            download.file
            Timber.d("onCompleted called $download.file")
        }

        override fun onDeleted(download: Download) {
            updateDownloadFailed(download.id, download.fileUri.toString())
            Timber.d("onDeleted called")
        }

        override fun onError(download: Download, error: Error, throwable: Throwable?) {
            updateDownloadFailed(download.id, download.fileUri.toString())
            errorCallback?.invoke(DownloadCallBack.ONERROR)
            Timber.d("onError called: $error")
        }

        override fun onPaused(download: Download) {
            updateDownloadPaused(download.id)
            Timber.d("onPaused called")
        }

        override fun onProgress(
            download: Download,
            etaInMilliSeconds: Long,
            downloadedBytesPerSecond: Long
        ) {
            updateDownloadProgress(download.id, download.progress)
            Timber.d("onProgress called")
        }

        override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
            updateDownloadStarted(download.id)
            Timber.d("onQueued called")
        }

        override fun onDownloadBlockUpdated(
            download: Download,
            downloadBlock: DownloadBlock,
            totalBlocks: Int
        ) {
            triggerActiveDownloads()
            Timber.d("onDownloadBlockUpdated called")
        }

        override fun onRemoved(download: Download) {
            Timber.d("onRemoved called")
        }

        override fun onResumed(download: Download) {
            updateDownloadResumed(download.id)
            Timber.d("onResumed called")
        }

        override fun onStarted(
            download: Download,
            downloadBlocks: List<DownloadBlock>,
            totalBlocks: Int
        ) {
            if (download.total > FileUtils.getFreeSpaceInExternalFilesDir(context)){
               errorCallback?.invoke(DownloadCallBack.STARTED)
                fetch.cancel(download.id)
                fetch.remove(download.id)
            }else{
                updateDownloadStarted(download.id)
            }
            Timber.d("onStarted called")
        }

        override fun onWaitingNetwork(download: Download) {
            Timber.d("onWaitingForNetwork called")
        }
    }

    var isDownloadActive = false
        private set

    private val _isDownloadRunning = MutableLiveData<Boolean>()
    val isDownloadRunning: LiveData<Boolean>
        get() = _isDownloadRunning


    private fun init() {
        if (fetch.isClosed) fetch = Fetch.Impl.getInstance(fetchConfiguration)
        _isDownloadRunning.value = false
    }

    private fun triggerActiveDownloads() {
        fetch.getDownloads(Func { list ->
            val activeDownloadsList =
                list.filter { it.status == Status.QUEUED || it.status == Status.DOWNLOADING || it.status == Status.PAUSED }
            _isDownloadRunning.postValue(activeDownloadsList.isNotEmpty())
            updateIsDownloadActive(activeDownloadsList.isNotEmpty())
        })
    }

    fun killActiveDownloads() {
        fetch.getDownloads(Func { list ->
            list.forEach { download ->
                when (download.status) {
                    Status.QUEUED -> {
                        fetch.cancel(download.id)
                        fetch.remove(download.id)
                    }
                    Status.DOWNLOADING -> {
                        fetch.cancel(download.id)
                        fetch.remove(download.id)
                    }
                    Status.PAUSED -> {
                        fetch.cancel(download.id)
                        fetch.remove(download.id)
                    }
                }
            }

        })
    }

    fun registerListener() {
        init()
        Timber.d("registerListener called")
        fetch.addListener(listener)
    }

    fun unregisterListener() {
        Timber.d("unregisterListener called")
        fetch.removeListener(listener)
    }

    fun close() {
        fetch.close()
    }

    fun enqueueDownload(magazine: DomainMagazine,authToken : String? ) {
        Timber.d("Magazine: $magazine")
        val filePath = createFilePath(magazine.id)
        val request = Request(magazine.editionUrl, filePath).apply {
            networkType = NetworkType.ALL
            addHeader(AUTH_HEADER_KEY, authToken!!)
        }
        fetch.enqueue(
            request,
            Func {
                updateDownloadPending(magazine.id, request.id)
            },
            Func { error: Error? ->
                Timber.d("Error queueing download: error:${error}")
            }
        )
    }


    private fun updateDownloadPending(magazineId: Long, downloadId: Int) {
        repository.updateDownloadStateProgressId(magazineId, DownloadState.PENDING, downloadId, 0)
    }

    private fun updateDownloadStarted(dId: Int) {
        repository.updateDownloadStateByDid(dId, DownloadState.RUNNING)
    }

    private fun updateDownloadProgress(dId: Int, inputProgress: Int) {
        repository.updateDownloadProgressByDid(dId, inputProgress)
    }

    private fun updateDownloadPaused(dId: Int) {
        repository.updateDownloadStateByDid(dId, DownloadState.PAUSED)
    }

    private fun updateDownloadResumed(dId: Int) {
        repository.updateDownloadStateByDid(dId, DownloadState.RUNNING)
    }

    private fun updateDownloadCompleted(dId: Int, fileUri: String) {
        repository.updateDownloadStateIdUriByDid(dId, fileUri, NO_DOWNLOAD, DownloadState.COMPLETED)
        triggerActiveDownloads()
    }

    private fun updateDownloadFailed(dId: Int, fileUri: String) {
        repository.updateDownloadStateIdProgressByDid(
            dId,
            DownloadState.EMPTY,
            NO_DOWNLOAD,
            NO_PROGRESS
        )
        deleteFile(fileUri)
        triggerActiveDownloads()
        Timber.d("updateDownloadFailed: $dId")
    }


    fun cancelDownload(dId: Int) {
        Timber.d("cancelDownload called")
        fetch.cancel(dId)
        fetch.remove(dId)
    }

    private fun createFilePath(id: Long): String {

       return  context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.path + "/.Downloads_PDF/"  + id.toString() + PDF_TYPE
    }

    private fun deleteFile(uri: String): Boolean {
        if (uri == FileUtils.NO_FILE) return false
        val file = File(URI.create(uri))
        return if (file.exists()) {
            file.delete()
            true
        } else
            false
    }


    private fun updateIsDownloadActive(downloadState: Boolean) {
        Timber.d("updateIsDownloadActive: $downloadState")
        if (this.isDownloadActive != isDownloadActive) this.isDownloadActive = downloadState
    }

}