package com.ssquare.myapplication.monokrome.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ssquare.myapplication.monokrome.data.Header
import com.ssquare.myapplication.monokrome.data.Magazine

@Database(entities = [Header::class, Magazine::class], version = 1, exportSchema = false)
abstract class MagazineDatabase : RoomDatabase() {

    abstract val headerDao: HeaderDao
    abstract val magazineDao: MagazineDao

    companion object {

        var INSTANCE: MagazineDatabase? = null

        fun getInstance(context: Context): MagazineDatabase {
            var instance = INSTANCE
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    MagazineDatabase::class.java,
                    "database6"
                ).build()
                INSTANCE = instance
            }
            return instance
        }
    }
}