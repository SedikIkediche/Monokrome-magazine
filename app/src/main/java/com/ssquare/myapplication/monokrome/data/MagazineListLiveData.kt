package com.ssquare.myapplication.monokrome.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

class MagazineListLiveData(
    header: LiveData<DomainHeader?>,
    magazines: LiveData<List<DomainMagazine>>
) :
    MediatorLiveData<Pair<DomainHeader?, List<DomainMagazine>?>>() {
    init {

        addSource(header) {
            postValue(Pair(it, magazines.value))
        }
        addSource(magazines) {
            postValue(Pair(header.value, it))
        }
    }

}