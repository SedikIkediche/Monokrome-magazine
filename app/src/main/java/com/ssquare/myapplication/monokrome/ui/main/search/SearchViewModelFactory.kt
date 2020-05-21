package com.ssquare.myapplication.monokrome.ui.main.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssquare.myapplication.monokrome.data.Repository

class SearchViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(repository) as T
        } else
            throw IllegalArgumentException("Unknown ViewModel class")
    }
}