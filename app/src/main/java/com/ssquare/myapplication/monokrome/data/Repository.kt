package com.ssquare.myapplication.monokrome.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ssquare.myapplication.monokrome.db.LocalCache
import com.ssquare.myapplication.monokrome.network.MonokromeApiService
import com.ssquare.myapplication.monokrome.network.NetworkMagazine
import com.ssquare.myapplication.monokrome.network.loadFromServer
import com.ssquare.myapplication.monokrome.util.DownloadState
import com.ssquare.myapplication.monokrome.util.OrderBy
import com.ssquare.myapplication.monokrome.util.commitLoadDataActive
import com.ssquare.myapplication.monokrome.util.createUriString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI


class Repository  constructor(
    private val context: Context,
    private val scope: CoroutineScope,
    private val cache: LocalCache,
    private val network: MonokromeApiService
) {

    private val _networkError = MutableLiveData<Exception>()
    val networkError: LiveData<Exception>
        get() = _networkError

    companion object {
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


    fun getCachedData(orderBy: OrderBy): MagazineListLiveData = cache.getCachedData(orderBy)

    fun getMagazine(id: Long) = cache.getMagazine(id)

    //Download

    fun updateFileUri(id: Long, fileUri: String) {
        scope.launch {
            withContext(Dispatchers.IO) {
                cache.updateFileUri(id, fileUri)
            }
        }
    }

    fun searchResult(search: String?) = cache.searchResult(search)

    fun updateDownloadProgress(id: Long, progress: Int) {
        scope.launch {
            withContext(Dispatchers.IO) {
                cache.updateDownloadProgress(id, progress)
            }
        }
    }

    fun updateDownloadId(id: Long, downloadId: Int) {
        scope.launch {
            withContext(Dispatchers.IO) {
                cache.updateDownloadId(id, downloadId)
            }
        }
    }

    fun updateDownloadState(id: Long, downloadState: DownloadState) {
        scope.launch {
            withContext(Dispatchers.IO) {
                cache.updateDownloadState(id, downloadState.ordinal)
            }
        }
    }

    fun updateFileUriByDid(dId: Int, fileUri: String) {
        scope.launch {
            withContext(Dispatchers.IO) {
                cache.updateFileUriByDid(dId, fileUri)
            }
        }
    }

    fun updateDownloadProgressByDid(dId: Int, progress: Int) {
        scope.launch {
            withContext(Dispatchers.IO) {
                cache.updateDownloadProgressByDid(dId, progress)
            }
        }
    }

    fun updateDownloadIdByDid(dId: Int, downloadId: Int) {
        scope.launch {
            withContext(Dispatchers.IO) {
                cache.updateDownloadIdByDid(dId, downloadId)
            }
        }
    }

    fun updateDownloadStateByDid(dId: Int, downloadState: DownloadState) {
        scope.launch {
            withContext(Dispatchers.IO) {
                cache.updateDownloadStateByDid(dId, downloadState.ordinal)
            }
        }
    }

    suspend fun loadAndCacheData(): Boolean {
        var resultState = false
        val result = network.loadFromServer()
        withContext(Dispatchers.IO) {
            commitLoadDataActive(context, true)
            resultState =
                if (result.header != null && result.magazineList != null && result.exception == null) {
                    val databaseMagazines = result.magazineList.toDatabaseMagazines()
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

    private fun List<NetworkMagazine>.toDatabaseMagazines(): List<Magazine> {
        return this.map {
            it.toMagazine()
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

}