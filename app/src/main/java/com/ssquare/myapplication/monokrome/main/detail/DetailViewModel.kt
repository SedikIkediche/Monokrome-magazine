package com.ssquare.myapplication.monokrome.main.detail

import androidx.lifecycle.ViewModel
import com.ssquare.myapplication.monokrome.main.data.Repository

class DetailViewModel(private val repository: Repository, private val magazineId: Long) : ViewModel() {

    val magazine = repository.getMagazine(magazineId)


}