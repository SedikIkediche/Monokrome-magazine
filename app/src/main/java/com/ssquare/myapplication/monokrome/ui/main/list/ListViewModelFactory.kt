package com.ssquare.myapplication.monokrome.ui.main.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssquare.myapplication.monokrome.data.Repository

@Suppress("UNCHECKED_CAST")
class ListViewModelFactory(private val reposiroty: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListViewModel::class.java)) {
            return ListViewModel(reposiroty) as T
        } else
            throw IllegalArgumentException("Unknown ViewModel class")
    }
}