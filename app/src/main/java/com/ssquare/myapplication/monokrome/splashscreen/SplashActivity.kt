package com.ssquare.myapplication.monokrome.splashscreen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ssquare.myapplication.monokrome.main.MainActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent =  Intent(this,MainActivity::class.java)
        startActivity(intent)
        finish()

    }
}
