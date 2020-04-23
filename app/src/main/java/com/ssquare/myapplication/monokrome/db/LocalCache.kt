package com.ssquare.myapplication.monokrome.db

import androidx.lifecycle.LiveData
import com.ssquare.myapplication.monokrome.data.Header
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.data.MagazineListLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

typealias MagazineListLiveData = LiveData<Pair<Header, List<Magazine>>>

class LocalCache(
    private val magazineDao: MagazineDao,
    private val headerDao: HeaderDao,
    private val scope: CoroutineScope
) {


    fun refresh(magazines: List<Magazine>, header: Header, insertFinished: () -> Unit) {
        scope.launch {
            withContext(Dispatchers.IO) {
                refreshMagazines(magazines)
                refreshHeader(header)
            }
            insertFinished()
        }
    }

    private suspend fun refreshHeader(header: Header) {
        headerDao.clear()
        headerDao.insert(header)
    }

    private suspend fun refreshMagazines(magazines: List<Magazine>) {
        magazineDao.clear()
        magazineDao.insertAll(magazines)
    }

    fun getMagazine(id: Int) = magazineDao.get(id)

    fun getMagazines(): MagazineListLiveData {
        val header = headerDao.get()
        val magazines = magazineDao.getAll()
        return MagazineListLiveData(header, magazines)
    }
}