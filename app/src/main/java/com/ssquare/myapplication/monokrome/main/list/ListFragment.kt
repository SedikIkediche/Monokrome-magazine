package com.ssquare.myapplication.monokrome.main.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.database.FirebaseDatabase
import com.ssquare.myapplication.monokrome.databinding.FragmentListBinding
import com.ssquare.myapplication.monokrome.main.data.MagazineListOrException
import com.ssquare.myapplication.monokrome.main.data.Repository
import com.ssquare.myapplication.monokrome.main.util.ClickAction

/**
 * A simple [Fragment] subclass.
 */
class ListFragment : Fragment() {
    lateinit var binding: FragmentListBinding
    private lateinit var viewModel: ListViewModel
    private lateinit var adapter: MagazineAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListBinding.inflate(inflater)
        val database = FirebaseDatabase.getInstance()
        val repository = Repository.getInstance(database)
        val factory = ListViewModelFactory(repository)
        viewModel = ViewModelProviders.of(this, factory).get(ListViewModel::class.java)

        initRecyclerView()

        viewModel.magazines.observe(viewLifecycleOwner, Observer {
            setupUi(it)
        })

        return binding.root
    }

    private fun setupUi(response: MagazineListOrException) {
        hideLoadingLayout()
        if (!response.data.isNullOrEmpty() && response.exception == null) {
            adapter.addHeaderAndSubmitList(response.data)
            hideError()
        } else {
            showError(response.exception!!.message!!)
        }

    }

    private fun initRecyclerView() {
        showLoadingLayout()
        val headerListener = MagazineAdapter.HeaderListener {
            Toast.makeText(requireContext(), "Header Clicked", Toast.LENGTH_SHORT).show()
        }

        val magazineListener = MagazineAdapter.MagazineListener { path, action ->
            when (action) {
                ClickAction.PREVIEW -> Toast.makeText(
                    context,
                    "item $path clicked, PREVIEW",
                    Toast.LENGTH_SHORT
                ).show()
                ClickAction.DOWNLOAD -> Toast.makeText(
                    context,
                    "item $path clicked action is DOWNLOAD",
                    Toast.LENGTH_SHORT
                ).show()
                ClickAction.READ -> Toast.makeText(
                    context,
                    "item $path clicked action is READ",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
        adapter = MagazineAdapter(magazineListener, headerListener)
        binding.recyclerview.adapter = adapter

    }

    private fun showLoadingLayout() {
        binding.shimmerLayout.startShimmer()
        binding.recyclerview.visibility = View.GONE
    }

    private fun hideLoadingLayout() {
        binding.shimmerLayout.hideShimmer()
        binding.recyclerview.visibility = View.VISIBLE
    }

    private fun showError(errorText: String) {
        binding.textError.visibility = View.VISIBLE
        binding.textError.text = errorText

    }

    private fun hideError() {
        binding.textError.visibility = View.GONE
    }


}
