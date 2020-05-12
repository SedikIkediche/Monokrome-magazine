package com.ssquare.myapplication.monokrome.db

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
        magazineDao.insertAll(magazines)
    }

    fun getMagazine(id: Long) = magazineDao.get(id)

    fun getCachedData(): MagazineListLiveData {
        val header = headerDao.get()
        val magazines = magazineDao.getAll()
        return MagazineListLiveData(header, magazines)
    }

    suspend fun updateFileUri(id: Long, path: String) = magazineDao.updateUri(id, path)

    suspend fun updateDownloadProgress(id: Long, progress: Int) =
        magazineDao.updateProgress(id, progress)

    suspend fun updateDownloadId(id: Long, downloadId: Int) =
        magazineDao.updateDownloadId(id, downloadId)

    suspend fun updateDownloadState(id: Long, downloadState: Int) =
        magazineDao.updateDownloadState(id, downloadState)


    suspend fun updateFileUriByDid(dId: Int, path: String) = magazineDao.updateUriByDid(dId, path)

    suspend fun updateDownloadProgressByDid(dId: Int, progress: Int) =
        magazineDao.updateProgressByDid(dId, progress)

    suspend fun updateDownloadIdByDid(dId: Int, downloadId: Int) =
        magazineDao.updateDownloadIdByDid(dId, downloadId)

    suspend fun updateDownloadStateByDid(dId: Int, downloadState: Int) =
        magazineDao.updateDownloadStateByDid(dId, downloadState)

}



