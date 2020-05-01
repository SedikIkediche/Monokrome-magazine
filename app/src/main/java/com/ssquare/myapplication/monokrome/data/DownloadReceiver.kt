package com.ssquare.myapplication.monokrome.data

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.ssquare.myapplication.monokrome.db.LocalCache
import com.ssquare.myapplication.monokrome.db.MagazineDatabase
import com.ssquare.myapplication.monokrome.network.FirebaseServer
import com.ssquare.myapplication.monokrome.ui.main.MainActivity
import com.ssquare.myapplication.monokrome.util.deleteFile
import com.ssquare.myapplication.monokrome.util.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class DownloadReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val repository = initDependencies(context)
        when (intent.action) {
            DownloadManager.ACTION_DOWNLOAD_COMPLETE -> {
                val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                val result = getDownloadStatus(context, downloadId)
                val downloadStatus = result.first
                if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
                    toast(context, "Download Completed")
                    updateDatabase(result, repository)
                } else {
                    deleteFile(result.third)
                    Log.d("DownloadReceiver", "download canceled")
                    toast(context, "download canceled")
                }
            }
            DownloadManager.ACTION_NOTIFICATION_CLICKED -> {
                Log.d("DownloadReceiver", "notification clicked")
                val activityIntent = Intent(context, MainActivity::class.java).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.applicationContext.startActivity(activityIntent)
            }
        }

    }

    private fun updateDatabase(
        result: Triple<Int, Long, String>,
        repository: Repository
    ) {
        val magazineId = result.second
        val magazineFileUri = result.third
        repository.updateFileUri(magazineId, magazineFileUri)
    }


}

private fun getDownloadStatus(context: Context, downloadId: Long): Triple<Int, Long, String> {
    val query = DownloadManager.Query().apply { setFilterById(downloadId) }
    val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
    val cursor = downloadManager.query(query)
    if (cursor.moveToFirst()) {
        val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
        val magazineId =
            cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION)).toLong()
        val fileUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
        return Triple(status, magazineId, fileUri)
    }
    return Triple(DownloadManager.ERROR_UNKNOWN, -1, "")
}

private fun initDependencies(context: Context): Repository {
    DownloadManager.ACTION_VIEW_DOWNLOADS
    val database = FirebaseDatabase.getInstance()
    val storage = FirebaseStorage.getInstance()
    val network = FirebaseServer(database, storage)
    val magazineDao = MagazineDatabase.getInstance(context).magazineDao
    val headerDao = MagazineDatabase.getInstance(context).headerDao
    val cache = LocalCache(magazineDao, headerDao)
    return Repository.getInstance(CoroutineScope(Dispatchers.Main), cache, network)
}

