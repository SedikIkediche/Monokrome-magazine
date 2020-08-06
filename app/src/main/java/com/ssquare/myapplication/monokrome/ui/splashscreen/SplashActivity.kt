package com.ssquare.myapplication.monokrome.ui.splashscreen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.ssquare.myapplication.monokrome.ui.auth.AuthActivity
import com.ssquare.myapplication.monokrome.ui.main.MainActivity
import com.ssquare.myapplication.monokrome.util.NO_AUTH_TOKEN
import com.ssquare.myapplication.monokrome.util.getAuthToken

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //get auth from sharedPreferences if null or not
        val authToken = getAuthToken(applicationContext)

        when (authToken) {
            NO_AUTH_TOKEN -> {
                navigateTo(this, AuthActivity::class.java)
            }
            else -> {
                navigateTo(this, MainActivity::class.java)
            }
        }
    }

    private fun navigateTo(context: Context, destination: Class<out AppCompatActivity>) {
        val intent = Intent(context, destination)
        startActivity(intent)
        finish()
    }
}
