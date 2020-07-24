package com.ssquare.myapplication.monokrome.ui.main.detail

import androidx.lifecycle.ViewModel
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.data.Repository
import com.ssquare.myapplication.monokrome.util.*

class DetailViewModel(private val repository: Repository, private val id: Long) :
    ViewModel() {

    val magazine = repository.getMagazine(id)
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

}