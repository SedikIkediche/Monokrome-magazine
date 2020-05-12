package com.ssquare.myapplication.monokrome.ui.main.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssquare.myapplication.monokrome.data.DownloadState
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.data.Repository
import com.ssquare.myapplication.monokrome.util.NO_DOWNLOAD
import com.ssquare.myapplication.monokrome.util.NO_FILE
import com.ssquare.myapplication.monokrome.util.NO_PROGRESS
import com.ssquare.myapplication.monokrome.util.deleteFile
import kotlinx.coroutines.launch


class ListViewModel(private val repository: Repository) : ViewModel() {

    val data = repository.getCachedData()
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

}