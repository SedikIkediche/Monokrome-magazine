package com.ssquare.myapplication.monokrome.ui.main

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
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
    }

    private fun setupDrawerMenuItemClick() {
        binding.navigationView.menu.findItem(R.id.admin).isVisible = isAdmin(this)
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
                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_listFragment_to_uploadFragment)
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
                showOneButtonDialog(
                    this,
                    message = getString(R.string.drawer_message_error),
                    positiveButtonText = getString(R.string.ok),
                    title = getString(R.string.oops)
                )
            }
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

    private fun openWebSite() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(WEB_SITE)
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
        downloadUtils.clear()
        unsubscribeFromTopic()
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