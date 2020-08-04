package com.ssquare.myapplication.monokrome.ui.main.detail

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.data.Repository
import com.ssquare.myapplication.monokrome.util.*

class DetailViewModel @ViewModelInject constructor(
    private val repository: Repository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _id = MutableLiveData<Long>()
    val magazine = Transformations.switchMap(_id) {
        repository.getMagazine(it)
    }
    var toDownloadMagazine: Magazine? = null

    fun delete(magazine: Magazine) {
        val fileDeleted = deleteFile(magazine.fileUri)
        if (fileDeleted) {
            repository.updateFileUri(magazine.id, NO_FILE)
            repository.updateDownloadProgress(magazine.id, NO_PROGRESS)
            repository.updateDownloadId(magazine.id, NO_DOWNLOAD)
            repository.updateDownloadState(magazine.id, DownloadState.EMPTY)
        }
    }

    fun setToDownload(magazine: Magazine?) {
        toDownloadMagazine = magazine
    }

    fun getMagazine(id: Long) {
        _id.value = id
    }

}