package com.ssquare.myapplication.monokrome.ui.auth.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.ui.auth.AuthActivity
import kotlinx.android.synthetic.main.fragment_register.view.*

/**
 * A simple [Fragment] subclass.
 */
class RegisterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_register, container, false)

        (activity as AuthActivity).setSupportActionBar(view.register_toolbar)

        (activity as AuthActivity).supportActionBar?.title = ""

        view.login_text_view.setOnClickListener {
            this.findNavController().navigate(R.id.loginFragment)
        }

        setHasOptionsMenu(true)

        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){

            android.R.id.home -> this.findNavController().navigateUp()
            else ->  super.onOptionsItemSelected(item)
        }
    }

}