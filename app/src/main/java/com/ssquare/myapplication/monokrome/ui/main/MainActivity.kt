package com.ssquare.myapplication.monokrome.ui.main

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.auth0.android.jwt.JWT
import com.ssquare.myapplication.monokrome.AppMessagingService.Companion.subscribeTopic
import com.ssquare.myapplication.monokrome.AppMessagingService.Companion.unsubscribeFromTopic
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

    var selectedItem = 0

    @Inject
    lateinit var authRepository: AuthRepository
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setupNavigation()
        setupDrawerMenuItemClick()
        subscribeTopic(this)
        downloadUtils.registerListener()
        downloadUtils.checkForActiveDownLoadsWhenMainActivityCreated()
    }

    private fun setupDrawerMenuItemClick() {
        setHomeChecked()
        binding.navigationView.menu.findItem(R.id.admin).isVisible = isAdmin(this)
        binding.navigationView.setNavigationItemSelectedListener {menuItem : MenuItem ->
             selectedItem = menuItem.itemId
             closeDrawer()
            true
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
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            showOneButtonDialog(
                this,
                message = getString(R.string.drawer_message_error),
                positiveButtonText = getString(R.string.ok),
                title = getString(R.string.oops)
            )
        }
    }

    private fun openInstagram() {
        val uri = Uri.parse(INSTAGRAM_App)
        val instagramIntent = Intent(Intent.ACTION_VIEW, uri)
        instagramIntent.setPackage(INSTAGRAM_PACKAGE)

        try {
            startActivity(instagramIntent)
        } catch (exception: ActivityNotFoundException) {
            openWebSite(INSTAGRAM_BROWSER)
        }
    }

    private fun openFacebook() {
        val uri = Uri.parse(FACEBOOK_BROWSER)

        val intent = Intent(Intent.ACTION_VIEW, uri)

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            showOneButtonDialog(
                this,
                message = getString(R.string.drawer_message_error),
                positiveButtonText = getString(R.string.ok),
                title = getString(R.string.oops)
            )
        }
    }

    private fun openWebSite(url: String) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(url)
        )
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            showOneButtonDialog(
                this,
                message = getString(R.string.drawer_message_error),
                positiveButtonText = getString(R.string.ok),
                title = getString(R.string.oops)
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
            showOneButtonDialog(
                this,
                message = getString(R.string.drawer_message_error),
                positiveButtonText = getString(R.string.ok),
                title = getString(R.string.oops)
            )
        }
    }

    private fun logout() {
        authRepository.logoutUser()
        unsubscribeFromTopic()
        val intent = Intent(this@MainActivity, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setHomeChecked() {
        binding.navigationView.setCheckedItem(R.id.home)
    }

    override fun onDestroy() {
        downloadUtils.unregisterListener()
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

    private fun isAdmin(context: Context): Boolean {
        getAuthToken(context)?.let {
            val jwt = JWT(it)
            val claim = jwt.getClaim("isAdmin").asInt()
            return claim == 1
        }
        return false
    }

    private fun closeDrawer() {
        binding.drawer.closeDrawer(GravityCompat.START)
    }

    private fun setupNavigation() {

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


    override fun onDrawerStateChanged(newState: Int) {

    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

    }

    override fun onDrawerClosed(drawerView: View) {
        when (selectedItem) {
            R.id.home -> {
                setHomeChecked()
            }
            R.id.about_us -> {
                setHomeChecked()
                this.findNavController(R.id.nav_host_fragment).navigate(R.id.aboutFragment)
            }
            R.id.rate_us -> {
                setHomeChecked()
                openPlayStore()
            }
            R.id.upload -> {
                setHomeChecked()
                findNavController(R.id.nav_host_fragment).navigate(R.id.action_listFragment_to_uploadFragment)
            }
            R.id.web_site -> {
                setHomeChecked()
                openWebSite(WEB_SITE)
            }
            R.id.facebook -> {
                setHomeChecked()
                openFacebook()
            }
            R.id.instagram -> {
                setHomeChecked()
                openInstagram()
            }
            R.id.contact_us -> {
                setHomeChecked()
                openContact()
            }
            R.id.logout -> {
                setHomeChecked()
                logout()
            }
        }
        selectedItem = 0
    }

    override fun onDrawerOpened(drawerView: View) {

    }

}