package com.ssquare.myapplication.monokrome.ui.main.search

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.data.Repository
import com.ssquare.myapplication.monokrome.data.getDownloadState
import com.ssquare.myapplication.monokrome.databinding.FragmentSearchBinding
import com.ssquare.myapplication.monokrome.db.LocalCache
import com.ssquare.myapplication.monokrome.db.MagazineDatabase
import com.ssquare.myapplication.monokrome.network.MonokromeApi
import com.ssquare.myapplication.monokrome.ui.main.MainActivity
import com.ssquare.myapplication.monokrome.ui.main.list.MagazineAdapter
import com.ssquare.myapplication.monokrome.ui.pdf.PdfViewActivity
import com.ssquare.myapplication.monokrome.util.*
import com.ssquare.myapplication.monokrome.util.networkcheck.ConnectivityProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 */
@AndroidEntryPoint
class SearchFragment : Fragment() {
    @Inject
    lateinit var provider: ConnectivityProvider
    @Inject
    lateinit var downloadUtils: DownloadUtils
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter: MagazineAdapter
    private var isNotConnected = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater)
        initDownloadUtils()
        initRecyclerView()
        setUpSearchView()
        setUpNavigateUpButton()

        viewModel.searchResult.observe(viewLifecycleOwner, Observer {
            setupUi(it)
        })

        return binding.root
    }

    private fun setupUi(it: List<Magazine>?) {
        if (it.isNullOrEmpty()) {
            showErrorLayout("No items found")
        } else {
            showData(it)
        }
    }

    private fun showErrorLayout(errorText: String) {
        binding.run {
            recyclerViewSearch.visibility = View.GONE
            textError.visibility = View.VISIBLE
            textError.text = errorText
            emptyListImage.visibility = View.VISIBLE
        }
    }

    private fun showData(magazines: List<Magazine>) {
        adapter.addHeaderAndSubmitList(magazines, null)
        binding.run {
            recyclerViewSearch.visibility = View.VISIBLE
            textError.visibility = View.GONE
            emptyListImage.visibility = View.GONE
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
                Log.d("SearchFragment", "onQueryTextSubmit called")
                viewModel.search(query)
                hideKeyBoard()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d("SearchFragment", "onQueryChange called")
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

    private fun downloadMagazine(magazine: Magazine) {
        if (isConnected(requireContext())) {
            if (!isLoadDataActive(requireContext())) {
                downloadUtils.enqueueDownload(magazine)
            } else {
                toast(requireContext(), "Loading Data From Server!")
            }

        } else {
            //show Error Layout (now internet)
        }
    }

    private fun navigateToDetail(id: Long) {
        val pathBundle = Bundle().apply { putLong(MAGAZINE_ID, id) }
        findNavController().navigate(R.id.action_searchFragment_to_detailFragment, pathBundle)
    }

    private fun navigateToPdf(fileUri: String) {
        val intent = Intent(context, PdfViewActivity::class.java).apply {
            putExtra(MAGAZINE_URI, fileUri)
        }
        startActivity(intent)
    }

}
