package com.ssquare.myapplication.monokrome.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val  binding: ActivityMainBinding = DataBindingUtil.setContentView(this,R.layout.activity_main)


    }



}