package com.ssquare.myapplication.monokrome.ui.main.detail

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.data.DownloadState
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.data.Repository
import com.ssquare.myapplication.monokrome.data.getDownloadState
import com.ssquare.myapplication.monokrome.databinding.FragmentDetailBinding
import com.ssquare.myapplication.monokrome.db.LocalCache
import com.ssquare.myapplication.monokrome.db.MagazineDatabase
import com.ssquare.myapplication.monokrome.network.FirebaseServer
import com.ssquare.myapplication.monokrome.ui.main.MainActivity
import com.ssquare.myapplication.monokrome.ui.pdf.PdfViewActivity
import com.ssquare.myapplication.monokrome.util.*

/**
 * A simple [Fragment] subclass.
 */

interface DetailClickListener {
    fun downloadOrRead(magazine: Magazine)
}

class DetailFragment : Fragment(), DetailClickListener {


    lateinit var binding: FragmentDetailBinding
    private lateinit var viewModel: DetailViewModel
    private val downloadUtils: DownloadUtils by lazy {
        (activity as MainActivity).downloadUtils
    }
    private val networkCheck: NetworkCheck by lazy {
        NetworkCheck(
            requireContext(),
            lifecycleScope
        ).apply { registerNetworkCallback() }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailBinding.inflate(inflater)

        val id = requireArguments().getLong(MAGAZINE_ID, -1)
        val database = FirebaseDatabase.getInstance()
        val storage = FirebaseStorage.getInstance()
        val network = FirebaseServer(database, storage)
        val magazineDao = MagazineDatabase.getInstance(requireContext()).magazineDao
        val headerDao = MagazineDatabase.getInstance(requireContext()).headerDao
        val cache = LocalCache(magazineDao, headerDao)
        val repository = Repository.getInstance(requireContext(), lifecycleScope, cache, network)
        val factory = DetailViewModelFactory(repository, id)
        viewModel = ViewModelProviders.of(this, factory).get(DetailViewModel::class.java)

        initDownloadUtils()

        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.clickListener = this

        return binding.root
    }

    override fun onPause() {
        networkCheck.unregisterNetworkCallback()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        networkCheck.registerNetworkCallback()
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

    private fun initDownloadUtils() {
        downloadUtils.isDownloadRunning.observe(viewLifecycleOwner, Observer { isDownloading ->
            commitDownloadActive(requireContext(), isDownloading)
            //updateUi Accordingly
        })
    }


    private fun downloadMagazine(magazine: Magazine) {
        if (networkCheck.checkConnectivity(requireContext())) {
            if (!isLoadDataActive(requireContext())) {
                downloadUtils.enqueueDownload(magazine)
            } else {
                toast(requireContext(), "Loading Data From Server!")
            }
        } else {
            showErrorLayout(getString(R.string.network_down))
        }
    }

    private fun showErrorLayout(error: String) {
        toast(requireContext(), error)
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

    private fun navigateToPdf(fileUri: String) {
        val intent = Intent(context, PdfViewActivity::class.java).apply {
            putExtra(MAGAZINE_URI, fileUri)
        }
        startActivity(intent)
    }

    override fun downloadOrRead(magazine: Magazine) {
        Log.d("DetailFragment", "Button Clicked")
        Log.d("DetailFragment", "downloadState = ${magazine.getDownloadState()}")
        Log.d("DetailFragment", "downloadId = ${magazine.downloadId}")
        when (magazine.getDownloadState()) {
            DownloadState.EMPTY -> {
                //download
                checkForPermission(magazine)
            }

            DownloadState.COMPLETED -> {
                //read
                navigateToPdf(magazine.fileUri)
            }
            DownloadState.RUNNING, DownloadState.PENDING, DownloadState.PAUSED -> {
                //cancel
                downloadUtils.cancelDownload(magazine.downloadId)
            }
        }

    }


}
