package com.ssquare.myapplication.monokrome.ui.main.list

import android.util.Log
import androidx.lifecycle.ViewModel
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.data.Repository
import com.ssquare.myapplication.monokrome.util.NO_FILE
import java.io.File
import java.net.URI


class ListViewModel(private val repository: Repository) : ViewModel() {

    val data = repository.getCachedData()
    val networkError = repository.networkError

    fun delete(magazine: Magazine) {
        val file = File(URI.create(magazine.fileUri!!))
        if (file.exists()) {
            Log.d("ListViewModel", "file exists = true")
            file.delete()
            repository.updateFileUri(magazine.id, NO_FILE)
        } else {
            Log.d("ListViewModel", "file exists = false")
        }

    }

    fun loadAndCacheData() = repository.loadAndCacheData()

}