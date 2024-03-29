package com.ssquare.myapplication.monokrome.ui.admin

import android.content.Context
import android.net.Uri
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.data.Repository
import com.ssquare.myapplication.monokrome.network.Error
import com.ssquare.myapplication.monokrome.network.MagazineOrError
import com.ssquare.myapplication.monokrome.util.FileUtils
import com.ssquare.myapplication.monokrome.util.JPEGTYPE
import com.ssquare.myapplication.monokrome.util.PNGTYPE
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class UploadViewModel @ViewModelInject constructor(
    private val repository: Repository,
    @Assisted private val savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _title = MutableLiveData<String>()
    val title: LiveData<String>
        get() = _title

    private val _description = MutableLiveData<String>()
    val description: LiveData<String>
        get() = _description

    private val _image = MutableLiveData<Uri>()
    val image: LiveData<Uri>
        get() = _image

    private val _file = MutableLiveData<String>()
    val file: LiveData<String>
        get() = _file

    private val _releaseDate = MutableLiveData<Long>()
    val releaseDate: LiveData<Long>
        get() = _releaseDate

    private val _uploadState = MutableLiveData<MagazineOrError>()
    val uploadState: LiveData<MagazineOrError>
        get() = _uploadState

    init {
        _releaseDate.value = System.currentTimeMillis() / 1000
    }

    fun setImage(image: Uri?) {
        _image.value = image
    }

    fun setFile(path: String?) {
        _file.value = path
    }

    fun setReleaseDate(releaseDate: Long) {
        _releaseDate.value = releaseDate
    }

    fun setTitle(title: String) {
        _title.value = title
    }

    fun setDescription(description: String) {
        _description.value = description
    }

    fun upload(callback: () -> Unit = {}) {
        Timber.tag("Upload").d("viewModel.upload() called")
        if (!title.value.isNullOrEmpty() && !description.value.isNullOrEmpty() && _image.value != null && _file.value != null) {
            when(FileUtils.getTypeFromPath(FileUtils.getPath(context,image.value!!))){
                JPEGTYPE, PNGTYPE -> {
                    callback()
                    viewModelScope.launch {
                        val magazineOrException = repository.uploadIssue(
                            title.value!!,
                            description.value!!,
                            _image.value!!,
                            _file.value!!,
                            releaseDate.value!!
                        )
                        _uploadState.value = magazineOrException
                    }
                }else -> {
                _uploadState.value =
                    MagazineOrError(null, Error(code = 415))
            }
            }

        } else {
            Timber.d("empty entry is: title: ${title.value.isNullOrEmpty()}, description: ${description.value.isNullOrEmpty()}, image: ${_image.value == null}, file: ${_file.value == null}")
            _uploadState.value = MagazineOrError(null, Error(code = 404))
        }
    }

    fun abortUpload() = repository.cancelNetworkOperations()


}