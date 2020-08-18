package com.ssquare.myapplication.monokrome

import android.app.Application
import androidx.work.*
import com.ssquare.myapplication.monokrome.work.RefreshDataWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.util.concurrent.TimeUnit


@HiltAndroidApp
class MagazineApplication : Application() {
    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        Timber.plant(DebugTree())
        //initPeriodicCache()
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


}