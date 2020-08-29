package com.ssquare.myapplication.monokrome.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import timber.log.Timber

class MagazineListLiveData(
    header: LiveData<DomainHeader?>,
    magazines: LiveData<List<DomainMagazine>>,
    dataCallback: () -> Unit = {},
    dataEmptyCallback: () -> Unit = {}
) :
    MediatorLiveData<Pair<DomainHeader?, List<DomainMagazine>?>>() {
    init {

        addSource(header) {
            Timber.d("MagazineListOrLiveData($it, ${magazines.value}) header source")
            if (it == null && magazines.value.isNullOrEmpty()) {
                dataEmptyCallback()
            } else {
                dataCallback()
                postValue(Pair(it, magazines.value))
            }

        }
        addSource(magazines) {
            Timber.d("MagazineListOrLiveData(${header.value}, ${it}) magazines source")
            if (header.value == null && it.isNullOrEmpty()) {
                dataEmptyCallback()
            } else {
                dataCallback()
                postValue(Pair(header.value, it))

            }


        }
    }

}