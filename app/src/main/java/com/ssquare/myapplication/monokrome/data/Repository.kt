package com.ssquare.myapplication.monokrome.data

import com.ssquare.myapplication.monokrome.db.LocalCache
import com.ssquare.myapplication.monokrome.network.FirebaseServer
import com.ssquare.myapplication.monokrome.network.NetworkResponse


class Repository private constructor(
    private val cache: LocalCache,
    private val network: FirebaseServer
) {

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

    fun getCacheData(): MagazineListLiveData = cache.getMagazines()

    fun getMagazine(id: Int) = cache.getMagazine(id)

    fun cacheData(header: Header, magazines: List<Magazine>) =
        cache.refresh(magazines, header)
}