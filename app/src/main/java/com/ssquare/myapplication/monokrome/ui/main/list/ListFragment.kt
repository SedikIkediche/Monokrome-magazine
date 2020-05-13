package com.ssquare.myapplication.monokrome.ui.main.list

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.data.DownloadState.*
import com.ssquare.myapplication.monokrome.data.Header
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.data.Repository
import com.ssquare.myapplication.monokrome.data.getDownloadState
import com.ssquare.myapplication.monokrome.databinding.FragmentListBinding
import com.ssquare.myapplication.monokrome.db.LocalCache
import com.ssquare.myapplication.monokrome.db.MagazineDatabase
import com.ssquare.myapplication.monokrome.network.FirebaseServer
import com.ssquare.myapplication.monokrome.ui.main.MainActivity
import com.ssquare.myapplication.monokrome.ui.pdf.PdfViewActivity
import com.ssquare.myapplication.monokrome.util.*
import com.ssquare.myapplication.monokrome.util.networkcheck.ConnectivityProvider

/**
 * A simple [Fragment] subclass.
 */
class ListFragment : Fragment() , ConnectivityProvider.ConnectivityStateListener {
    lateinit var binding: FragmentListBinding
    private lateinit var viewModel: ListViewModel
    private lateinit var adapter: MagazineAdapter
    private var isNotConnected = false
    private val provider: ConnectivityProvider by lazy { ConnectivityProvider.createProvider(requireContext()) }


    private val downloadUtils: DownloadUtils by lazy {
        (activity as MainActivity).downloadUtils
    }

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
        val repository = Repository.getInstance(requireContext(), lifecycleScope, cache, network)
        val factory = ListViewModelFactory(repository)
        viewModel = ViewModelProviders.of(this, factory).get(ListViewModel::class.java)

        initRecyclerView()
        initDownloadUtils()

        viewModel.networkError.observe(viewLifecycleOwner, Observer {
            setupUi(null, null, it)
        })

        viewModel.data.observe(viewLifecycleOwner, Observer {
            if (isDataCached(requireContext()))
            setupUi(it.first, it.second)
        })

        return binding.root
    }

    private fun initDownloadUtils() {
        downloadUtils.isDownloadRunning.observe(viewLifecycleOwner, Observer { isDownloading ->
            commitDownloadActive(requireContext(), isDownloading)
            binding.recyclerview.itemAnimator =
                if (isDownloading) null else DefaultItemAnimator()
        })
    }

    private fun setUpToolbar() {

        getMainActivity().setSupportActionBar(binding.listFragmentToolbar)
        getMainActivity().supportActionBar?.title = getString(R.string.home)

        setHasOptionsMenu(true)
        addBannerClickListener()
    }


    private fun checkForPermission(magazine: Magazine) {
        viewModel.setToDownload(magazine)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        } else {
            downloadMagazine(magazine)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            downloadMagazine(viewModel.toDownloadMagazine!!)
        } else {
            viewModel.setToDownload(null)
            toast(requireContext(), "Storage Permission Denied")
        }
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
    }

    private fun setupUi(header: Header?, magazines: List<Magazine>?, exception: Exception? = null) {
        when {
            header == null && magazines.isNullOrEmpty() && exception == null -> {
                Log.d("ListFragment", "Null Data")
                return
            }
            header != null && !magazines.isNullOrEmpty() && exception == null -> {
                adapter.addHeaderAndSubmitList(magazines, header)
                showData()
            }
            exception != null -> {
                showError("Something wrong happened: $exception")
            }
        }
    }

    private fun initRecyclerView() {
        Log.d("ListFragment", "initRecyclerView called")
        val headerListener = MagazineAdapter.HeaderListener { /*Handle header click */ }

        val magazineListener = MagazineAdapter.MagazineListener { magazine, action ->
            when {
                (action == ClickAction.PREVIEW_OR_DELETE && magazine.getDownloadState() != COMPLETED)
                        || action == ClickAction.PREVIEW_ONLY -> {
                    //preview
                    navigateToDetail(magazine.id)
                }

                action == ClickAction.PREVIEW_OR_DELETE && magazine.getDownloadState() == COMPLETED -> {
                    //delete
                    viewModel.delete(magazine)
                }

                action == ClickAction.DOWNLOAD_OR_READ && magazine.getDownloadState() == EMPTY -> {
                    //download
                    checkForPermission(magazine)
                }

                action == ClickAction.DOWNLOAD_OR_READ && magazine.getDownloadState() == COMPLETED -> {
                    //read
                    navigateToPdf(magazine.fileUri)
                }
                action == ClickAction.DOWNLOAD_OR_READ
                        && (magazine.getDownloadState() == RUNNING || magazine.getDownloadState() == PENDING || magazine.getDownloadState() == PAUSED) -> {
                    //cancel
                    downloadUtils.cancelDownload(magazine.downloadId)
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
        if (isConnected(requireContext())) {
            if (!isLoadDataActive(requireContext())) {
                downloadUtils.enqueueDownload(magazine)
            } else {
                toast(requireContext(), "Loading Data From Server!")
            }

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