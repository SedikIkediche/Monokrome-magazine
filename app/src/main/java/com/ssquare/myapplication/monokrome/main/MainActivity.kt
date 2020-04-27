package com.ssquare.myapplication.monokrome.main

import android.R.attr
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import br.com.mauker.materialsearchview.MaterialSearchView
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

      val  binding: ActivityMainBinding = DataBindingUtil.setContentView(this,R.layout.activity_main)


    }



}
