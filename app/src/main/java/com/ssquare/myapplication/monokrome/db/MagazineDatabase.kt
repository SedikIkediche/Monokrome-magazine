package com.ssquare.myapplication.monokrome.db

import android.app.DownloadManager
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ssquare.myapplication.monokrome.data.DownloadState
import com.ssquare.myapplication.monokrome.data.Header
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                    "database8"
                )
                    .addCallback(CleanDownloadsCallback(context))
                    .build()
                INSTANCE = instance
            }
            return instance
        }
    }

    internal class CleanDownloadsCallback(val context: Context) : RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)

            val dao = getInstance(context).magazineDao
            CoroutineScope(Dispatchers.IO).launch {
                dao.getRunningDownloads().forEach {

                    val downloadManager =
                        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val cursor =
                        downloadManager.query(DownloadManager.Query().setFilterById(it.downloadId.toLong()))
                    val fileUri = DOWNLOAD_DIRECTORY_URI + it.id + PDF_TYPE
                    if (!cursor.moveToFirst()) {
                        dao.updateDownloadId(it.id, NO_DOWNLOAD)
                        dao.updateDownloadState(it.id, DownloadState.EMPTY.ordinal)
                        dao.updateProgress(it.id, NO_PROGRESS)
                        deleteFile(fileUri)
                    } else {
                        val status =
                            cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))

                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            dao.updateDownloadId(it.id, NO_DOWNLOAD)
                            dao.updateDownloadState(it.id, DownloadState.COMPLETED.ordinal)
                            dao.updateUri(it.id, fileUri)
                        }
                    }
                }
            }
        }
    }

}