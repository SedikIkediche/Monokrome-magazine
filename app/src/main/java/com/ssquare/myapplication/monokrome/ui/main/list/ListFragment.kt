package com.ssquare.myapplication.monokrome.ui.main.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.data.Header
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.data.Repository
import com.ssquare.myapplication.monokrome.databinding.FragmentListBinding
import com.ssquare.myapplication.monokrome.db.LocalCache
import com.ssquare.myapplication.monokrome.db.MagazineDatabase
import com.ssquare.myapplication.monokrome.network.FirebaseServer
import com.ssquare.myapplication.monokrome.util.*
import timber.log.Timber

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

        val isDataCached = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            DATA_CACHED, false
        )
        val isUpToDate = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            DATA_UP_TO_DATE, false
        )

        Timber.d("soheib: onViewCreated()")
        binding = FragmentListBinding.inflate(inflater)
        val database = FirebaseDatabase.getInstance()
        val storage = FirebaseStorage.getInstance()
        val network = FirebaseServer(database, storage)
        val magazineDao = MagazineDatabase.getInstance(requireContext()).magazineDao
        val headerDao = MagazineDatabase.getInstance(requireContext()).headerDao
        val cache = LocalCache(magazineDao, headerDao, lifecycleScope)

        val repository = Repository.getInstance(cache, network)
        val factory = ListViewModelFactory(repository)
        viewModel = ViewModelProviders.of(this, factory).get(ListViewModel::class.java)

        initRecyclerView()


        if (isUpToDate) {
            viewModel.cachedData.observe(viewLifecycleOwner, Observer {
                setupUi(it.first, it.second, null)
            })
        } else {
            if (isConnected(requireContext())) {
                showLoading()
                viewModel.networkResponse.observe(viewLifecycleOwner, Observer {
                    setupUi(it.header, it.magazineList, it.exception)
                    viewModel.cacheData(it.header, it.magazineList) {
                        commitUpToDateData(requireContext(), true)
                        if (!isDataCached) commitCacheData(requireContext(), true)
                    }
                })
            } else {
                if (isDataCached) {
                    viewModel.cachedData.observe(viewLifecycleOwner, Observer {
                        setupUi(it.first, it.second, null)
                    })
                } else {
                    showError("Please connect to the internet")
                }
            }


        }


        return binding.root
    }

    private fun setupUi(header: Header?, magazines: List<Magazine>?, exception: Exception?) {
        if (header != null && magazines != null && exception == null) {
            adapter.addHeaderAndSubmitList(magazines, header)
            showData()
        } else {
            showError("Something wrong happened: $exception")
        }
    }

    private fun initRecyclerView() {

        val headerListener = MagazineAdapter.HeaderListener { /*Handle header click */ }

        val magazineListener = MagazineAdapter.MagazineListener { magazine, action ->
            when (action) {
                ClickAction.PREVIEW -> {
                    navigateToDetail(magazine.path)
                }
                ClickAction.DOWNLOAD -> {
                    downloadMagazine(magazine)

                }
                ClickAction.READ -> { /*Handle Read click */
                }
            }

        }
        adapter = MagazineAdapter(magazineListener, headerListener)
        binding.recyclerview.adapter = adapter
    }

    private fun downloadMagazine(magazine: Magazine) {
        if (isConnected(requireContext())) {
            viewModel.downloadFile(magazine, requireContext())
        } else {
            showErrorLayout(getString(R.string.network_down))
        }
    }

    private fun navigateToDetail(path: String) {
        val pathBundle = Bundle().apply { putString(MAGAZINE_PATH, path) }
        findNavController().navigate(R.id.action_listFragment_to_detailFragment, pathBundle)
    }

    private fun showLoading() {
        binding.run {
            recyclerview.visibility = View.GONE
            textError.visibility = View.GONE
            shimmerLayout.visibility = View.VISIBLE
            shimmerLayout.startShimmer()
        }

    }


    private fun showError(errorText: String) {
        binding.run {
            shimmerLayout.hideShimmer()
            shimmerLayout.visibility = View.GONE
            recyclerview.visibility = View.GONE
        }
        showErrorLayout(errorText)
    }

    private fun showData() {
        binding.run {
            shimmerLayout.hideShimmer()
            shimmerLayout.visibility = View.GONE
            textError.visibility = View.GONE
            recyclerview.visibility = View.VISIBLE
        }
    }

    private fun showErrorLayout(errorText: String) {
        binding.run {
            textError.visibility = View.VISIBLE
            textError.text = errorText
        }
    }

}
