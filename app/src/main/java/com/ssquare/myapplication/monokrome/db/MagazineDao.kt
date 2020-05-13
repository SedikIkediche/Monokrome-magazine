package com.ssquare.myapplication.monokrome.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ssquare.myapplication.monokrome.data.Magazine

@Dao
interface MagazineDao {

    @Query("SELECT * FROM magazines ORDER BY releaseDate ASC")
    fun getAll(): LiveData<List<Magazine>>

    @Query("SELECT * FROM magazines WHERE id=:id ")
    fun get(id: Long): LiveData<Magazine>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(magazines: List<Magazine>)

    @Query("DELETE FROM magazines")
    suspend fun clear()

    @Query("SELECT * From magazines WHERE title LIKE :search")
    fun searchResult(search : String) : LiveData<List<Magazine>>

    @Query("UPDATE magazines SET fileUri=:path WHERE id=:id")
    suspend fun updateUri(id: Long, path: String)

    @Query("UPDATE magazines SET downloadProgress=:progress WHERE id=:id")
    suspend fun updateProgress(id: Long, progress: Int)

    @Query("UPDATE magazines SET downloadId=:downloadId WHERE id=:id")
    suspend fun updateDownloadId(id: Long, downloadId: Int)

    @Query("UPDATE magazines SET downloadState=:downloadState WHERE id=:id")
    suspend fun updateDownloadState(id: Long, downloadState: Int)


    //downloadUtils
    @Query("UPDATE magazines SET fileUri=:path WHERE downloadId=:dId")
    suspend fun updateUriByDid(dId: Int, path: String)

    @Query("UPDATE magazines SET downloadProgress=:progress WHERE downloadId=:dId")
    suspend fun updateProgressByDid(dId: Int, progress: Int)

    @Query("UPDATE magazines SET downloadId=:downloadId WHERE downloadId=:dId")
    suspend fun updateDownloadIdByDid(dId: Int, downloadId: Int)

    @Query("UPDATE magazines SET downloadState=:downloadState WHERE downloadId=:dId")
    suspend fun updateDownloadStateByDid(dId: Int, downloadState: Int)




}