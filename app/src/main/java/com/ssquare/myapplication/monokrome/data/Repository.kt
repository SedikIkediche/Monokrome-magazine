package com.ssquare.myapplication.monokrome.data

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.db.LocalCache
import com.ssquare.myapplication.monokrome.network.*
import com.ssquare.myapplication.monokrome.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import timber.log.Timber


class Repository constructor(
    private val context: Context,
    private val scope: CoroutineScope,
    private val cache: LocalCache,
    private val network: MonokromeApiService,
    private val client: OkHttpClient
) {

    private val _networkError = MutableLiveData<Error>()
    val networkError: LiveData<Error>
        get() = _networkError


    fun loadAndCacheData(): Boolean {
        var resultState = false
        scope.launch {
            val authToken = getAuthToken(context)
            val result = network.loadFromServer(authToken)
            Timber.d("load from server: $result")
            withContext(Dispatchers.IO) {
                commitLoadDataActive(context, true)
                resultState =
                    if (result.header != null && result.error == null) {
                        if (!result.magazineList.isNullOrEmpty()) {
                            val databaseMagazines = result.magazineList.toMagazines(context)
                            cache.refresh(databaseMagazines, result.header)
                            _networkError.postValue(null)
                            commitCacheData(context)
                            true
                        } else {
                            Timber.d("loadAndCacheData() called magazineList=${result.magazineList}, header = ${result.header}")
                            _networkError.postValue(
                                Error(
                                    message = context.getString(R.string.no_issues_available),
                                    code = 404
                                )
                            )
                            true
                        }
                    } else {
                        _networkError.postValue(
                            result.error
                        )
                        false
                    }
                commitLoadDataActive(context, false)
            }
        }
        Timber.d("value : $resultState")
        return resultState
    }

    fun getCachedData(orderBy: OrderBy): MagazineListLiveData {
        val header = Transformations.map(cache.getCachedHeader()) {
            it.toDomainHeader(context)
        }
        val magazines = Transformations.map(cache.getCachedMagazines(orderBy)) {
            it.toDomainMagazines(context)
        }
        return MagazineListLiveData(
            header,
            magazines, dataEmptyCallback = {
                Timber.d("isDataCached: ${isDataCached(context)}")
                if (!isDataCached(context)) {
                    Timber.d("dataEmptyCallback() called")
                    _networkError.postValue(
                        Error(
                            message = context.getString(R.string.no_issues_available),
                            code = 404
                        )
                    )
                }

            }, dataCallback = { _networkError.value = null }
        )
    }

    fun getMagazine(id: Long) = Transformations.map(cache.getMagazine(id)) {
        it.toDomainMagazine(context)
    }

    fun delete(magazine: DomainMagazine) {
        val fileDeleted = FileUtils.deleteFile(magazine.fileUri)
        if (fileDeleted) {
            updateFileUri(magazine.id, FileUtils.NO_FILE)
            updateDownloadProgress(magazine.id, NO_PROGRESS)
            updateDownloadId(magazine.id, NO_DOWNLOAD)
            updateDownloadState(magazine.id, DownloadState.EMPTY)
        }
    }


    suspend fun uploadIssue(
        title: String,
        description: String,
        imageUri: Uri,
        editionPath: String,
        releaseDate: Long
    ): MagazineOrError {
        val authToken = getAuthToken(context)
        val imageFile = FileUtils.getFileFromUri(context, imageUri)!!
        val imageMimeType = "image/jpeg"//FileUtils.getTypeFromUri(context, imageUri)!!
        val imageRequestBody = RequestBody.create(MediaType.parse(imageMimeType), imageFile)
        val image = MultipartBody.Part.createFormData("image", imageFile.name, imageRequestBody)

        val editionFile = FileUtils.getFileFromPath(editionPath)!!
        val editionMimeType = FileUtils.getTypeFromPath(editionPath)!!
        val editionRequestBody = RequestBody.create(MediaType.parse(editionMimeType), editionFile)

        commitUploadingActive(context, true)
        val edition =
            MultipartBody.Part.createFormData("edition", editionFile.name, editionRequestBody)

        val magazineOrError =
            network.uploadIssue(authToken, title, description, image, edition, releaseDate)
        Timber.tag("Upload").d("Uploading issue: $magazineOrError")

        commitUploadingActive(context, false)
        return magazineOrError
    }

    fun cancelNetworkOperations() {
        client.dispatcher().cancelAll()
    }

    //Download

    private fun updateFileUri(id: Long, fileUri: String) {
        Timber.d("updateFileUri() called")
        scope.launch {
            withContext(Dispatchers.IO) {
                cache.updateFileUri(id, fileUri)
            }
        }
    }

    fun searchResult(search: String?) = Transformations.map(cache.searchResult(search)) {
        it.toDomainMagazines(context)
    }

    private fun updateDownloadProgress(id: Long, progress: Int) {
        Timber.d("updateDownloadProgress() called")
        scope.launch {
            withContext(Dispatchers.IO) {
                cache.updateDownloadProgress(id, progress)
            }
        }
    }

    private fun updateDownloadId(id: Long, downloadId: Int) {
        Timber.d("updateDownloadId() called")
        scope.launch {
            withContext(Dispatchers.IO) {
                cache.updateDownloadId(id, downloadId)
            }
        }
    }

    private fun updateDownloadState(id: Long, downloadState: DownloadState) {
        Timber.d("updateDownloadState() called")
        scope.launch {
            withContext(Dispatchers.IO) {
                cache.updateDownloadState(id, downloadState.ordinal)
            }
        }
    }

    fun updateDownloadStateProgressId(
        id: Long,
        downloadState: DownloadState,
        downloadId: Int,
        progress: Int
    ) {
        Timber.d("updateDownloadStateProgressId() called")
        scope.launch {
            withContext(Dispatchers.IO) {
                cache.updateDownloadState(id, downloadState.ordinal)
                cache.updateDownloadId(id, downloadId)
                cache.updateDownloadProgress(id, progress)
            }
        }
    }

    fun updateDownloadStateIdUriByDid(
        dId: Int,
        fileUri: String,
        downloadId: Int,
        downloadState: DownloadState
    ) {
        Timber.d("updateDownloadStateIdUriByDid() called")
        scope.launch {
            withContext(Dispatchers.IO) {
                cache.updateFileUriByDid(dId, fileUri)
                cache.updateDownloadStateByDid(dId, downloadState.ordinal)
                cache.updateDownloadIdByDid(dId, downloadId)
            }
        }
    }

    fun updateFileUriByDid(dId: Int, fileUri: String) {
        Timber.d("updateFileUriByDid() called")
        scope.launch {
            withContext(Dispatchers.IO) {
                cache.updateFileUriByDid(dId, fileUri)
            }
        }
    }

    fun updateDownloadProgressByDid(dId: Int, progress: Int) {
        Timber.d("updateDownloadProgressByDid() called")
        scope.launch {
            withContext(Dispatchers.IO) {
                cache.updateDownloadProgressByDid(dId, progress)
            }
        }
    }

    fun updateDownloadIdByDid(dId: Int, downloadId: Int) {
        Timber.d("updateDownloadIdByDid() called")
        scope.launch {
            withContext(Dispatchers.IO) {
                cache.updateDownloadIdByDid(dId, downloadId)
            }
        }
    }

    fun updateDownloadStateByDid(dId: Int, downloadState: DownloadState) {
        scope.launch {
            withContext(Dispatchers.IO) {
                val updated = cache.updateDownloadStateByDid(dId, downloadState.ordinal)
                Timber.d("updateDownloadStateByDid() called: updated = $updated ************")
            }
        }
    }

    fun updateDownloadStateIdProgressByDid(
        dId: Int,
        downloadState: DownloadState,
        downloadId: Int,
        progress: Int
    ) {
        scope.launch {
            withContext(Dispatchers.IO) {
                val updated = cache.updateDownloadStateByDid(dId, downloadState.ordinal)
                cache.updateDownloadProgressByDid(dId, progress)
                cache.updateDownloadIdByDid(dId, downloadId)
                Timber.d("updateDownloadStateIdProgressByDid() called: updated = $updated ************")
            }
        }
    }

}