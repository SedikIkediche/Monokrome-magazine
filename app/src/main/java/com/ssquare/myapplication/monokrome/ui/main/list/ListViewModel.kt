package com.ssquare.myapplication.monokrome.ui.main.list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.data.Repository
import com.ssquare.myapplication.monokrome.util.*
import kotlinx.coroutines.launch


class ListViewModel(private val repository: Repository) : ViewModel() {

    private val _orderBy = MutableLiveData<OrderBy>()

    val data = Transformations.switchMap(_orderBy) {
        repository.getCachedData(it)
    }
    val networkError = repository.networkError
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

    fun loadAndCacheData() {
        viewModelScope.launch { repository.loadAndCacheData() }
    }

    fun setToDownload(magazine: Magazine?) {
        toDownloadMagazine = magazine
    }

    fun orderBy(orderBy: OrderBy) {
        _orderBy.postValue(orderBy)
    }

}