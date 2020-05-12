package com.ssquare.myapplication.monokrome.db

import android.util.Log
import com.ssquare.myapplication.monokrome.data.Header
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.data.MagazineListLiveData


class LocalCache(
    private val magazineDao: MagazineDao,
    private val headerDao: HeaderDao
) {


    suspend fun refresh(magazines: List<Magazine>, header: Header) {
        refreshMagazines(magazines)
        refreshHeader(header)
    }


    private suspend fun refreshHeader(header: Header) {
        headerDao.clear()
        headerDao.insert(header)
    }

    private suspend fun refreshMagazines(magazines: List<Magazine>) {
        magazineDao.clear()
        Log.d("LocalCache", "dataSize = ${magazineDao.insertAll(magazines)}")


    }

    fun getMagazine(id: Int) = magazineDao.get(id)

    fun searchResult(search : String) = magazineDao.searchResult(search)

    fun getCachedData(): MagazineListLiveData {
        val header = headerDao.get()
        val magazines = magazineDao.getAll()
        return MagazineListLiveData(header, magazines)
    }

}

