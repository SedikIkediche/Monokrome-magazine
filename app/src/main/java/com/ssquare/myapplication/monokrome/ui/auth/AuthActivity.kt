package com.ssquare.myapplication.monokrome.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ssquare.myapplication.monokrome.R
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AuthActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        Timber.d("onCreate: $")
    }
}
