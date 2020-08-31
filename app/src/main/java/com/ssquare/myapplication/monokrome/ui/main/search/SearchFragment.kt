package com.ssquare.myapplication.monokrome.ui.main.search

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.data.DomainMagazine
import com.ssquare.myapplication.monokrome.data.getDownloadState
import com.ssquare.myapplication.monokrome.databinding.FragmentSearchBinding
import com.ssquare.myapplication.monokrome.ui.main.list.MagazineAdapter
import com.ssquare.myapplication.monokrome.ui.pdf.PdfViewActivity
import com.ssquare.myapplication.monokrome.util.*
import com.ssquare.myapplication.monokrome.util.networkcheck.ConnectivityProvider
import com.ssquare.myapplication.monokrome.util.networkcheck.ConnectivityProvider.Companion.hasInternet
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 */
@AndroidEntryPoint
class SearchFragment : Fragment(), ConnectivityProvider.ConnectivityStateListener {
    @Inject
    lateinit var provider: ConnectivityProvider

    @Inject
    lateinit var downloadUtils: DownloadUtils
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter: MagazineAdapter
    private lateinit var alertDialog: AlertDialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater)
        initDownloadUtils()
        initRecyclerView()
        setUpAlertDialog()
        setUpSearchView()
        setUpNavigateUpButton()
        setContainerBackgroundColor()
        viewModel.searchResult.observe(viewLifecycleOwner, Observer {
            setupUi(it)
        })

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

    private fun setupUi(it: List<DomainMagazine>?) {
        if (it.isNullOrEmpty()) {
            showEmpty()
        } else {
            showData(it)
        }
    }

    private fun showEmpty() {
        binding.run {
            recyclerViewSearch.visibility = View.GONE
            textError.visibility = View.VISIBLE
            emptyListImage.visibility = View.VISIBLE
            textError.text = getString(R.string.no_issues_available)
        }
    }

    private fun showData(magazines: List<DomainMagazine>?) {
        adapter.addHeaderAndSubmitList(magazines, null)
        binding.run {
            recyclerViewSearch.visibility = View.VISIBLE
            textError.visibility = View.GONE
            emptyListImage.visibility = View.GONE
        }
    }

    private fun setUpAlertDialog() {
        alertDialog = AlertDialog.Builder(requireContext()).create()
    }

    private fun showErrorDialog(message: String) {
        if (!alertDialog.isShowing)
            alertDialog = showOneButtonDialog(
                context = requireContext(),
                title = getString(R.string.oops),
                message = message,
                positiveButtonText = getString(
                    R.string.retry
                )
            )
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

    private fun setUpNavigateUpButton() {
        binding.searchToolBar.getChildAt(0).setOnClickListener {
            hideKeyBoard()
            findNavController().navigateUp()
        }
    }

    private fun setUpSearchView() {
        binding.searchView.requestFocus()
        showKeyBoard()
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Timber.d("onQueryTextSubmit called")
                viewModel.search(query)
                hideKeyBoard()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Timber.d("onQueryChange called")
                viewModel.search(newText)
                return true
            }
        })
    }

    private fun showKeyBoard() {
        getInputMethodManagerInstance()?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun hideKeyBoard() {
        getInputMethodManagerInstance()?.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun getInputMethodManagerInstance(): InputMethodManager? {
        return getSystemService(requireContext(), InputMethodManager::class.java)
    }


    private fun initRecyclerView() {
        val headerListener = MagazineAdapter.HeaderListener { /*Handle header click */ }
        val magazineListener = MagazineAdapter.MagazineListener { magazine, action ->
            when {
                (action == ClickAction.PREVIEW_OR_DELETE && magazine.getDownloadState() != DownloadState.COMPLETED)
                        || action == ClickAction.PREVIEW_ONLY -> {
                    //preview
                    navigateToDetail(magazine.id)
                }

                action == ClickAction.PREVIEW_OR_DELETE && magazine.getDownloadState() == DownloadState.COMPLETED -> {
                    //delete
                    viewModel.delete(magazine)
                }

                action == ClickAction.DOWNLOAD_OR_READ && magazine.getDownloadState() == DownloadState.EMPTY -> {
                    //download
                    checkForPermission(magazine)
                }

                action == ClickAction.DOWNLOAD_OR_READ && magazine.getDownloadState() == DownloadState.COMPLETED -> {
                    //read
                    navigateToPdf(magazine.fileUri)
                }
                action == ClickAction.DOWNLOAD_OR_READ
                        && (magazine.getDownloadState() == DownloadState.RUNNING || magazine.getDownloadState() == DownloadState.PENDING || magazine.getDownloadState() == DownloadState.PAUSED) -> {
                    //cancel
                    downloadUtils.cancelDownload(magazine.downloadId)
                }
            }
        }
        adapter = MagazineAdapter(magazineListener, headerListener)
        binding.recyclerViewSearch.adapter = adapter
    }

    private fun initDownloadUtils() {
        downloadUtils.isDownloadRunning.observe(viewLifecycleOwner, Observer { isDownloading ->
            commitDownloadActive(requireContext(), isDownloading)
            binding.recyclerViewSearch.itemAnimator =
                if (isDownloading) null else DefaultItemAnimator()

        })
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

    private fun downloadMagazine(magazine: DomainMagazine) {
        if (provider.getNetworkState().hasInternet()) {
            if (!isLoadDataActive(requireContext())) {
                downloadUtils.enqueueDownload(magazine, getAuthToken(requireContext()))
            } else {
                showErrorDialog(getString(R.string.loading_from_server))
            }

        } else {
            showErrorDialog(message = getString(R.string.connectivity_error_message))
        }
    }

    private fun navigateToDetail(id: Long) {
        val pathBundle = Bundle().apply { putLong(MAGAZINE_ID, id) }
        findNavController().navigate(R.id.action_searchFragment_to_detailFragment, pathBundle)
    }

    private fun navigateToPdf(fileUri: String) {
        val intent = Intent(context, PdfViewActivity::class.java).apply {
            putExtra(PDF_FILE_NAME, getPdfFileName(fileUri))
        }
        startActivity(intent)
    }

    override fun onStateChange(state: ConnectivityProvider.NetworkState) {
        if (!state.hasInternet() && isDownloadActive(requireContext())) {
            downloadUtils.killActiveDownloads()
            showErrorDialog(
                getString(R.string.network_down)
            )

        }
    }


}
