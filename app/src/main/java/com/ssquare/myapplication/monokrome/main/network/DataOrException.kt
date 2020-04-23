package com.ssquare.myapplication.monokrome.main.network

import com.ssquare.myapplication.monokrome.main.data.Header
import com.ssquare.myapplication.monokrome.main.data.Magazine

data class DataOrException<out T, out E : Exception?>(val data: T?, val exception: E?) {
    init {
        if (data == null && exception == null) {
            throw IllegalArgumentException("Both data and exception can't be null")
        } else if (data != null && exception != null) {
            throw IllegalArgumentException("Both data and exception can't be non-null")
        }
    }
}

data class MagazineListOrException(
    val magazineList: List<Magazine>?,
    val header: Header?,
    val exception: Exception?
) {
    init {
        if (magazineList == null && header == null && exception == null) {
            throw IllegalArgumentException("Both data and exception can't be null")
        } else if (magazineList != null && header == null && exception != null) {
            throw IllegalArgumentException("Both data and exception can't be non-null")
        }
    }
}