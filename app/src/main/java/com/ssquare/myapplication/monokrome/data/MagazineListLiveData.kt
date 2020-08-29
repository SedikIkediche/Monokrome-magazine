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

    private var count = 0

    init {

        addSource(header) {
            Timber.d("MagazineListOrLiveData($it, ${magazines.value}) header source")
            if (it != null && !magazines.value.isNullOrEmpty()) {
                dataCallback()
                postValue(Pair(it, magazines.value))
            }

        }
        addSource(magazines) {
            count++
            Timber.d("MagazineListOrLiveData(${header.value}, ${it}) magazines source")
            if (header.value == null && it.isNullOrEmpty()) {
                if (count > 1)
                    dataEmptyCallback()
            } else {
                dataCallback()
                postValue(Pair(header.value, it))

            }


        }
    }

}