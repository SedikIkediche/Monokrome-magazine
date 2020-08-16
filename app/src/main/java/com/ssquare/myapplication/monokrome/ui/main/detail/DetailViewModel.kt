package com.ssquare.myapplication.monokrome.ui.main.detail

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.ssquare.myapplication.monokrome.data.DomainMagazine
import com.ssquare.myapplication.monokrome.data.Repository

class DetailViewModel @ViewModelInject constructor(
    private val repository: Repository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _id = MutableLiveData<Long>()
    val magazine = Transformations.switchMap(_id) {
        repository.getMagazine(it)
    }
    var toDownloadMagazine: DomainMagazine? = null

    fun delete(magazine: DomainMagazine) = repository.delete(magazine)

    fun setToDownload(magazine: DomainMagazine?) {
        toDownloadMagazine = magazine
    }

    fun getMagazine(id: Long) {
        _id.value = id
    }

}