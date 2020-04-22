package com.ssquare.myapplication.monokrome.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ssquare.myapplication.monokrome.main.data.Magazine

@Dao
interface MagazineDao {

    @Query("SELECT * FROM magazines")
    fun getAll(): LiveData<List<Magazine>>

    @Query("SELECT * FROM magazines WHERE id=:id ")
    fun get(id: Int): LiveData<Magazine>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg magazine: Magazine)
}