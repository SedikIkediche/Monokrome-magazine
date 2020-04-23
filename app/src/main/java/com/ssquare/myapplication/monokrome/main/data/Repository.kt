package com.ssquare.myapplication.monokrome.main.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.ssquare.myapplication.monokrome.main.db.LocalCache
import com.ssquare.myapplication.monokrome.main.network.FirebaseServer
import timber.log.Timber


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

    fun loadAndRefreshData() {
        val dataOrException = network.loadData()
        Transformations.map(dataOrException) {
            if (it.header != null || it.magazineList != null) {
                cache.insert(it.magazineList!!, it.header!!, insertFinished = {
                    Timber.d("inserted cash data")
                })
            }
        }
    }

    fun getMagazineList(): MagazineListLiveData {
        //if(dataCashed == false)    loadAnRefreshData
        loadAndRefreshData()
        return cache.getMagazines()
    }

    fun getMagazine(id: Int) = cache.getMagazine(id)


}