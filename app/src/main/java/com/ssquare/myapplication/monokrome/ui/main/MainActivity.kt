package com.ssquare.myapplication.monokrome.ui.main

import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.firebase.messaging.FirebaseMessaging
import com.ssquare.myapplication.monokrome.AppMessagingService.Companion.TOPIC
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.data.Repository
import com.ssquare.myapplication.monokrome.databinding.ActivityMainBinding
import com.ssquare.myapplication.monokrome.db.LocalCache
import com.ssquare.myapplication.monokrome.db.MagazineDatabase
import com.ssquare.myapplication.monokrome.network.MonokromeApi
import com.ssquare.myapplication.monokrome.util.DownloadUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


class MainActivity : AppCompatActivity() {
    lateinit var downloadUtils: DownloadUtils
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
          binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        initDownloadUtils()
        setupNavigation()
        subscribeTopic()

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

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(this.findNavController(R.id.nav_host_fragment), binding.drawer)
    }

    override fun onBackPressed() {
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawer(GravityCompat.START)
        } else{
            super.onBackPressed()
        }
    }
    private fun initDownloadUtils() {
        Log.d("MainActivity", "initDownloadUtils called")
        val network = MonokromeApi.retrofitService
        val magazineDao = MagazineDatabase.getInstance(applicationContext).magazineDao
        val headerDao = MagazineDatabase.getInstance(applicationContext).headerDao
        val cache = LocalCache(magazineDao, headerDao)
        val repository = Repository.getInstance(
            applicationContext, CoroutineScope(Dispatchers.Main), cache, network
        )
        downloadUtils = DownloadUtils.getInstance(applicationContext, repository)
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

    private fun subscribeTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
            .addOnCompleteListener { task ->
                var msg = getString(R.string.message_subscribed)
                if (!task.isSuccessful) {
                    msg = getString(R.string.message_subscribe_failed)
                }
                Log.d("AppMessagingService", msg);
            }
        // [END subscribe_topics]
    }

}