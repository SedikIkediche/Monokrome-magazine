package com.ssquare.myapplication.monokrome.ui.main.list

import androidx.lifecycle.ViewModel
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.data.Repository
import com.ssquare.myapplication.monokrome.util.NO_FILE
import com.ssquare.myapplication.monokrome.util.deleteFile


class ListViewModel(private val repository: Repository) : ViewModel() {

    val data = repository.getCachedData()
    val networkError = repository.networkError

    fun delete(magazine: Magazine) {
        val fileDeleted = deleteFile(magazine.fileUri)
        if (fileDeleted)
            repository.updateFileUri(magazine.id, NO_FILE)
    }

    fun loadAndCacheData() = repository.loadAndCacheData()

}