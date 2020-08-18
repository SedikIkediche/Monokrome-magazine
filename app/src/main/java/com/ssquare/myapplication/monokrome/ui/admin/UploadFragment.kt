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
import android.os.Environment
import android.provider.MediaStore
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.nbsp.materialfilepicker.MaterialFilePicker
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.databinding.FragmentUploadBinding
import com.ssquare.myapplication.monokrome.ui.main.MainActivity
import com.ssquare.myapplication.monokrome.util.*
import com.ssquare.myapplication.monokrome.util.networkcheck.ConnectivityProvider
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class UploadFragment : Fragment(), ConnectivityProvider.ConnectivityStateListener {

    lateinit var binding: FragmentUploadBinding

    @Inject
    lateinit var provider: ConnectivityProvider
    private var isConnected: Boolean = false
    private val viewModel: UploadViewModel by viewModels()
    private lateinit var alertDialog: AlertDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUploadBinding.inflate(inflater)
        initLayout()
        setUpAlertDialog()


        viewModel.image.observe(viewLifecycleOwner, Observer { image ->
            image?.let { displayImage(it) }
        })

        viewModel.file.observe(viewLifecycleOwner, Observer { file ->
            file?.let { displayFile(it) }
        })

        viewModel.uploadState.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it.magazine != null && it.exception == null) {
                    //hide
                    uploadSuccess()
                } else if (it.magazine == null && it.exception != null) {
                    showError(it.exception.message!!)
                }
            }
        })

        viewModel.releaseDate.observe(viewLifecycleOwner, Observer {
            it?.let { displayReleaseDate(it) }
        })

        binding.image.setOnClickListener {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when {
            requestCode == SELECT_FILE_CODE && resultCode == RESULT_OK -> {
                data ?: throw IllegalArgumentException("data must not be null")
                val path = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH)
                setFile(path)
            }
            resultCode == RESULT_OK && data != null && data.data != null && requestCode == SELECT_IMAGE_CODE -> {
                val uri = data.data
                setImage(uri)
            }
        }
    }

    private fun initLayout() {
        val imageBitmap = getBitmapFromVectorDrawable(requireContext(), R.drawable.ic_add_image)
        binding.image.setImageBitmap(imageBitmap)
    }

    private fun showError(message: String) {
        alertDialog.hideDialog()
        showTwoButtonDialog(
            activity = activity as MainActivity,
            title = getString(R.string.oops),
            message = message,
            positiveButtonText = getString(
                R.string.retry
            ),
            negativeButtonText = "Cancel",
            negativeFun = {
                navigateUp()
            }
        )
    }

    private fun setUpAlertDialog() {
        alertDialog = AlertDialog.Builder(requireContext()).create()
    }

    private fun showLoading() {
        alertDialog.showLoading(activity as MainActivity, R.string.uploading)
    }

    private fun uploadSuccess() {
        alertDialog.hide()
        showOneButtonDialog(
            activity as MainActivity,
            "Success",
            "issue uploaded successfully.",
            "Ok"
        ) {
            viewModel.loadAndCacheData()
            navigateUp()
        }
    }

    private fun navigateUp() {
        this.findNavController().navigate(R.id.action_uploadFragment_to_listFragment)
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

        } else {
            toast(requireContext(), "Storage Permission Denied")
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
        startActivityForResult(intent, SELECT_IMAGE_CODE)
    }

    private fun selectFile() {
        val externalStorage = Environment.getExternalStorageDirectory()
        MaterialFilePicker()
            .withSupportFragment(this)
            // With cross icon on the right side of toolbar for closing picker straight away
            .withCloseMenu(true)
            // Showing hidden files
            .withHiddenFiles(false)
            .withRootPath(externalStorage.absolutePath)
            // Want to choose only jpg images
            //.withFilter(Pattern.compile(".*\\.(jpg|jpeg)$"))
            // Don't apply filter to directories names
            .withFilterDirectories(false)
            .withTitle("Choose File")
            .withRequestCode(SELECT_FILE_CODE)
            .start()
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
        toast(requireContext(), fileType!!)
        if (fileType == "application/pdf") {
            viewModel.setFile(path)
        } else {
            showError("Document format must be pdf")
        }

    }

    private fun displayImage(image: Uri) {
        val imageBitmap = decodeBitmap(image)
        binding.image.setImageBitmap(imageBitmap)
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
        if (isConnected) {
            val title = binding.textTitle.text.toString()
            val description = binding.textDescription.text.toString()
            viewModel.setTitle(title)
            viewModel.setDescription(description)
            showLoading()
            viewModel.upload()
        } else {
            //show error (no internet)
            toast(requireContext(), "No internet")
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

    override fun onStateChange(state: ConnectivityProvider.NetworkState) {
        isConnected = state.hasInternet()
    }

    private fun pasteText(context: Context): String? {
        val clipboard = getSystemService(context, ClipboardManager::class.java) as ClipboardManager
        if ((clipboard.primaryClipDescription?.hasMimeType(MIMETYPE_TEXT_PLAIN) == true)) {
            val item = clipboard.primaryClip?.getItemAt(0)
            return item?.text.toString()
        }
        return null
    }
}