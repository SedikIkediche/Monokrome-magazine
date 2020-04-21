package com.ssquare.myapplication.monokrome.main.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.database.FirebaseDatabase
import com.ssquare.myapplication.monokrome.databinding.FragmentDetailBinding
import com.ssquare.myapplication.monokrome.main.data.Repository
import com.ssquare.myapplication.monokrome.main.util.MAGAZINE_PATH

/**
 * A simple [Fragment] subclass.
 */
class DetailFragment : Fragment() {
    lateinit var binding: FragmentDetailBinding
    lateinit var viewModel: DetailViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailBinding.inflate(inflater)
        val database = FirebaseDatabase.getInstance()
        val repository = Repository.getInstance(database)
        val magazinePath = requireArguments().getString(MAGAZINE_PATH, "")
        val factory = DetailViewModelFactory(repository, magazinePath)
        viewModel = ViewModelProviders.of(this, factory).get(DetailViewModel::class.java)
        return binding.root
    }

}
