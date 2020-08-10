package com.ssquare.myapplication.monokrome.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.ssquare.myapplication.monokrome.db.LocalCache
import com.ssquare.myapplication.monokrome.network.MonokromeApiService
import com.ssquare.myapplication.monokrome.network.NetworkMagazine
import com.ssquare.myapplication.monokrome.network.loadFromServer
import com.ssquare.myapplication.monokrome.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI


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


    fun getCachedData(orderBy: OrderBy): MagazineListLiveData {
        Log.d(TAG, "getCachedData() called")
        val header = Transformations.map(cache.getCachedHeader()) {
            it.toDomainHeader()
        }
        val magazines = Transformations.map(cache.getCachedMagazines(orderBy)) {
            it.toDomainMagazines()
        }
        return MagazineListLiveData(header, magazines)
    }

    fun getMagazine(id: Long) = Transformations.map(cache.getMagazine(id)) {
        it.toDomainMagazine()
    }

    //Download

    fun updateFileUri(id: Long, fileUri: String) {
        Log.d(TAG, "updateFileUri() called")
        scope.launch {
            withContext(Dispatchers.IO) {
                cache.updateFileUri(id, fileUri)
            }
        }
    }

    fun searchResult(search: String?) = Transformations.map(cache.searchResult(search)) {
        it.toDomainMagazines()
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
                Log.d(TAG, "updateDownloadStateByDid() called: updated = $updated ************")
            }
        }
    }

    suspend fun loadAndCacheData(): Boolean {
        var resultState = false
        val authToken = getAuthToken(context)
        val result = network.loadFromServer(authToken)
        withContext(Dispatchers.IO) {
            commitLoadDataActive(context, true)
            resultState =
                if (result.header != null && result.magazineList != null && result.exception == null) {
                    val databaseMagazines = result.magazineList.toMagazines()
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

    //map entities

    private fun List<NetworkMagazine>.toMagazines(): List<Magazine> {
        return this.map {
            it.toMagazine()
        }
    }

    private fun List<Magazine>.toDomainMagazines(): List<DomainMagazine> {
        return this.map {
            it.toDomainMagazine()
        }
    }

    private fun NetworkMagazine.toMagazine(): Magazine {
        val uri = createUriString(context, id)
        return if (File(URI.create(uri)).exists()) {
            Magazine(
                this.id,
                this.title,
                this.description,
                this.releaseDate,
                this.imageUrl,
                this.editionUrl,
                uri,
                100,
                downloadState = DownloadState.COMPLETED.ordinal
            )
        } else {
            Magazine(
                this.id,
                this.title,
                this.description,
                this.releaseDate,
                this.imageUrl,
                this.editionUrl
            )
        }
    }

    private fun Magazine.toDomainMagazine(): DomainMagazine {
        return run {
            val authToken = getAuthToken(context) ?: ""
            val header = LazyHeaders.Builder().addHeader(AUTH_HEADER_KEY, authToken).build()
            val glideUrl = GlideUrl(this.imageUrl, header)

            DomainMagazine(
                id = this.id,
                title = this.title,
                description = this.description,
                releaseDate = this.releaseDate,
                imageUrl = glideUrl,
                editionUrl = this.editionUrl,
                fileUri = this.fileUri,
                downloadProgress = this.downloadProgress,
                downloadId = this.downloadId,
                downloadState = this.downloadState
            )
        }
    }

    private fun DomainMagazine.toMagazine(): Magazine {
        val imageUrl = this.imageUrl?.toStringUrl() ?: ""

        return Magazine(
            this.id,
            this.title,
            this.description,
            this.releaseDate,
            imageUrl,
            this.editionUrl,
            this.fileUri,
            this.downloadProgress,
            this.downloadState
        )
    }

    private fun DomainHeader.toHeader(): Header {
        val imageUrl = this.imageUrl?.toStringUrl() ?: ""
        return Header(this.id, imageUrl)
    }

    private fun Header?.toDomainHeader(): DomainHeader? {

        return if (this != null) {
            val authToken = getAuthToken(context) ?: ""
            val header = LazyHeaders.Builder().addHeader(AUTH_HEADER_KEY, authToken).build()
            val glideUrl = GlideUrl(this.imageUrl, header)
            DomainHeader(this.id, glideUrl)
        } else null
    }


}