package com.ssquare.myapplication.monokrome.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks
import com.ssquare.myapplication.monokrome.db.LocalCache
import com.ssquare.myapplication.monokrome.network.FirebaseServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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


    fun loadAndCacheData(): Boolean {
        var resultState = false
        scope.launch {
            val task = network.loadFromServer()
            withContext(Dispatchers.IO) {
                val result = Tasks.await(task)
                resultState =
                    if (result.header != null && result.magazineList != null && result.exception == null) {
                        cache.refresh(result.magazineList, result.header)
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

}