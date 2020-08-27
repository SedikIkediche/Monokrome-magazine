package com.ssquare.myapplication.monokrome.ui.main.list

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DefaultItemAnimator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.data.DomainHeader
import com.ssquare.myapplication.monokrome.data.DomainMagazine
import com.ssquare.myapplication.monokrome.data.getDownloadState
import com.ssquare.myapplication.monokrome.databinding.FragmentListBinding
import com.ssquare.myapplication.monokrome.network.Error
import com.ssquare.myapplication.monokrome.ui.main.MainActivity
import com.ssquare.myapplication.monokrome.ui.pdf.PdfViewActivity
import com.ssquare.myapplication.monokrome.util.*
import com.ssquare.myapplication.monokrome.util.DownloadState.*
import com.ssquare.myapplication.monokrome.util.OrderBy.*
import com.ssquare.myapplication.monokrome.util.networkcheck.ConnectivityProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */

@AndroidEntryPoint
class ListFragment : Fragment(), ConnectivityProvider.ConnectivityStateListener {
    @Inject
    lateinit var downloadUtils: DownloadUtils

    @Inject
    lateinit var provider: ConnectivityProvider
    private val viewModel: ListViewModel by viewModels()
    lateinit var binding: FragmentListBinding

    private lateinit var adapter: MagazineAdapter
    private var isNotConnected = false

    private var isDownloadDialogShown = false

    private  lateinit var dialog: MaterialAlertDialogBuilder

    private val errorCallback = fun(){
        if (!isDownloadDialogShown){
            dialog.show()
            isDownloadDialogShown = true
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListBinding.inflate(inflater)
        setUpToolbar()
        initRecyclerView()
        initDownloadUtils()
        setContainerBackgroundColor()

        dialog = MaterialAlertDialogBuilder(requireActivity())
            .setTitle(getString(R.string.oops))
            .setMessage(getString(R.string.download_error_message))
            .setPositiveButton(getString(R.string.ok)) { dialog, which ->
                dialog.dismiss()
            }.setOnDismissListener {
                isDownloadDialogShown = false
            }
        downloadUtils.setErrorCallback(errorCallback)

        viewModel.orderBy(getOrderBy(requireContext()))
        viewModel.networkError.observe(viewLifecycleOwner, Observer {
            setupUi(null, null, it)
        })

        viewModel.data.observe(viewLifecycleOwner, Observer {
            if (isDataCached(requireContext())){
                setupUi(it.first, it.second)
                binding.swipeRefreshLayout.isRefreshing=false
            }
        })

        binding.swipeRefreshLayout.setOnRefreshListener {
            if (isDataCached(requireContext())){
                binding.swipeRefreshLayout.isRefreshing=false
            }else{
                cacheData()
            }

        }

        return binding.root
    }

    private fun setContainerBackgroundColor() {
        binding.root.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.list_item_container_background
            )
        )
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

    private fun checkForPermission(magazine: DomainMagazine) {
        viewModel.setToDownload(magazine)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE_PERMISSION_CODE
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
        if (requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
        setUpOrderByOption(menu)
    }

    private fun setUpOrderByOption(menu: Menu) {
        val orderBy = PreferenceManager.getDefaultSharedPreferences(context).getInt(
            ORDER_BY,
            0
        )
        when (orderBy) {
            MOST_RECENT.ordinal -> {
                menu.findItem(R.id.sort_by_most_recent).isChecked = true
            }
            A_TO_Z.ordinal -> {
                menu.findItem(R.id.sort_from_a_to_z).isChecked = true
            }
            Z_TO_A.ordinal -> {
                menu.findItem(R.id.sort_from_z_to_a).isChecked = true
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search -> {
                findNavController().navigate(R.id.searchFragment)
                true
            }
            R.id.sort_by_most_recent -> {
                item.isChecked = !item.isChecked
                orderBy(item.itemId)
                true
            }
            R.id.sort_from_a_to_z -> {
                item.isChecked = !item.isChecked
                orderBy(item.itemId)
                true
            }
            R.id.sort_from_z_to_a -> {
                item.isChecked = !item.isChecked
                orderBy(item.itemId)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun orderBy(id: Int) {
        when (id) {
            R.id.sort_by_most_recent -> {
                viewModel.orderBy(MOST_RECENT)
                commitOrderBy(requireContext(), MOST_RECENT)
            }
            R.id.sort_from_a_to_z -> {
                viewModel.orderBy(A_TO_Z)
                commitOrderBy(requireContext(), A_TO_Z)
            }
            R.id.sort_from_z_to_a -> {
                viewModel.orderBy(Z_TO_A)
                commitOrderBy(requireContext(), Z_TO_A)
            }
        }
    }

    private fun addBannerClickListener() {
        binding.closeButton.setOnClickListener {
            binding.bannerLayout.visibility = View.GONE
        }
        binding.tryAgainButton.setOnClickListener {
            binding.bannerLayout.visibility = View.GONE
            viewModel.loadAndCacheData()
        }
    }


    private fun cacheData() {
        viewModel.loadAndCacheData()
        commitCacheData(requireContext())
    }

    private fun setupUi(
        header: DomainHeader?,
        magazines: List<DomainMagazine>?,
        error: Error? = null
    ) {
        when {
            header == null && magazines.isNullOrEmpty() && error == null -> {
                return
            }
            header != null && !magazines.isNullOrEmpty() && error == null -> {
                adapter.addHeaderAndSubmitList(magazines, header)
                showData()
            }
            error != null -> {
                showError(error.message)
            }
        }
    }

    private fun initRecyclerView() {
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
            putExtra(PDF_FILE_NAME, getPdfFileName(fileUri))
        }
        startActivity(intent)
    }

    private fun downloadMagazine(magazine: DomainMagazine) {
        if (isConnected(requireContext())) {
            if (!isLoadDataActive(requireContext())) {
                downloadUtils.enqueueDownload(magazine, getAuthToken(requireContext()))
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

    private fun showError(errorText: String?) {
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

    private fun showErrorLayout(errorText: String?) {
        binding.run {
            errorContainer.visibility = View.VISIBLE
            textError.text = errorText ?: getString(R.string.general_error)
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