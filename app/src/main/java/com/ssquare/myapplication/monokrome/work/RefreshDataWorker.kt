package com.ssquare.myapplication.monokrome.work

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ssquare.myapplication.monokrome.data.Repository
import com.ssquare.myapplication.monokrome.db.LocalCache
import com.ssquare.myapplication.monokrome.db.MagazineDatabase
import com.ssquare.myapplication.monokrome.network.MonokromeApi
import com.ssquare.myapplication.monokrome.util.isDownloadActive
import com.ssquare.myapplication.monokrome.util.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

class RefreshDataWorker(private val appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {


    companion object {
        const val WORK_NAME = "RefreshDataWorker"
    }


    @SuppressLint("RestrictedApi")
    override suspend fun doWork(): Result {
        Timber.d("doWork called")
        val repository = initRepository()


        return if (!isDownloadActive(appContext)) {
            val loadState = repository.loadAndCacheData()
            if (loadState) {
                Result.Success()
            } else {
                Result.Retry()
            }
        } else {
            toast(appContext, "Downloading Magazine From Server!")
            Result.Retry()
        }
    }

    private fun initRepository(): Repository {
        val network = MonokromeApi.retrofitService
        val magazineDao = MagazineDatabase.getInstance(appContext).magazineDao
        val headerDao = MagazineDatabase.getInstance(appContext).headerDao
        val cache = LocalCache(magazineDao, headerDao)
        return Repository.getInstance(
            applicationContext,
            CoroutineScope(Dispatchers.Main),
            cache,
            network
        )
    }


}