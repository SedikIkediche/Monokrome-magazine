package com.ssquare.myapplication.monokrome.db

import android.database.DatabaseUtils
import androidx.lifecycle.LiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.ssquare.myapplication.monokrome.data.Header
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.util.OrderBy
import com.ssquare.myapplication.monokrome.util.OrderBy.*
import javax.inject.Inject


class LocalCache @Inject constructor(
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

    fun searchResult(search: String?) = magazineDao.searchResult(getQueryByInput(search))

    private fun getAllByOrder(orderBy: OrderBy) =
        magazineDao.getAllByOrder(getQueryByOrder(orderBy))

    fun getCachedMagazines(orderBy: OrderBy): LiveData<List<Magazine>> = getAllByOrder(orderBy)

    fun getCachedHeader(): LiveData<Header> = headerDao.get()


    private fun getQueryByInput(input: String?): SimpleSQLiteQuery {
        return if (input.isNullOrBlank()) {
            SimpleSQLiteQuery("SELECT * From magazines ORDER BY releaseDate DESC")
        } else {
            val escapedInput = DatabaseUtils.sqlEscapeString(input)
            val search = escapedInput.substring(1 until escapedInput.length - 1)
            SimpleSQLiteQuery("SELECT * From magazines WHERE title LIKE '%$search%' ORDER BY releaseDate DESC")
        }
    }

    private fun getQueryByOrder(orderBy: OrderBy): SimpleSQLiteQuery {
        return when (orderBy) {
            MOST_RECENT -> SimpleSQLiteQuery("SELECT * FROM magazines ORDER BY releaseDate DESC")
            A_TO_Z -> SimpleSQLiteQuery("SELECT * FROM magazines ORDER BY title ASC")
            Z_TO_A -> SimpleSQLiteQuery("SELECT * FROM magazines ORDER BY title DESC")
        }
    }


    //for download
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

