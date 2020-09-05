package com.ssquare.myapplication.monokrome.ui.main.list

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.ssquare.myapplication.monokrome.data.DomainMagazine
import com.ssquare.myapplication.monokrome.data.Repository
import com.ssquare.myapplication.monokrome.util.OrderBy
import kotlinx.coroutines.launch
import timber.log.Timber


class ListViewModel @ViewModelInject constructor(
    private val repository: Repository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _orderBy = MutableLiveData<OrderBy>()

    val data = Transformations.switchMap(_orderBy) {
        Timber.d("order by: $it")
        repository.getCachedData(it)
    }
    val networkError = repository.networkError
    var toDownloadMagazine: DomainMagazine? = null


    fun delete(magazine: DomainMagazine) = repository.delete(magazine)

    fun loadAndCacheData() {
        viewModelScope.launch {
            repository.loadAndCacheData()
        }
    }


    fun setToDownload(magazine: DomainMagazine?) {
        toDownloadMagazine = magazine
    }

    fun orderBy(orderBy: OrderBy) {
        _orderBy.postValue(orderBy)
    }

}