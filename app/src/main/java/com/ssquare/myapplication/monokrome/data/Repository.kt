package com.ssquare.myapplication.monokrome.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.ssquare.myapplication.monokrome.db.LocalCache
import com.ssquare.myapplication.monokrome.network.MagazineOrException
import com.ssquare.myapplication.monokrome.network.MonokromeApiService
import com.ssquare.myapplication.monokrome.network.loadFromServer
import com.ssquare.myapplication.monokrome.network.uploadIssue
import com.ssquare.myapplication.monokrome.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber


class Repository constructor(
    private val context: Context,
    private val scope: CoroutineScope,
    private val cache: LocalCache,
    private val network: MonokromeApiService
) {

    private val _networkError = MutableLiveData<Exception>()
    val networkError: LiveData<Exception>
        get() = _networkError

    companion object {
        private const val TAG = "Repository"

        private var INSTANCE: Repository? = null
        fun getInstance(
            context: Context,
            scope: CoroutineScope,
            cache: LocalCache,
            network: MonokromeApiService
        ): Repository {
            var instance = INSTANCE
            if (instance == null) {
                instance = Repository(context, scope, cache, network)
                INSTANCE = instance
            }
            return instance
        }
    }

    suspend fun loadAndCacheData(): Boolean {
        var resultState = false
        val authToken = getAuthToken(context)
        val result = network.loadFromServer(authToken)
        Timber.d("load from server: $result")
        withContext(Dispatchers.IO) {
            commitLoadDataActive(context, true)
            resultState =
                if (result.header != null && result.magazineList != null && result.exception == null) {
                    val databaseMagazines = result.magazineList.toMagazines(context)
                    cache.refresh(databaseMagazines, result.header)
                    true
                } else {
                    _networkError.postValue(
                        result.exception
                    )
                    false
                }
            commitLoadDataActive(context, false)
        }

        return resultState
    }

    fun getCachedData(orderBy: OrderBy): MagazineListLiveData {
        Timber.d("getCachedData() called")
        val header = Transformations.map(cache.getCachedHeader()) {
            it.toDomainHeader(context)
        }
        val magazines = Transformations.map(cache.getCachedMagazines(orderBy)) {
            it.toDomainMagazines(context)
        }
        return MagazineListLiveData(header, magazines)
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

    //Download

    private fun updateFileUri(id: Long, fileUri: String) {
        Log.d(TAG, "updateFileUri() called")
        scope.launch {
            withContext(Dispatchers.IO) {
                cache.updateFileUri(id, fileUri)
            }
        }
    }

    fun searchResult(search: String?) = Transformations.map(cache.searchResult(search)) {
        it.toDomainMagazines(context)
    }

    fun updateDownloadProgress(id: Long, progress: Int) {
        Log.d(TAG, "updateDownloadProgress() called")
        scope.launch {
            withContext(Dispatchers.IO) {
                cache.updateDownloadProgress(id, progress)
            }
        }
    }

    fun updateDownloadId(id: Long, downloadId: Int) {
        Log.d(TAG, "updateDownloadId() called")
        scope.launch {
            withContext(Dispatchers.IO) {
                cache.updateDownloadId(id, downloadId)
            }
        }
    }

    fun updateDownloadState(id: Long, downloadState: DownloadState) {
        Log.d(TAG, "updateDownloadState() called")
        scope.launch {
            withContext(Dispatchers.IO) {
                cache.updateDownloadState(id, downloadState.ordinal)
            }
        }
    }

    fun updateFileUriByDid(dId: Int, fileUri: String) {
        Log.d(TAG, "updateFileUriByDid() called")
        scope.launch {
            withContext(Dispatchers.IO) {
                cache.updateFileUriByDid(dId, fileUri)
            }
        }
    }

    fun updateDownloadProgressByDid(dId: Int, progress: Int) {
        Log.d(TAG, "updateDownloadProgressByDid() called")
        scope.launch {
            withContext(Dispatchers.IO) {
                cache.updateDownloadProgressByDid(dId, progress)
            }
        }
    }

    fun updateDownloadIdByDid(dId: Int, downloadId: Int) {
        Log.d(TAG, "updateDownloadIdByDid() called")
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


    suspend fun uploadIssue(
        title: String,
        description: String,
        imageUri: Uri,
        editionPath: String,
        releaseDate: Long
    ): MagazineOrException {
        val authToken = getAuthToken(context)
        val imageFile = FileUtils.getFileFromUri(context, imageUri)!!
        val imageMimeType = FileUtils.getTypeFromUri(context, imageUri)!!
        val imageRequestBody = RequestBody.create(MediaType.parse(imageMimeType), imageFile)
        val image = MultipartBody.Part.createFormData("image", imageFile.name, imageRequestBody)

        val editionFile = FileUtils.getFileFromPath(editionPath)!!
        val editionMimeType = FileUtils.getTypeFromPath(editionPath)!!
        val editionRequestBody = RequestBody.create(MediaType.parse(editionMimeType), editionFile)
        val edition =
            MultipartBody.Part.createFormData("edition", editionFile.name, editionRequestBody)

        val magazineOrException =
            network.uploadIssue(authToken, title, description, image, edition, releaseDate)
        Timber.d("Uploading issue: $magazineOrException")

        return magazineOrException
    }
}