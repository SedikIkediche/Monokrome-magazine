package com.ssquare.myapplication.monokrome.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks
import com.ssquare.myapplication.monokrome.db.LocalCache
import com.ssquare.myapplication.monokrome.network.FirebaseServer
import com.ssquare.myapplication.monokrome.network.NetworkMagazine
import com.ssquare.myapplication.monokrome.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI


class Repository private constructor(
    private val scope: CoroutineScope,
    private val cache: LocalCache,
    private val network: FirebaseServer
) {
    private val _networkError = MutableLiveData<Exception>()
    val networkError: LiveData<Exception>
        get() = _networkError

    companion object {
        var INSTANCE: Repository? = null
        fun getInstance(
            scope: CoroutineScope,
            cache: LocalCache,
            network: FirebaseServer
        ): Repository {
            var instance = INSTANCE
            if (instance == null) {
                instance = Repository(scope, cache, network)
                INSTANCE = instance
            }
            return instance
        }
    }


    fun getCachedData(): MagazineListLiveData = cache.getCachedData()

    fun getMagazine(id: Int) = cache.getMagazine(id)

    fun updateFileUri(id: Long, fileUri: String) {
        scope.launch {
            withContext(Dispatchers.IO) {
                cache.updateFileUri(id, fileUri)
            }
        }
    }

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


    //for DownloadUtils
    fun updateFileUriByDid(dId: Int, fileUri: String) {
        scope.launch {
            withContext(Dispatchers.IO) {
                cache.updateFileUriByDid(dId, fileUri)
            }
        }
    }

    fun getMagazineByDid(dId: Int) {

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
    //for DownloadUtils


    fun cancelDownload(magazine: Magazine) {
        scope.launch {
            val fileUri = DOWNLOAD_DIRECTORY_URI + magazine.id + PDF_TYPE
            deleteFile(fileUri)
            cache.run {
                this.updateDownloadProgress(magazine.id, -1)
                this.updateFileUri(magazine.id, NO_FILE)
                this.updateDownloadId(magazine.id, NO_DOWNLOAD)
            }
        }
    }


    suspend fun loadAndCacheData(): Boolean {
        var resultState = false
            val task = network.loadFromServer()
            withContext(Dispatchers.IO) {
                val result = Tasks.await(task)
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
            }
        return resultState
    }

    private fun List<NetworkMagazine>.toDatabaseMagazines(): List<Magazine> {
        return this.map {
            Magazine(
                it.id,
                it.title, it.description, it.releaseDate, it.imageUrl, it.editionUrl,
                getFilePath(it.id)
            )
        }

    }

    private fun getFilePath(id: Long): String {
        val path = DOWNLOAD_DIRECTORY_URI + id + PDF_TYPE
        return if (File(URI.create(path)).exists()) path else NO_FILE
    }
}