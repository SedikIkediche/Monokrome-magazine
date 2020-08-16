package com.ssquare.myapplication.monokrome.ui.main.search

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.ssquare.myapplication.monokrome.data.DomainMagazine
import com.ssquare.myapplication.monokrome.data.Repository

class SearchViewModel @ViewModelInject constructor(private val repository: Repository) : ViewModel() {

    var toDownloadMagazine: DomainMagazine? = null
    private val _searchInput = MutableLiveData<String>()

    init {
        _searchInput.value = null
    }

    val searchResult = Transformations.switchMap(_searchInput) {
        repository.searchResult(it)
    }

    fun search(input: String?) {
        _searchInput.postValue(input)
    }

    fun delete(magazine: DomainMagazine) = repository.delete(magazine)

    fun setToDownload(magazine: DomainMagazine?) {
        toDownloadMagazine = magazine
    }

}