package com.ssquare.myapplication.monokrome.ui.main

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import androidx.appcompat.app.AlertDialog
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
import com.ssquare.myapplication.monokrome.util.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), DrawerLayout.DrawerListener {
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
        binding.navigationView.setNavigationItemSelectedListener {
            return@setNavigationItemSelectedListener when (it.itemId) {
                R.id.home -> {
                    closeDrawer()
                    true
                }
                R.id.about_us -> {
                    closeDrawer()
                    true
                }
                R.id.rate_us -> {
                    closeDrawer()
                    openPlayStore()
                    true
                }
                R.id.upload -> {
                    closeDrawer()
                    //Here where your code goes
                    true
                }
                R.id.web_site -> {
                    closeDrawer()
                    openWebSite()
                    true
                }
                R.id.facebook -> {
                    closeDrawer()
                    openFacebook()
                    true
                }
                R.id.instagram -> {
                    closeDrawer()
                    openInstagram()
                    true
                }
                R.id.contact_us -> {
                    closeDrawer()
                    openContact()
                    true
                }
                R.id.settings -> {
                    closeDrawer()
                    true
                }
                R.id.logout -> {
                    closeDrawer()
                    logout()
                    true
                }
                else -> true
            }

        }
        binding.drawer.addDrawerListener(this)
    }

    private fun openContact() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("monokromemag@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Question from the app")
            putExtra(Intent.EXTRA_TEXT, "Hey team,\n")
        }
        if (intent.resolveActivity(packageManager) == null) {
            startActivity(intent)
        } else {
            showErrorDialog(
                this,
                getString(R.string.drawer_message_error),
                getString(R.string.ok),
                getString(R.string.oops)
            )
        }
    }

    private fun openInstagram() {
        val uri = Uri.parse(INSTAGRAM_App)
        val insta = Intent(Intent.ACTION_VIEW, uri)
        insta.setPackage(INSTAGRAM_PACKAGE)

        val list =
            packageManager.queryIntentActivities(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
        if (list.size > 0) {
            startActivity(insta)
        } else {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(INSTAGRAM_BROWSER)
            )
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                showErrorDialog(
                    this,
                    getString(R.string.drawer_message_error),
                    getString(R.string.ok),
                    getString(R.string.oops)
                )
            }
        }
    }

    private fun openFacebook() {
        val uri = Uri.parse(FACEBOOK_BROWSER)

        val intent = Intent(Intent.ACTION_VIEW,uri)

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            showErrorDialog(
                this,
                getString(R.string.drawer_message_error),
                getString(R.string.ok),
                getString(R.string.oops)
            )
        }
    }

    private fun openWebSite() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(WEB_SITE)
        )
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            showErrorDialog(
                this,
                getString(R.string.drawer_message_error),
                getString(R.string.ok),
                getString(R.string.oops)
            )
        }
    }

    private fun openPlayStore() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(PLAY_STORE + PACKAGE_NAME)
        )
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            showErrorDialog(
                this,
                getString(R.string.drawer_message_error),
                getString(R.string.ok),
                getString(R.string.oops)
            )
        }
    }

    private fun logout() {
        authRepository.logoutUser()
        FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC)
        val intent = Intent(this@MainActivity, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setHomeChecked() {
        binding.navigationView.setCheckedItem(R.id.home)
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
            closeDrawer()
        } else {
            super.onBackPressed()
        }
    }

    private fun closeDrawer() {
        binding.drawer.closeDrawer(GravityCompat.START)
    }

    private fun setupNavigation() {
        /**   NavigationUI.setupWithNavController(
        binding.navigation,
        Navigation.findNavController(this, R.id.nav_host_fragment)
        )**/

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

    override fun onDrawerStateChanged(newState: Int) {

    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

    }

    override fun onDrawerClosed(drawerView: View) {
        setHomeChecked()
    }

    override fun onDrawerOpened(drawerView: View) {

    }

}