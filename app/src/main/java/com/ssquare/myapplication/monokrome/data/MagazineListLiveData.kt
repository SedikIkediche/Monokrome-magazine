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
            Timber.d("MagazineListOrLiveData($it, ${magazines.value})")
            if (it == null && magazines.value != null && magazines.value!!.isEmpty()) {
                dataEmptyCallback()
            } else {
                dataCallback()
                postValue(Pair(it, magazines.value))
            }

        }
        addSource(magazines) {
            Timber.d("MagazineListOrLiveData(${header.value}, ${it})")
            if (header.value == null && it != null && it.isEmpty()) {
                dataEmptyCallback()
            } else {
                dataCallback()
                postValue(Pair(header.value, it))

            }


        }
    }

}