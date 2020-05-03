package com.ssquare.myapplication.monokrome.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ssquare.myapplication.monokrome.data.Magazine

@Dao
interface MagazineDao {

    @Query("SELECT * FROM magazines ORDER BY releaseDate ASC")
    fun getAll(): LiveData<List<Magazine>>

    @Query("SELECT * FROM magazines WHERE id=:id ")
    fun get(id: Int): LiveData<Magazine>

    @Query("SELECT * FROM magazines WHERE downloadId != -1")
    suspend fun getRunningDownloads(): List<Magazine>

    @Query("UPDATE magazines SET fileUri=:path WHERE id=:id")
    suspend fun updateUri(id: Long, path: String)

    @Query("UPDATE magazines SET downloadProgress=:progress WHERE id=:id")
    suspend fun updateProgress(id: Long, progress: Int)

    @Query("UPDATE magazines SET downloadId=:downloadId WHERE id=:id")
    suspend fun updateDownloadId(id: Long, downloadId: Int)

    @Query("UPDATE magazines SET downloadProgress= -1")
    suspend fun invalidateProgress()



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(magazines: List<Magazine>)

    @Query("DELETE FROM magazines")
    suspend fun clear()
}