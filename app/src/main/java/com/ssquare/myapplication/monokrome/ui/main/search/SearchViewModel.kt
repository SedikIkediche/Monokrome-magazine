package com.ssquare.myapplication.monokrome.ui.main.search

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.data.Repository
import com.ssquare.myapplication.monokrome.util.*

class SearchViewModel @ViewModelInject constructor(private val repository: Repository) : ViewModel() {

    var toDownloadMagazine: Magazine? = null
    private val _searchInput = MutableLiveData<String>()

    init {
        _searchInput.value = null
    }

    val searchResult = Transformations.switchMap(_searchInput) {
        repository.searchResult(it)
    }

    fun search(input: String?) {
        _searchInput.postValue(input)
    }

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

}