package com.ssquare.myapplication.monokrome.ui.main.detail

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.data.DomainMagazine
import com.ssquare.myapplication.monokrome.data.getDownloadState
import com.ssquare.myapplication.monokrome.databinding.FragmentDetailBinding
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
class DetailFragment : Fragment(), DetailClickListener {

    lateinit var binding: FragmentDetailBinding
    private val viewModel: DetailViewModel by viewModels()
    private lateinit var alertDialog: AlertDialog

    @Inject
    lateinit var downloadUtils: DownloadUtils

    @Inject
    lateinit var provider: ConnectivityProvider

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getMagazineId()
        // Inflate the layout for this fragment
        binding = FragmentDetailBinding.inflate(inflater)
        setContainerBackgroundColor()
        setUpAlertDialog()
        initDownloadUtils()

        closeButtonClickListener()

        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.clickListener = this

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

    private fun closeButtonClickListener() {
        binding.buttonClose.setOnClickListener {
            this.findNavController().navigateUp()
        }
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
        if (requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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

    private fun downloadMagazine(magazine: DomainMagazine) {
        if (provider.getNetworkState().hasInternet()) {
            if (!isLoadDataActive(requireContext())) {
                downloadUtils.enqueueDownload(magazine, getAuthToken(requireContext()))
            } else {
                toast(requireContext(), "Loading Data From Server!")
            }
        } else {
            showErrorDialog(getString(R.string.connectivity_error_message))
        }

    }

    private fun setUpAlertDialog() {
        alertDialog = AlertDialog.Builder(requireContext()).create()
    }

    private fun showErrorDialog(message: String) {
        alertDialog.hideDialog()
        showOneButtonDialog(
            context = requireContext(),
            title = getString(R.string.oops),
            message = message,
            positiveButtonText = getString(
                R.string.retry
            )
        )
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

    private fun navigateToPdf(fileUri: String) {
        val intent = Intent(context, PdfViewActivity::class.java).apply {
            putExtra(PDF_FILE_NAME, getPdfFileName(fileUri))
        }
        startActivity(intent)
    }

    override fun downloadOrRead(magazine: DomainMagazine) {
        Timber.d("Button Clicked")
        Timber.d("downloadState = ${magazine.getDownloadState()}")
        Timber.d("downloadId = ${magazine.downloadId}")
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