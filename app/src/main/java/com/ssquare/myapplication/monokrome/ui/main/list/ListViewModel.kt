package com.ssquare.myapplication.monokrome.ui.main.list

import androidx.lifecycle.ViewModel
import com.ssquare.myapplication.monokrome.data.Header
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.data.Repository


class ListViewModel(private val repository: Repository) : ViewModel() {


    val networkResponse = repository.networkResponse()
    val cachedData = repository.getCacheData()

    fun cacheData(header: Header?, magazines: List<Magazine>?) {
        if (header != null && magazines != null) {
            repository.cacheData(header, magazines)
        }
    }


}

