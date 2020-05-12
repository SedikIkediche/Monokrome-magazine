package com.ssquare.myapplication.monokrome

import android.app.Application
import androidx.work.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.ssquare.myapplication.monokrome.data.Repository
import com.ssquare.myapplication.monokrome.db.LocalCache
import com.ssquare.myapplication.monokrome.db.MagazineDatabase
import com.ssquare.myapplication.monokrome.network.FirebaseServer
import com.ssquare.myapplication.monokrome.util.DownloadUtils
import com.ssquare.myapplication.monokrome.work.RefreshDataWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MagazineApplication : Application() {
    private val applicationScope = CoroutineScope(Dispatchers.Default)
    val downloadUtils: DownloadUtils by lazy {
        initDownloadUtils()
    }
    override fun onCreate() {
        super.onCreate()

        initDownloadUtils()
        initPeriodicCache()
    }

    private fun initPeriodicCache() {
        applicationScope.launch {
            setupRecurringWork()
        }
    }

    private fun setupRecurringWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED).build()

        val cacheWorkRequest = PeriodicWorkRequestBuilder<RefreshDataWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.HOURS)
            .setInitialDelay(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(applicationContext)
            .enqueueUniquePeriodicWork(
                RefreshDataWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                cacheWorkRequest
            )
    }

    private fun initDownloadUtils(): DownloadUtils {
        val database = FirebaseDatabase.getInstance()
        val storage = FirebaseStorage.getInstance()
        val network = FirebaseServer(database, storage)
        val magazineDao = MagazineDatabase.getInstance(applicationContext).magazineDao
        val headerDao = MagazineDatabase.getInstance(applicationContext).headerDao
        val cache = LocalCache(magazineDao, headerDao)
        val repository = Repository.getInstance(
            applicationContext, CoroutineScope(Dispatchers.Main), cache, network
        )
        return DownloadUtils.getInstance(applicationContext, repository)
    }

}