package com.ssquare.myapplication.monokrome.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ssquare.myapplication.monokrome.db.LocalCache
import com.ssquare.myapplication.monokrome.network.FirebaseServer
import com.ssquare.myapplication.monokrome.network.NetworkResponse


class Repository private constructor(
    private val cache: LocalCache,
    private val network: FirebaseServer
) {
    private val _networkError = MutableLiveData<String>()
    val networkError: LiveData<String>
        get() = _networkError

    companion object {
        var INSTANCE: Repository? = null
        fun getInstance(
            cache: LocalCache,
            network: FirebaseServer
        ): Repository {
            var instance = INSTANCE
            if (instance == null) {
                instance = Repository(cache, network)
                INSTANCE = instance
            }
            return instance
        }
    }

    fun networkResponse(): NetworkResponse = network.loadData()

    fun getMagazineList(): MagazineListLiveData {
        //if(dataCashed == false)    loadAnRefreshData
        return cache.getMagazines()
    }

    fun getMagazine(id: Int) = cache.getMagazine(id)

    fun cacheData(header: Header, magazines: List<Magazine>, block: () -> Unit) =
        cache.refresh(magazines, header, block)
}