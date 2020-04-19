package com.ssquare.myapplication.monokrome.main.list

import androidx.lifecycle.ViewModel
import com.ssquare.myapplication.monokrome.main.data.Repository

class ListViewModel(private val repository: Repository) : ViewModel() {

    val magazines = repository.getMagazineList()
}