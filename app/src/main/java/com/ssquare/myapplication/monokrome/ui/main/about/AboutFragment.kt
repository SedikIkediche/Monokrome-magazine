package com.ssquare.myapplication.monokrome.ui.main.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ssquare.myapplication.monokrome.databinding.FragmentAboutBinding

/**
 * A simple [Fragment] subclass.
 */
class AboutFragment : Fragment() {

    private lateinit var binding: FragmentAboutBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAboutBinding.inflate(inflater)

        setupCloseButtonClickListener()

        return binding.root
    }

    private fun setupCloseButtonClickListener() {
        binding.buttonClose.setOnClickListener {
            this.findNavController().navigateUp()
        }
    }

}