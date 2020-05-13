package com.ssquare.myapplication.monokrome.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ssquare.myapplication.monokrome.data.Magazine

@Dao
interface MagazineDao {

    @Query("SELECT * FROM magazines ORDER BY releaseDate ASC")
    fun getAll(): LiveData<List<Magazine>>

    @Query("SELECT * FROM magazines WHERE id=:id ")
    fun get(id: Int): LiveData<Magazine>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(magazines: List<Magazine>)

    @Query("DELETE FROM magazines")
    suspend fun clear()

    @Query("SELECT * From magazines WHERE title LIKE :search")
    fun searchResult(search : String) : LiveData<List<Magazine>>
}