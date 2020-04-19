package com.ssquare.myapplication.monokrome.main.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.ssquare.myapplication.monokrome.main.data.Repository

@Suppress("UNCHECKED_CAST")
class ListViewModelFactory(private val reposiroty: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListViewModel::class.java)) {
            return ListViewModel(reposiroty) as T
        } else
            throw IllegalArgumentException("Unknown ViewModel class")
    }
}