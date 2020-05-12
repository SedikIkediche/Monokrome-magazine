package com.ssquare.myapplication.monokrome.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.ssquare.myapplication.monokrome.MagazineApplication
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.databinding.ActivityMainBinding
import com.ssquare.myapplication.monokrome.util.DownloadUtils


class MainActivity : AppCompatActivity() {
    private val downloadUtil: DownloadUtils by lazy {
        (this.applicationContext as MagazineApplication).downloadUtils
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    override fun onStop() {
        downloadUtil.unregisterListener()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        downloadUtil.registerListener()
    }


}