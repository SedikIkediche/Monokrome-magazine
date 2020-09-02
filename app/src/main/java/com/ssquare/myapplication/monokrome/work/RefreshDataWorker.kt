package com.ssquare.myapplication.monokrome.work

import android.annotation.SuppressLint
import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ssquare.myapplication.monokrome.data.Repository
import com.ssquare.myapplication.monokrome.util.isDownloadActive
import timber.log.Timber

class RefreshDataWorker @WorkerInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
     private val repository: Repository) :
    CoroutineWorker(appContext, params) {


    @SuppressLint("RestrictedApi")
    override suspend fun doWork(): Result {
        Timber.d("doWork called")

        return if (!isDownloadActive(appContext)) {
            val loadState = repository.loadAndCacheData()
            if (loadState) {
                Result.Success()
            } else {
                Result.Retry()
            }
        } else {
            Result.Retry()
        }
    }
}