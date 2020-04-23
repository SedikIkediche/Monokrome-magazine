package com.ssquare.myapplication.monokrome.main.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ssquare.myapplication.monokrome.main.data.Header

@Dao
interface HeaderDao {

    @Query("SELECT * FROM header ORDER BY id  LIMIT 1")
    fun get(): LiveData<Header>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(header: Header)

    @Query("DELETE FROM header")
    suspend fun clear()
}