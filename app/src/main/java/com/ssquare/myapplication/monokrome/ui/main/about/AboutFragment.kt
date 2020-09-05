package com.ssquare.myapplication.monokrome.ui.main.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ssquare.myapplication.monokrome.R
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

        binding.root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.list_item_container_background))

        setupCloseButtonClickListener()

        return binding.root
    }

    private fun setupCloseButtonClickListener() {
        binding.buttonClose.setOnClickListener {
            this.findNavController().navigateUp()
        }
    }

}