package com.ssquare.myapplication.monokrome.ui.auth.termsAndConditions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ssquare.myapplication.monokrome.databinding.FragmentConditionsBinding

class ConditionsFragment : Fragment() {

    private lateinit var binding: FragmentConditionsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConditionsBinding.inflate(inflater)
        setupCloseButtonClickListener()

        return binding.root
    }

    private fun setupCloseButtonClickListener() {
        binding.buttonClose.setOnClickListener {
            this.findNavController().navigateUp()
        }
    }
}