package com.ssquare.myapplication.monokrome.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks
import com.ssquare.myapplication.monokrome.db.LocalCache
import com.ssquare.myapplication.monokrome.network.FirebaseServer
import com.ssquare.myapplication.monokrome.network.NetworkMagazine
import com.ssquare.myapplication.monokrome.util.DOWNLOAD_DIRECTORY_URI
import com.ssquare.myapplication.monokrome.util.NO_FILE
import com.ssquare.myapplication.monokrome.util.PDF_TYPE
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
                cache.updatePath(id, fileUri)
            }
        }
    }

    fun loadAndCacheData(): Boolean {
        var resultState = false
        scope.launch {
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