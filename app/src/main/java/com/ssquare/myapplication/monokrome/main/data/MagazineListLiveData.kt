package com.ssquare.myapplication.monokrome.main.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

class MagazineListLiveData(header: LiveData<Header>, magazines: LiveData<List<Magazine>>) :
    MediatorLiveData<Pair<Header?, List<Magazine>?>>() {
    init {
        addSource(header) {
            postValue(Pair(it, magazines.value))
        }
        addSource(magazines) {
            postValue(Pair(header.value, it))
        }
    }

}