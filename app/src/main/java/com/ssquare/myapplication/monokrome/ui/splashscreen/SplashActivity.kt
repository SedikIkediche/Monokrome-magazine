package com.ssquare.myapplication.monokrome.ui.splashscreen

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ssquare.myapplication.monokrome.ui.main.MainActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent =  Intent(this,MainActivity::class.java)
        startActivity(intent)
        finish()

    }
}
