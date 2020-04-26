package com.ssquare.myapplication.monokrome.work

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ssquare.myapplication.monokrome.util.commitCacheData

class CacheWorker(private val appContext: Context, params: WorkerParameters) : Worker(
    appContext, params
) {
    @SuppressLint("RestrictedApi")
    override fun doWork(): Result {
        commitCacheData(appContext, false)
        return Result.Success()
    }
}