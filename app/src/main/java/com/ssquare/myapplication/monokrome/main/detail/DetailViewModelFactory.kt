package com.ssquare.myapplication.monokrome.main.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssquare.myapplication.monokrome.main.data.Repository
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class DetailViewModelFactory(private val repository: Repository, private val id: Long) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java))
            return DetailViewModel(repository, id) as T
        else
            throw IllegalArgumentException("Unknown ViewModel Class")
    }
}