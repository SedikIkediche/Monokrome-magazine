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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.data.Repository
import com.ssquare.myapplication.monokrome.data.getDownloadState
import com.ssquare.myapplication.monokrome.databinding.FragmentDetailBinding
import com.ssquare.myapplication.monokrome.db.LocalCache
import com.ssquare.myapplication.monokrome.db.MagazineDatabase
import com.ssquare.myapplication.monokrome.network.MonokromeApi
import com.ssquare.myapplication.monokrome.ui.main.MainActivity
import com.ssquare.myapplication.monokrome.ui.pdf.PdfViewActivity
import com.ssquare.myapplication.monokrome.util.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
@AndroidEntryPoint
class DetailFragment : Fragment(), DetailClickListener {

    lateinit var binding: FragmentDetailBinding
    private val viewModel: DetailViewModel by viewModels()

    @Inject
    lateinit var downloadUtils: DownloadUtils

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getMagazineId()
        // Inflate the layout for this fragment
        binding = FragmentDetailBinding.inflate(inflater)
        binding.root.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.list_item_container_background
            )
        )

        initDownloadUtils()

        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.clickListener = this

        return binding.root
    }

    private fun getMagazineId() {
        val id = requireArguments().getLong(MAGAZINE_ID, -1)
        viewModel.getMagazine(id)
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
        // check for connectivity
        if (!isLoadDataActive(requireContext())) {
            downloadUtils.enqueueDownload(magazine)
        } else {
            toast(requireContext(), "Loading Data From Server!")
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