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

    @Query("UPDATE magazines SET fileUri=:path WHERE id=:id")
    suspend fun updatePath(id: Long, path: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(magazines: List<Magazine>)

    @Query("DELETE FROM magazines")
    suspend fun clear()
}