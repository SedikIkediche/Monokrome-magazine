package com.ssquare.myapplication.monokrome.ui.main.list

import androidx.lifecycle.ViewModel
import com.ssquare.myapplication.monokrome.data.Repository


class ListViewModel(private val repository: Repository) : ViewModel() {

    val data = repository.getCachedData()
    val networkError = repository.networkError

    fun loadAndCacheData() = repository.loadAndCacheData()

}