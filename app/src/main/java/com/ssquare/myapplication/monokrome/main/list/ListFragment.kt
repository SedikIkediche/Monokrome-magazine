package com.ssquare.myapplication.monokrome.main.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.databinding.FragmentListBinding
import com.ssquare.myapplication.monokrome.main.data.MagazineListOrException
import com.ssquare.myapplication.monokrome.main.data.Repository
import com.ssquare.myapplication.monokrome.main.util.ClickAction
import com.ssquare.myapplication.monokrome.main.util.MAGAZINE_PATH

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
        val storage = FirebaseStorage.getInstance()
        val repository = Repository.getInstance(database, storage)
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
        if (!response.magazineList.isNullOrEmpty() && response.headerUrl != null && response.exception == null) {
            adapter.addHeaderAndSubmitList(response.magazineList, response.headerUrl)
            hideError()
        } else {
            showError(response.exception!!.message!!)
        }

    }

    private fun initRecyclerView() {
        showLoadingLayout()
        //check connectivity
        val headerListener = MagazineAdapter.HeaderListener { /*Handle header click */ }

        val magazineListener = MagazineAdapter.MagazineListener { magazine, action ->
            when (action) {
                ClickAction.PREVIEW -> {
                    navigateToDetail(magazine.path)
                }
                ClickAction.DOWNLOAD -> {
                    viewModel.downloadFile(magazine, requireContext())
                }
                ClickAction.READ -> { /*Handle Read click */
                }
            }

        }
        adapter = MagazineAdapter(magazineListener, headerListener)
        binding.recyclerview.adapter = adapter
    }

    private fun navigateToDetail(path: String) {
        val pathBundle = Bundle().apply { putString(MAGAZINE_PATH, path) }
        findNavController().navigate(R.id.action_listFragment_to_detailFragment, pathBundle)
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
