package com.ssquare.myapplication.monokrome.auth.login

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController

import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.main.MainActivity
import kotlinx.android.synthetic.main.fragment_login.view.*

/**
 * A simple [Fragment] subclass.
 */
class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view  = inflater.inflate(R.layout.fragment_login, container, false)


        view.login_button.setOnClickListener {
            val intent = Intent(context,MainActivity::class.java).apply {
                startActivity(this)
            }
        }

        view.join_text_view.setOnClickListener {
            this.findNavController().navigate(R.id.registerFragment)
        }

        return  view
    }

}
