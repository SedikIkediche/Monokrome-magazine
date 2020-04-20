package com.ssquare.myapplication.monokrome.main.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssquare.myapplication.monokrome.main.data.Repository

@Suppress("UNCHECKED_CAST")
class DetailViewModelFactory(private val repository: Repository, private val path: String) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java))
            return DetailViewModel(repository, path) as T
        else
            throw IllegalArgumentException("Unknown ViewModel Class")
    }
}