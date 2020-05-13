package com.ssquare.myapplication.monokrome.ui.main.detail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.databinding.FragmentDetailBinding
import com.ssquare.myapplication.monokrome.ui.main.MainActivity

/**
 * A simple [Fragment] subclass.
 */
class DetailFragment : Fragment() {

    private lateinit var binding : FragmentDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDetailBinding.inflate(inflater)


        binding.root.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.list_item_container_background))

        return binding.root
    }

}
