package com.ssquare.myapplication.monokrome.ui.main.list

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import br.com.mauker.materialsearchview.MaterialSearchView
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
import com.ssquare.myapplication.monokrome.ui.pdf.PdfViewActivity
import com.ssquare.myapplication.monokrome.util.*
import kotlinx.android.synthetic.main.fragment_list.*

/**
 * A simple [Fragment] subclass.
 */
class ListFragment : Fragment() {
    lateinit var binding: FragmentListBinding
    private lateinit var repository: Repository
    private lateinit var viewModel: ListViewModel
    private lateinit var adapter: MagazineAdapter

    private val networkCheck: NetworkCheck by lazy {
        NetworkCheck(
            requireContext(),
            lifecycleScope
        ).apply { registerNetworkCallback() }
    }

    private var isDownloadRunning = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentListBinding.inflate(inflater)
        setupToolbar()

        val database = FirebaseDatabase.getInstance()
        val storage = FirebaseStorage.getInstance()
        val network = FirebaseServer(database, storage)
        val magazineDao = MagazineDatabase.getInstance(requireContext()).magazineDao
        val headerDao = MagazineDatabase.getInstance(requireContext()).headerDao
        val cache = LocalCache(magazineDao, headerDao)
        repository = Repository.getInstance(lifecycleScope, cache, network)
        val factory = ListViewModelFactory(repository)
        viewModel = ViewModelProviders.of(this, factory).get(ListViewModel::class.java)

        initRecyclerView()
        Log.d("ListFragment", "is data chached = ${isDataCached(requireContext())}")
        networkCheck.isConnected.observe(viewLifecycleOwner, Observer { isConnected ->
            if (!isDataCached(requireContext())) {
                showLoading()
                when (isConnected) {
                    true -> {
                        cacheData()
                    }
                    false -> {
                        showError("Please Connect To The Internet!")
                    }
                }
            } else {
                if (!isConnected && isDownloadRunning) {
                    viewModel.terminateRunningDownloads()
                    isDownloadRunning = false
                    // showError("Please Connect To The Internet!")
                }

            }
        })

        viewModel.networkError.observe(viewLifecycleOwner, Observer {
            setupUi(null, null, it)
        })

        viewModel.data.observe(viewLifecycleOwner, Observer {
            if (isDataCached(requireContext()))
                setupUi(it.first, it.second)
        })

        return binding.root
    }

    override fun onPause() {
        Log.d("ListFragment", "onPause() called")
        networkCheck.unregisterNetworkCallback()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        networkCheck.registerNetworkCallback()
    }

    override fun onDestroyView() {
        Log.d("ListFragment", "onDestroyView called")
        viewModel.terminateRunningDownloads()
        super.onDestroyView()
    }

    override fun onDestroy() {
        toast(requireContext().applicationContext, "onDestroy() called")
        Log.d("ListFragment", "onDestroy called")
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val matches =
                data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (matches != null && matches.size > 0) {
                val searchWrd = matches[0]
                if (!TextUtils.isEmpty(searchWrd)) {
                    binding.searchView.setQuery(searchWrd, false)
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fagment_loolbar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.search -> {
                showSearchView()
                true
            }
            R.id.filter_list -> {
                viewModel.terminateRunningDownloads()
                true
            }
            android.R.id.home -> {
                if (!binding.drawer.isDrawerOpen(GravityCompat.START)) {
                    binding.drawer.openDrawer(GravityCompat.START)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupToolbar() {
        (activity as MainActivity).setSupportActionBar(binding.listFragmentToolbar)
        NavigationUI.setupActionBarWithNavController(
            activity as MainActivity,
            this.findNavController(),
            binding.drawer
        )
        NavigationUI.setupWithNavController(binding.navigation, this.findNavController())
        setHasOptionsMenu(true)
        addSearchViewListener()
    }

    private fun showSearchView() {
        binding.searchView.openSearch()
    }

    private fun addSearchViewListener() {
        binding.searchView.setSearchViewListener(object : MaterialSearchView.SearchViewListener {
            override fun onSearchViewOpened() {
                requireActivity().onBackPressedDispatcher.addCallback(
                    viewLifecycleOwner,
                    onBackPressedCallback
                )
            }

            override fun onSearchViewClosed() {
                onBackPressedCallback.remove()
            }
        })

        binding.searchView.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {

                binding.searchView.closeSearch()

                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        binding.searchView.setOnVoiceClickedListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice searching...")
            startActivityForResult(intent, REQUEST_CODE)
        }

        binding.searchView.findViewById<View>(R.id.transparent_view).visibility = View.GONE
        binding.searchView.findViewById<LinearLayout>(R.id.search_bar)
            .setBackgroundResource(R.drawable.search_view_background)
    }


    private fun cacheData() {
        viewModel.loadAndCacheData()
        commitCacheData(requireContext())
        launchUpdateWorker(requireContext())
    }

    private fun setupUi(header: Header?, magazines: List<Magazine>?, exception: Exception? = null) {
        when {
            header == null && magazines.isNullOrEmpty() && exception == null -> {
                Log.d("ListFragment", "Null Data")
                return
            }
            header != null && magazines != null && exception == null -> {
                Log.d("setupUi", "magazines: ${magazines[0].downloadId}")
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

        val magazineListener = MagazineAdapter.MagazineListener { magazine, action ->
            when {
                action == ClickAction.PREVIEW_OR_DELETE && magazine.fileUri == NO_FILE -> {
                    //preview
                    navigateToDetail(magazine.id)
                }
                action == ClickAction.DOWNLOAD_OR_READ && magazine.fileUri == NO_FILE -> {
                    //download
                    downloadMagazine(magazine)
                }
                action == ClickAction.PREVIEW_OR_DELETE && magazine.fileUri != NO_FILE -> {
                    //delete
                    viewModel.delete(magazine)
                }
                action == ClickAction.DOWNLOAD_OR_READ && magazine.fileUri != NO_FILE -> {
                    //read
                    navigateToPdf(magazine.fileUri)
                }
            }
        }
        adapter = MagazineAdapter(magazineListener, headerListener)
        binding.recyclerview.adapter = adapter
    }

    private fun navigateToPdf(fileUri: String) {
        val intent = Intent(context, PdfViewActivity::class.java).apply {
            putExtra(MAGAZINE_URI, fileUri)
        }
        startActivity(intent)
    }

    private fun downloadMagazine(magazine: Magazine) {
        if (networkCheck.checkConnectivity(requireContext())) {
            downloadWithPrDownloader(magazine, requireContext(), repository, recyclerview)
            isDownloadRunning = true
        } else {
            showErrorLayout(getString(R.string.network_down))
        }
    }


    private fun navigateToDetail(id: Long) {
        val pathBundle = Bundle().apply { putLong(MAGAZINE_ID, id) }
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

    val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (binding.searchView.isOpen) {
                binding.searchView.closeSearch()
            }
            if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
                binding.drawer.closeDrawer(GravityCompat.START)
            }
        }
    }

}