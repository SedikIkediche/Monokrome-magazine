package com.ssquare.myapplication.monokrome.ui.main

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.data.Repository
import com.ssquare.myapplication.monokrome.databinding.ActivityMainBinding
import com.ssquare.myapplication.monokrome.db.LocalCache
import com.ssquare.myapplication.monokrome.db.MagazineDatabase
import com.ssquare.myapplication.monokrome.network.FirebaseServer
import com.ssquare.myapplication.monokrome.util.DownloadUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


class MainActivity : AppCompatActivity() {
    lateinit var downloadUtils: DownloadUtils


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        initDownloadUtils()
    }

    override fun onStop() {
        downloadUtils.unregisterListener()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        downloadUtils.registerListener()
    }

    override fun onDestroy() {
        downloadUtils.close()
        DownloadUtils.clear()
        super.onDestroy()
    }

    private fun initDownloadUtils() {
        Log.d("MainActivity", "initDownloadUtils called")
        val database = FirebaseDatabase.getInstance()
        val storage = FirebaseStorage.getInstance()
        val network = FirebaseServer(database, storage)
        val magazineDao = MagazineDatabase.getInstance(applicationContext).magazineDao
        val headerDao = MagazineDatabase.getInstance(applicationContext).headerDao
        val cache = LocalCache(magazineDao, headerDao)
        val repository = Repository.getInstance(
            applicationContext, CoroutineScope(Dispatchers.Main), cache, network
        )
        downloadUtils = DownloadUtils.getInstance(applicationContext, repository)
    }

}