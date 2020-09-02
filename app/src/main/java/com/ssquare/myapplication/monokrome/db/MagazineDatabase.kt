package com.ssquare.myapplication.monokrome.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ssquare.myapplication.monokrome.data.Header
import com.ssquare.myapplication.monokrome.data.Magazine

@Database(entities = [Header::class, Magazine::class], version = 1, exportSchema = false)
abstract class MagazineDatabase : RoomDatabase() {

    abstract val headerDao: HeaderDao
    abstract val magazineDao: MagazineDao

    companion object {
        const val DATABASE_NAME = "database"

    }
}