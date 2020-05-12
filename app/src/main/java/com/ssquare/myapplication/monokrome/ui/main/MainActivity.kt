package com.ssquare.myapplication.monokrome.ui.main

import android.app.SearchManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.plusAssign
import androidx.navigation.ui.NavigationUI
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.databinding.ActivityMainBinding
import com.ssquare.myapplication.monokrome.databinding.FragmentListBinding
import com.ssquare.myapplication.monokrome.ui.main.list.ListFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
          binding = DataBindingUtil.setContentView(this,R.layout.activity_main)

        NavigationUI.setupWithNavController(
            binding.navigation,
            Navigation.findNavController(this,R.id.nav_host_fragment)
        )

        Navigation.findNavController(this,R.id.nav_host_fragment)
            .addOnDestinationChangedListener { controller, destination, arguments ->

                when (destination.id) {
                    R.id.listFragment -> {
                        binding.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    }else -> {
                    binding.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }
                }
            }

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

}