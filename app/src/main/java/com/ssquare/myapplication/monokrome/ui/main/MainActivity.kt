package com.ssquare.myapplication.monokrome.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import com.auth0.android.jwt.JWT
import com.ssquare.myapplication.monokrome.AppMessagingService
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.data.AuthRepository
import com.ssquare.myapplication.monokrome.databinding.ActivityMainBinding
import com.ssquare.myapplication.monokrome.ui.auth.AuthActivity
import com.ssquare.myapplication.monokrome.util.DownloadUtils
import com.ssquare.myapplication.monokrome.util.getAuthToken
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
        //setupNavigation()
        setupDrawerMenu()
        AppMessagingService.subscribeTopic(this)
        downloadUtils.setToken(getAuthToken(applicationContext))
    }

    private fun setupDrawerMenu() {
        binding.navigation.menu.findItem(R.id.item_admin).isVisible = isAdmin(this)
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

                R.id.item_upload -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.uploadFragment)
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
                    downloadUtils.clear()
                    AppMessagingService.unsubscribeFromTopic()
                    val intent = Intent(this@MainActivity, AuthActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }

                else -> true
            }
        }
    }

    private fun isAdmin(context: Context): Boolean {
        getAuthToken(context)?.let {
            val jwt = JWT(it)
            val claim = jwt.getClaim("isAdmin").asInt()
            return claim == 1
        }
        return false
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
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


    companion object {
        private const val TAG = "MainActivity"
    }

}