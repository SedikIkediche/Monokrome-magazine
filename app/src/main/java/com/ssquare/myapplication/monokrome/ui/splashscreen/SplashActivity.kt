package com.ssquare.myapplication.monokrome.ui.splashscreen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.ssquare.myapplication.monokrome.ui.auth.AuthActivity
import com.ssquare.myapplication.monokrome.ui.main.MainActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser

        when (currentUser) {
            null -> {
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
