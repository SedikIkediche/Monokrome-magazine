package com.ssquare.myapplication.monokrome.ui.admin

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.databinding.FragmentUploadBinding
import com.ssquare.myapplication.monokrome.network.Error
import com.ssquare.myapplication.monokrome.util.*
import com.ssquare.myapplication.monokrome.util.networkcheck.ConnectivityProvider
import com.ssquare.myapplication.monokrome.util.networkcheck.ConnectivityProvider.Companion.hasInternet
import dagger.hilt.android.AndroidEntryPoint
import id.zelory.compressor.Compressor
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class UploadFragment : Fragment(), ConnectivityProvider.ConnectivityStateListener {

    lateinit var binding: FragmentUploadBinding

    @Inject
    lateinit var provider: ConnectivityProvider

    @Inject
    lateinit var downloadUtils: DownloadUtils
    private val viewModel: UploadViewModel by viewModels()
    private lateinit var alertDialog: AlertDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUploadBinding.inflate(inflater)
        initLayout()
        setUpAlertDialog()
        initDownloadUtils()
        setContainerBackgroundColor()

        viewModel.image.observe(viewLifecycleOwner, Observer { image ->
            image?.let { displayImage(it) }
        })

        viewModel.file.observe(viewLifecycleOwner, Observer { file ->
            file?.let { displayFile(it) }
        })

        viewModel.uploadState.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it.magazine != null && it.error == null) {
                    //hide
                    uploadSuccess()
                    Timber.tag("Upload").d("Upload State observer called")
                } else if (it.magazine == null && it.error != null) {
                    handleError(it.error)
                }
            }
        })

        viewModel.releaseDate.observe(viewLifecycleOwner, Observer {
            it?.let { displayReleaseDate(it) }
        })

        binding.containerImage.setOnClickListener {
            checkForPermission(SELECT_IMAGE_CODE)
        }

        binding.textEdition.setOnClickListener {
            checkForPermission(SELECT_FILE_CODE)
        }

        binding.buttonUpload.setOnClickListener {
            checkForPermission(UPLOAD_CODE)
        }


        binding.buttonPasteTitle.setOnClickListener {
            binding.textTitle.setText(pasteText(requireContext()))
        }

        binding.buttonPasteDescription.setOnClickListener {
            binding.textDescription.setText(pasteText(requireContext()))
        }

        binding.textReleaseDate.setOnClickListener {
            selectDate(requireContext())
        }

        closeButtonClickListener()

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        provider.addListener(this)
    }

    override fun onStop() {
        super.onStop()
        provider.removeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        deleteTempCachedFile()
        deleteTempImageFile()
    }

    private fun initDownloadUtils() {
        downloadUtils.isDownloadRunning.observe(viewLifecycleOwner, Observer { isDownloading ->
            commitDownloadActive(requireContext(), isDownloading)
        })
    }

    private fun deleteTempImageFile() {
        requireContext().cacheDir.listFiles()?.forEach { file ->
            if (file.name == getString(R.string.file_name_compressor)) {
                file.delete()
                Timber.d("Deleted file image: ${file.name}")
            }
        }
    }

    private fun deleteTempCachedFile() {
        requireContext().cacheDir.listFiles()?.forEach { file ->
            if (file.name == getString(R.string.file_name_temp_pdf)) {
                file.delete()
                Timber.d("Deleted file pdf: ${file.name}")
            }
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when {
            requestCode == SELECT_FILE_CODE && resultCode == RESULT_OK -> {
                data ?: throw IllegalArgumentException("data must not be null")
                val path = data.data!!
                setFile(
                    FileUtils.createTempFileInCache(
                        FileUtils.getDisplayName(
                            path,
                            requireContext()
                        ), requireContext(), path
                    )
                )
            }
            resultCode == RESULT_OK && data != null && data.data != null && requestCode == SELECT_IMAGE_CODE -> {
                val uri = data.data
                compressImage(uri)
            }
        }
    }

    private fun compressImage(uri: Uri?) {
        val actualImageFile = File(
            FileUtils.createTempFileInCache(
                FileUtils.getDisplayName(uri!!, requireContext()),
                requireContext(),
                uri
            )
        )
        lifecycleScope.launch {
            val compressedImageFile = Compressor.compress(requireContext(), actualImageFile)
            setImage(compressedImageFile.toUri())
            Timber.d("image size ${compressedImageFile.length() / 1024}")
        }
    }

    private fun initLayout() {
        val imageBitmap = getBitmapFromVectorDrawable(requireContext(), R.drawable.ic_add_image)
        binding.image.setImageBitmap(imageBitmap)
    }

    private fun showErrorDialog(message: String) {
        alertDialog.hide()
        binding.buttonUpload.isClickable = true
        showTwoButtonDialog(
            context = requireContext(),
            title = getString(R.string.oops),
            message = message,
            positiveButtonText = getString(
                R.string.retry
            ),
            negativeButtonText = getString(
                R.string.cancel
            ),
            negativeFun = {
                navigateUp()
            }
        )
    }

    private fun handleError(error: Error) {
        Timber.d("error message:${error.message}")
        when {
            error.code == 404 -> {
                showErrorDialog(getString(R.string.error_entries_null))
            }
            error.code == 400 -> {
                showErrorDialog(getString(R.string.failed_to_upload))
            }
            error.code == 415 -> {
                showErrorDialog(getString(R.string.wrong_format))
            }

            error.message == getString(R.string.software_connection_abort) -> {
                showErrorDialog(getString(R.string.network_down))
            }

            error.message == getString(R.string.error_connection_timed_out) || error.message == getString(
                R.string.error_timeout
            ) -> {
                viewModel.abortUpload()
                showErrorDialog(getString(R.string.failed_connect_to_server))
            }


            else -> {
                showErrorDialog(getString(R.string.internal_server_error))
            }
        }

    }

    private fun setUpAlertDialog() {
        alertDialog = AlertDialog.Builder(requireContext()).create()
    }

    private fun showLoading() {
        Timber.tag("Upload").d("showLoading() called")
        binding.buttonUpload.isClickable = false
        alertDialog.showLoading(requireContext(), R.string.uploading)
    }

    private fun uploadSuccess() {
        alertDialog.hideDialog()
        binding.buttonUpload.isClickable = true
        showOneButtonDialog(
            requireContext(),
            getString(R.string.success),
            getString(R.string.issue_uploaded_successfully),
            getString(R.string.ok), dismissFun = {
                navigateUp()
            }
        )
    }

    private fun navigateUp() {
        this.findNavController().navigateUp()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                SELECT_IMAGE_CODE -> selectImage()
                SELECT_FILE_CODE -> selectFile()
                UPLOAD_CODE -> upload()
            }

        }
    }

    private fun checkForPermission(code: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                code
            )
        } else {
            when (code) {
                SELECT_IMAGE_CODE -> selectImage()
                SELECT_FILE_CODE -> selectFile()
                UPLOAD_CODE -> upload()
            }
        }
    }

    private fun selectImage() {
        val intent = Intent().apply {
            action = Intent.ACTION_PICK
            type = "image/*"
        }
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(intent, SELECT_IMAGE_CODE)
        } else {
            alertDialog.hide()
            showOneButtonDialog(
                requireContext(),
                message = getString(R.string.install_gallery_app),
                positiveButtonText = getString(R.string.ok),
                title = getString(R.string.oops)
            )
        }
    }

    private fun selectFile() {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = FileUtils.MIME_TYPE_PDF
        }

        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(intent, SELECT_FILE_CODE)
        } else {
            alertDialog.hide()
            alertDialog = showOneButtonDialog(
                requireContext(),
                message = getString(R.string.intall_file_manager),
                positiveButtonText = getString(R.string.ok),
                title = getString(R.string.oops)
            )
        }
    }

    private fun selectDate(context: Context) {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        DatePickerDialog(
            context,

            R.style.date_picker_theme,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.apply {
                    set(year, month, dayOfMonth)
                    val timestamp = timeInMillis / 1000
                    viewModel.setReleaseDate(timestamp)
                }
            },
            currentYear,
            currentMonth,
            currentDay
        ).show()
    }

    private fun setImage(image: Uri?) {
        Timber.d("image uri: $image")
        viewModel.setImage(image)
    }

    private fun setFile(path: String?) {
        val fileType = FileUtils.getTypeFromPath(path)
        if (fileType == FileUtils.MIME_TYPE_PDF) {
            viewModel.setFile(path)
        } else {
            showErrorDialog(getString(R.string.document_format))
        }

    }

    private fun displayImage(image: Uri) {
        val imageBitmap = decodeBitmap(image)
        binding.selectedImage.setImageBitmap(imageBitmap)
    }

    private fun displayFile(path: String) {
        binding.textEdition.text = FileUtils.getFileNameFromPath(path)
    }

    private fun displayReleaseDate(time: Long) {
        val calendar = Calendar.getInstance().apply { timeInMillis = time * 1000 }
        val date = DateFormat.format("dd MMMM yyyy", calendar).toString()
        binding.textReleaseDate.text = date
    }

    private fun upload() {
        Timber.tag("Upload").d("upload() called")
        if (provider.getNetworkState().hasInternet()) {
            Timber.tag("Upload").d("connectivity: yes")
            if (!isDownloadActive(requireContext())) {
                Timber.tag("Upload").d("Download active: yes")
                val title = binding.textTitle.text.toString().trim()
                val description = binding.textDescription.text.toString().trim()
                viewModel.setTitle(title)
                viewModel.setDescription(description)
                viewModel.upload {
                    showLoading()
                }
            } else {
                Timber.tag("Upload").d("DownloadActive: no")
                showErrorDialog(getString(R.string.error_wait_for_download))
            }
        } else {
            Timber.tag("Upload").d("connectivity: no")
            showErrorDialog(getString(R.string.connectivity_error_message))
        }

    }

    private fun decodeBitmap(selectedPhotoUri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= 28) {
            val source =
                ImageDecoder.createSource(requireContext().contentResolver, selectedPhotoUri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(
                requireContext().contentResolver,
                selectedPhotoUri
            )
        }
    }


    private fun pasteText(context: Context): String? {
        val clipboard = getSystemService(context, ClipboardManager::class.java) as ClipboardManager
        if ((clipboard.primaryClipDescription?.hasMimeType(MIMETYPE_TEXT_PLAIN) == true)) {
            val item = clipboard.primaryClip?.getItemAt(0)
            return item?.text.toString()
        }
        return null
    }

    override fun onStateChange(state: ConnectivityProvider.NetworkState) {
        if (!state.hasInternet()) {
            when {
                isDownloadActive(requireContext()) -> {
                    downloadUtils.killActiveDownloads()
                    showErrorDialog(
                        getString(R.string.network_down)
                    )
                }

                isUploadingActive(requireContext()) -> {
                    viewModel.abortUpload()
                }
            }
        }
    }
}