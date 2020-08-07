package com.ssquare.myapplication.monokrome.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import com.ssquare.myapplication.monokrome.AppMessagingService.Companion.TOPIC
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.data.AuthRepository
import com.ssquare.myapplication.monokrome.databinding.ActivityMainBinding
import com.ssquare.myapplication.monokrome.ui.auth.AuthActivity
import com.ssquare.myapplication.monokrome.util.DownloadUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var downloadUtils: DownloadUtils

    @Inject
    lateinit var authRepository: AuthRepository
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setupNavigation()
        setupDrawerMenuItemClick()
        subscribeTopic()

    }

    private fun setupDrawerMenuItemClick() {
        binding.navigation.setNavigationItemSelectedListener {
            return@setNavigationItemSelectedListener when (it.itemId) {
                R.id.home -> {
                    true
                }
                R.id.about_us -> {
                    true
                }
                R.id.rate_us -> {
                    true
                }
                R.id.web_site -> {
                    true
                }
                R.id.facebook -> {
                    true
                }
                R.id.instagram -> {
                    true
                }
                R.id.contact_us -> {
                    true
                }
                R.id.settings -> {
                    true
                }
                R.id.logout -> {
                    authRepository.logoutUser()
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC)
                    val intent = Intent(this@MainActivity, AuthActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }

                else -> true
            }

        }
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
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(
            this.findNavController(R.id.nav_host_fragment),
            binding.drawer
        )
    }

    override fun onBackPressed() {
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun setupNavigation() {
        NavigationUI.setupWithNavController(
            binding.navigation,
            Navigation.findNavController(this, R.id.nav_host_fragment)
        )

        Navigation.findNavController(this, R.id.nav_host_fragment)
            .addOnDestinationChangedListener { controller, destination, arguments ->
                when (destination.id) {
                    R.id.listFragment -> {
                        binding.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    }
                    else -> {
                        binding.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    }
                }
            }
    }

    private fun checkGooglePlayServices(): Boolean {
        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        return if (status != ConnectionResult.SUCCESS) {
            Log.e("AppMessagingService", "Error")
            // ask user to update google play services.
            false
        } else {
            Log.i("AppMessagingService", "Google play services updated")
            true
        }
    }

    private fun subscribeTopic() {
        if (checkGooglePlayServices()) {
            FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
                .addOnCompleteListener { task ->
                    Log.d("AppMessagingService", task.toString())
                    var msg = getString(R.string.message_subscribe_failed)
                    if (task.isSuccessful) {
                        msg = getString(R.string.message_subscribed)
                    }
                    Log.d("AppMessagingService", msg)
                }.addOnFailureListener { exception ->
                    Log.e(
                        "AppMessagingService",
                        getString(R.string.message_subscribe_failed),
                        exception
                    )
                }
        } else {
            Log.d("AppMessagingService", "Error with GooglePlayServices")
        }

    }

    companion object {
        private const val TAG = "MainActivity"
    }

}