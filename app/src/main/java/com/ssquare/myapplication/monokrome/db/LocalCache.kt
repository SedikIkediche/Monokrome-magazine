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

    fun getCachedData(): MagazineListLiveData {
        val header = headerDao.get()
        val magazines = magazineDao.getAll()
        return MagazineListLiveData(header, magazines)
    }

    suspend fun updateFileUri(id: Long, path: String) = magazineDao.updateUri(id, path)

    suspend fun updateDownloadProgress(id: Long, progress: Int) =
        magazineDao.updateProgress(id, progress)

    suspend fun updateDownloadId(id: Long, downloadId: Long) =
        magazineDao.updateDownloadId(id, downloadId)

    suspend fun invalidateProgress() = magazineDao.invalidateProgress()

    suspend fun getRunningDownloads() = magazineDao.getRunningDownloads()

    suspend fun updateDownloadState(id: Long, downloadState: Int) =
        magazineDao.updateDownloadState(id, downloadState)

    suspend fun getMagazineDownloadId(id: Long) = magazineDao.getMagazineDownloadId(id)
}

