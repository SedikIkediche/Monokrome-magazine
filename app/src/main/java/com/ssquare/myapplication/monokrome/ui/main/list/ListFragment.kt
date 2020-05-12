package com.ssquare.myapplication.monokrome.ui.main.list

import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
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
import com.ssquare.myapplication.monokrome.ui.main.MainActivity
import com.ssquare.myapplication.monokrome.util.*
import com.ssquare.myapplication.monokrome.util.networkcheck.ConnectivityProvider
import com.ssquare.myapplication.monokrome.work.RefreshDataWorker
import kotlinx.android.synthetic.main.fragment_list.*
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 */
class ListFragment : Fragment() , ConnectivityProvider.ConnectivityStateListener {
    lateinit var binding: FragmentListBinding
    private lateinit var viewModel: ListViewModel
    private lateinit var adapter: MagazineAdapter
    private var isNotConnected = false
    private val provider: ConnectivityProvider by lazy { ConnectivityProvider.createProvider(requireContext()) }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
          binding =  FragmentListBinding.inflate(inflater)

        setUpToolbar()

        val database = FirebaseDatabase.getInstance()
        val storage = FirebaseStorage.getInstance()
        val network = FirebaseServer(database, storage)
        val magazineDao = MagazineDatabase.getInstance(requireContext()).magazineDao
        val headerDao = MagazineDatabase.getInstance(requireContext()).headerDao
        val cache = LocalCache(magazineDao, headerDao)
        val repository = Repository.getInstance(lifecycleScope, cache, network)
        val factory = ListViewModelFactory(repository)
        viewModel = ViewModelProviders.of(this, factory).get(ListViewModel::class.java)

        initRecyclerView()

        viewModel.networkError.observe(viewLifecycleOwner, Observer {
            setupUi(null, null, it)
        })

        viewModel.data.observe(viewLifecycleOwner, Observer {
            if (isDataCached(requireContext()))
                setupUi(it.first, it.second)
        })

        return binding.root
    }

    private fun setUpToolbar() {

        getMainActivity().setSupportActionBar(binding.listFragmentToolbar)
        getMainActivity().supportActionBar?.title = getString(R.string.home)

        setHasOptionsMenu(true)
        addBannerClickListener()

    }

    private fun getMainActivity() = activity as MainActivity

    override fun onStart() {
        super.onStart()
        provider.addListener(this)
    }

    override fun onStop() {
        super.onStop()
        provider.removeListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fagment_loolbar_menu, menu)

        menu.findItem(R.id.sort_by_most_recent).isChecked = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search -> {
                findNavController().navigate(R.id.searchMagazineFragment)
                true
            }
            R.id.sort_by_most_recent-> {
                item.isChecked = !item.isChecked
                true
            }
            R.id.sort_from_a_to_z -> {
                item.isChecked = !item.isChecked
                true
            }
            R.id.sort_from_z_to_a -> {
                item.isChecked = !item.isChecked
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addBannerClickListener() {
        binding.closeButton.setOnClickListener {
            binding.bannerLayout.visibility = View.GONE
        }
        binding.tryAgainButton.setOnClickListener {
            binding.bannerLayout.visibility = View.GONE
        }
    }


    private fun cacheData() {
        viewModel.loadAndCacheData()
        commitCacheData(requireContext())
        launchUpdateWorker()
    }

    private fun setupUi(header: Header?, magazines: List<Magazine>?, exception: Exception? = null) {
        when {
            header == null && magazines.isNullOrEmpty() && exception == null -> {
                return
            }
            header != null && magazines != null && exception == null -> {
                adapter.addHeaderAndSubmitList(magazines, header)
                showData()
            }
            exception != null -> {
                showError("Something wrong happened: $exception")
            }
        }
    }

    private fun initRecyclerView() {
        val headerListener = MagazineAdapter.HeaderListener { /*Handle header click */ }

        val magazineListener =
            MagazineAdapter.MagazineListener { magazine, action ->
                when (action) {
                    ClickAction.PREVIEW -> {
                        navigateToDetail( magazine.path)
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
            downloadFile(magazine, requireContext())
        } else {
            showErrorLayout(getString(R.string.network_down))
        }
    }

    private fun launchUpdateWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED).build()

        val cacheWorkRequest = OneTimeWorkRequest.Builder(RefreshDataWorker::class.java)
            .setInitialDelay(REFRESH_TIME, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(requireContext().applicationContext)
            .enqueue(cacheWorkRequest)
    }

    private fun navigateToDetail(
        path: String
    ) {
        val pathBundle = Bundle().apply { putString(MAGAZINE_PATH, path) }
        this.findNavController().navigate(R.id.action_listFragment_to_detailFragment,pathBundle)
    }

    private fun showLoading() {
        binding.run {
            shimmerLayout.startShimmer()
            recyclerview.visibility = View.GONE
            errorContainer.visibility = View.GONE
            bannerLayout.visibility = View.GONE
            onlineIndicator.visibility = View.GONE
            if (isNotConnected) {
                backOnlineIndicator.visibility = View.VISIBLE
                isNotConnected = false
            }
            shimmerLayout.visibility = View.VISIBLE

        }
    }

    private fun showError(errorText: String) {
        binding.run {
            shimmerLayout.visibility = View.GONE
            shimmerLayout.stopShimmer()
            recyclerview.visibility = View.GONE
        }
        showErrorLayout(errorText)
    }

    private fun showData() {
        binding.run {
            shimmerLayout.visibility = View.GONE
            backOnlineIndicator.visibility = View.GONE
            errorContainer.visibility = View.GONE
            bannerLayout.visibility = View.GONE
            onlineIndicator.visibility = View.GONE
            recyclerview.visibility = View.VISIBLE
            shimmerLayout.stopShimmer()
        }
    }

    private fun showErrorLayout(errorText: String) {
        binding.run {
            errorContainer.visibility = View.VISIBLE
            textError.text = errorText
            bannerLayout.visibility = View.VISIBLE
            onlineIndicator.visibility = View.VISIBLE
            isNotConnected = true
        }
    }

    override fun onStateChange(state: ConnectivityProvider.NetworkState) {
        if (!isDataCached(requireContext())) {
            showLoading()
            when (state.hasInternet()) {
                true -> {
                    cacheData()
                }
                false -> {
                    showError("No results found!")
                }
            }
        }
    }
}